// SonarQube Go Plugin
// Copyright (C) 2018-2019 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

//go:generate go run generate_source.go

import (
	"bytes"
	"errors"
	"fmt"
	"go/ast"
	"go/parser"
	"go/token"
	"io/ioutil"
	"os"
	"strings"
	"unicode/utf8"
)


type Token struct {
	Value  string `json:"value,omitempty"`
	Line   int    `json:"line"`
	Column int    `json:"column"`
}

type Node struct {
	Token      *Token  `json:"token,omitempty"`
	Children   []*Node `json:"children,omitempty"`
	// internal fields
	offset    int // position of first character belonging to the node
	endOffset int // position of first character immediately after the node
	//Slang fields
	SlangTree string
	TextRange  *TextRange
	ParentField string
}

type TextRange struct {
	Start *TextPointer
	End   *TextPointer
}

type TextPointer struct {
	Line       int
	LineOffset int
}

func toSlangTree(fileSet *token.FileSet, astFile *ast.File, fileContent string) *Node {
	return NewUastMapper(fileSet, astFile, fileContent).toUast()
}

func readAstFile(filename string) (fileSet *token.FileSet, astFile *ast.File, fileContent string, err error) {
	var bytes []byte
	if filename == "-" {
		bytes, err = ioutil.ReadAll(os.Stdin)
	} else {
		bytes, err = ioutil.ReadFile(filename)
	}
	if err != nil {
		return
	}
	fileContent = string(bytes)
	fileSet, astFile, err = readAstString(filename, fileContent)
	return
}

func readAstString(filename string, fileContent string) (fileSet *token.FileSet, astFile *ast.File, err error) {
	fileSet = token.NewFileSet()
	astFile, err = parser.ParseFile(fileSet, filename, fileContent, parser.ParseComments)
	if err != nil {
		return
	}
	fileSize := fileSet.File(astFile.Pos()).Size()
	if len(fileContent) != fileSize {
		err = errors.New(fmt.Sprintf("Unexpected file size, expect %d instead of %d for file %s",
			len(fileContent), fileSize, filename))
	}
	return
}

type UastMapper struct {
	astFile           *ast.File
	fileContent       string
	hasCarriageReturn bool
	file              *token.File
	comments          []*Node
	commentPos        int
	paranoiac         bool
}

func NewUastMapper(fileSet *token.FileSet, astFile *ast.File, fileContent string) *UastMapper {
	t := &UastMapper{
		astFile:           astFile,
		fileContent:       fileContent,
		hasCarriageReturn: strings.IndexByte(fileContent, '\r') != -1,
		file:              fileSet.File(astFile.Pos()),
		paranoiac:         true,
	}
	//t.comments = t.mapAllComments()
	//t.commentPos = 0
	return t
}

func (t *UastMapper) toUast() *Node {
	compilationUnit := t.mapFile(t.astFile,  "")

	if t.paranoiac && (compilationUnit.offset < 0 || compilationUnit.endOffset > len(t.fileContent)) {
		panic("Unexpected compilationUnit" + t.location(compilationUnit.offset, compilationUnit.endOffset))
	}
	return compilationUnit
}

func (t *UastMapper) mapPackageDecl(file *ast.File) *Node {
	var children []*Node
	// "package" node is the very first node, header comments are appended before
	packageNode := t.createUastExpectedToken(file.Package, token.PACKAGE.String(), "")
	if packageNode != nil {
		children = t.appendCommentOrMissingToken(children, 0, packageNode.offset)
		children = append(children, packageNode)
	}
	children = t.appendNode(children, t.mapIdent(file.Name, "Name"))
	return t.createNativeNode(nil, children, "File.Package")
}

func (t *UastMapper) appendNodeList(parentList []*Node, children []*Node, nativeNode string) []*Node {
	// TODO provide the next Token offset, so the last separator can be part of the children
	return t.appendNode(parentList, t.createNativeNode(nil, children, nativeNode))
}

func (t *UastMapper) mapBasicLit(astNode *ast.BasicLit, fieldName string) *Node {
	if astNode == nil {
		return nil
	}

	return t.createUastExpectedToken(astNode.Pos(), astNode.Value, fieldName+"(BasicLit)")
}

func (t *UastMapper) handleSwitchCase(casePos token.Pos, isDefault bool, children []*Node) []*Node {
	tok := token.CASE
	if isDefault {
		tok = token.DEFAULT
	}
	children = t.appendNode(children, t.createUastTokenFromPosAstToken(casePos, tok, "Case"))
	return children
}

func (t *UastMapper) mapIfStmt(astNode *ast.IfStmt, fieldName string) *Node {
	if astNode == nil {
		return nil
	}
	var children []*Node
	children = t.appendNode(children, t.createUastTokenFromPosAstToken(astNode.If, token.IF, "If"))
	children = t.appendNode(children, t.createAdditionalInitAndCond(astNode.Init, astNode.Cond))
	children = t.appendNode(children, t.mapBlockStmt(astNode.Body, "Body"))
	children = t.appendNode(children, t.mapStmt(astNode.Else, "Else"))
	return t.createNativeNode(astNode, children, fieldName+"(IfStmt)")
}

func (t *UastMapper) createAdditionalInitAndCond(astInit ast.Stmt, astCond ast.Expr) *Node {
	var children []*Node
	children = t.appendNode(children, t.mapStmt(astInit, "Init"))
	children = t.appendNode(children, t.mapExpr(astCond,  "Cond"))
	return t.createNativeNode( nil, children, "InitAndCond")
}

//Create Native node
func (t *UastMapper) createNativeNode(astNode ast.Node, children []*Node, nativeNode string) *Node {
	if len(children) > 0 {
		return &Node{
			Children:   children,
			offset:     children[0].offset,
			endOffset:  children[len(children)-1].endOffset,
			SlangTree: "Native",
			TextRange: &TextRange{Start: children[0].TextRange.Start, End: children[len(children)-1].TextRange.End},
			ParentField: "nativeChild",
		}

	} else if astNode != nil {
		offset := t.file.Offset(astNode.Pos())
		endOffset := t.file.Offset(astNode.End())
		return t.createToken(offset, endOffset, nativeNode)
	} else {
		return nil
	}
}

func (t *UastMapper) createToken(offset, endOffset int, nativeNode string) *Node {
	if offset < 0 || endOffset < offset || endOffset > len(t.fileContent) {
		location := t.location(offset, endOffset)
		panic("Invalid token" + location)
	}
	if endOffset == offset{
		if t.paranoiac {
			location := t.location(offset, endOffset)
			panic("Invalid empty token" + location)
		}
		return nil
	}

	startPosition := t.toPosition(offset)
	endPosition := t.toPosition(endOffset)
	if !startPosition.IsValid() || !endPosition.IsValid() {
		if t.paranoiac {
			location := t.location(offset, endOffset)
			panic("Invalid token position" + location)
		}
		return nil
	}
	startLine := startPosition.Line
	startLineOffset := offset - startPosition.Column + 1
	startColumn := utf8.RuneCountInString(t.fileContent[startLineOffset:offset]) + 1

	endLine := endPosition.Line
	endLineOffset := endOffset - endPosition.Column + 1
	endColumn := utf8.RuneCountInString(t.fileContent[endLineOffset:endOffset]) + 1

	if offset > 0 && offset == len(t.fileContent) && isEndOfLine(t.fileContent[offset-1]) {
		startLine++
		startColumn = 1
	}
	if offset > 0 && endOffset == len(t.fileContent) && isEndOfLine(t.fileContent[endOffset-1]) {
		endLine++
		endColumn = 1
	}

	return &Node{
		Token: &Token{
			Line:   startLine,
			Column: startColumn,
			Value:  t.fileContent[offset:endOffset],
		},
		offset:     offset,
		endOffset:  endOffset,
		SlangTree: "Token",
		TextRange: &TextRange{
			Start:  &TextPointer{Line: startLine, LineOffset: startColumn},
			End:    &TextPointer{Line: endLine, LineOffset: endColumn},
		},
		ParentField: "",
	}
}

func (t *UastMapper) createUastTokenFromPosAstToken(pos token.Pos, tok token.Token, nativeNode string) *Node {
	if pos == token.NoPos {
		return nil
	}
	if !(tok.IsOperator() || tok.IsKeyword()) {
		if t.paranoiac {
			offset := t.file.Offset(pos)
			location := t.location(offset, offset)
			panic(fmt.Sprintf("Unsupported token '%s'%s", tok.String(), location))
		}
		return nil
	}

	return t.createUastExpectedToken(pos, tok.String(), nativeNode)
}

func (t *UastMapper) createUastExpectedToken(pos token.Pos, expectedValue string, nativeNode string) *Node {
	if pos == token.NoPos {
		return nil
	}
	offset := t.file.Offset(pos)
	var endOffset int
	endOffset, expectedValue = t.computeEndOffsetSupportingMultiLineToken(offset, expectedValue)
	node := t.createToken(offset, endOffset, nativeNode)
	if node != nil && node.Token.Value != expectedValue {
		if t.paranoiac {
			location := t.location(offset, endOffset)
			panic(fmt.Sprintf("Invalid token value '%s' instead of '%s'%s",
				node.Token.Value, expectedValue, location))
		}
		return nil
	}
	return node
}

func (t *UastMapper) computeEndOffsetSupportingMultiLineToken(offset int, value string) (int, string) {
	length := len(value)
	endOffset := offset + length
	if offset < 0 || !t.hasCarriageReturn {
		return endOffset, value
	}
	contentLength := len(t.fileContent)
	// computedEndOffset will be equal to offset + len(value) + <computed number of \r characters>
	computedEndOffset := offset
	for length > 0 && computedEndOffset < contentLength {
		if t.fileContent[computedEndOffset] != '\r' {
			length--
		}
		computedEndOffset++
	}
	if computedEndOffset != endOffset {
		return computedEndOffset, t.fileContent[offset:computedEndOffset]
	}
	return endOffset, value
}

func (t *UastMapper) toPosition(offset int) token.Position {
	position := t.file.Position(t.file.Pos(offset))
	if t.paranoiac && !position.IsValid() {
		panic("Invalid offset" + t.location(offset, offset))
	}
	return position
}

func (t *UastMapper) appendNode(children []*Node, child *Node) []*Node {
	if child == nil {
		return children
	}
	// Comments are not appended before the first child. They will be appended by an
	// ancestor node before a non first child (except for the "package" node, it's the
	// very first node, it has his specific logic to append header comments)
	if len(children) > 0 {
		lastChild := children[len(children)-1]
		children = t.appendCommentOrMissingToken(children, lastChild.endOffset, child.offset)
		if t.paranoiac && children[len(children)-1].endOffset > child.offset {
			panic("Invalid token sequence" + t.location(children[len(children)-1].endOffset, child.offset))
		}
	}
	return t.appendNodeCheckOrder(children, child)
}

func (t *UastMapper) appendCommentOrMissingToken(children []*Node, offset, endOffset int) []*Node {
	if len(t.comments) == 0 {
		return t.appendMissingToken(children, offset, endOffset)
	}
	// when a child append a comment, it move the 'commentPos' forward, so the parent has to rewind
	for t.commentPos > 0 && t.comments[t.commentPos-1].offset >= offset {
		t.commentPos--
	}

	for t.commentPos < len(t.comments) {
		commentNode := t.comments[t.commentPos]
		if commentNode.offset >= offset {
			if commentNode.endOffset <= endOffset {
				children = t.appendMissingToken(children, offset, commentNode.offset)
				children = t.appendNodeCheckOrder(children, commentNode)
				offset = commentNode.endOffset
			} else {
				break
			}
		}
		t.commentPos++
	}
	return t.appendMissingToken(children, offset, endOffset)
}

func (t *UastMapper) appendNodeCheckOrder(parentList []*Node, child *Node) []*Node {
	if child == nil {
		return parentList
	}
	if len(parentList) > 0 {
		lastChild := parentList[len(parentList)-1]
		if t.paranoiac && lastChild.endOffset > child.offset {
			panic("Invalid token sequence" + t.location(lastChild.endOffset, child.offset))
		}
	}
	return append(parentList, child)
}

var missingKeywordToken = map[byte]string{
	',': ",", ';': ";", '.': ".", '[': "[", ']': "]", '=': "=", ':': ":",
	't': "type", 'r': "range", 'e': "else", 'c': "chan", '<': "<-"}

func (t *UastMapper) appendMissingToken(children []*Node, offset, endOffset int) []*Node {
	if offset < 0 || endOffset < offset || endOffset > len(t.fileContent) {
		return nil
	}
	for offset < endOffset && t.fileContent[offset] <= ' ' {
		offset++
	}
	for endOffset > offset && t.fileContent[endOffset-1] <= ' ' {
		endOffset--
	}
	for offset < endOffset {
		missingTokenValue := missingKeywordToken[t.fileContent[offset]]
		tokenLength := len(missingTokenValue)
		if tokenLength == 0 || t.fileContent[offset:offset+tokenLength] != missingTokenValue {
			if t.paranoiac {
				location := t.location(offset, endOffset)
				panic(fmt.Sprintf("Invalid missing token '%s'%s", t.fileContent[offset:endOffset], location))
			}
			tokenLength = endOffset - offset
		}
		missingToken := t.createToken(offset, offset+tokenLength, "")
		children = t.appendNodeCheckOrder(children, missingToken)
		offset += tokenLength
		for offset < endOffset && t.fileContent[offset] <= ' ' {
			offset++
		}
	}
	return children
}

func (t *UastMapper) location(offset, endOffset int) string {
	var out bytes.Buffer
	out.WriteString(fmt.Sprintf(" at offset %d:%d for file %s", offset, endOffset, t.file.Name()))
	if 0 <= offset && offset <= t.file.Size() {
		p := t.file.Position(t.file.Pos(offset))
		out.WriteString(fmt.Sprintf(":%d:%d", p.Line, p.Column))
	}
	return out.String()
}

func isEndOfLine(ch byte) bool {
	return ch == '\n' || ch == '\r'
}
