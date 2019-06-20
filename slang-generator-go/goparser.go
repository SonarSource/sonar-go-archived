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
	"strconv"
	"strings"
	"unicode/utf8"
)

type Token struct {
	Value     string     `json:"text"`
	TextRange *TextRange `json:"textRange"`
	TokenType string     `json:"type"`
}

type Node struct {
	Token    *Token  `json:"-"`
	Children []*Node `json:"-"`
	// internal fields
	offset    int // position of first character belonging to the node
	endOffset int // position of first character immediately after the node
	//Slang fields
	SlangType  string                 `json:"@type"`
	TextRange  *TextRange             `json:"metaData"`
	SlangField map[string]interface{} `json:"slangF"`
}

type TextRange struct {
	StartLine   int
	StartColumn int
	EndLine     int
	EndColumn   int
}

func toSlangTree(fileSet *token.FileSet, astFile *ast.File, fileContent string) (*Node, []*Node, []*Token) {
	return NewSlangMapper(fileSet, astFile, fileContent).toSlang()
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

type SlangMapper struct {
	astFile           *ast.File
	fileContent       string
	hasCarriageReturn bool
	file              *token.File
	comments          []*Node
	commentPos        int
	tokens            []*Token
	paranoiac         bool
}

func NewSlangMapper(fileSet *token.FileSet, astFile *ast.File, fileContent string) *SlangMapper {
	t := &SlangMapper{
		astFile:           astFile,
		fileContent:       fileContent,
		hasCarriageReturn: strings.IndexByte(fileContent, '\r') != -1,
		file:              fileSet.File(astFile.Pos()),
		tokens:            nil,
		paranoiac:         true,
	}
	t.comments = t.mapAllComments()
	t.commentPos = 0
	return t
}

func (t *SlangMapper) toSlang() (*Node, []*Node, []*Token) {
	compilationUnit := t.mapFile(t.astFile, "")

	if t.paranoiac && (compilationUnit.offset < 0 || compilationUnit.endOffset > len(t.fileContent)) {
		panic("Unexpected compilationUnit" + t.location(compilationUnit.offset, compilationUnit.endOffset))
	}
	return compilationUnit, t.comments, t.tokens
}

func (t *SlangMapper) mapAllComments() []*Node {
	var list []*Node
	for _, commentGroup := range t.astFile.Comments {
		for _, comment := range commentGroup.List {
			node := t.createExpectedToken(comment.Pos(), comment.Text, "")
			list = append(list, node)
		}
	}
	return list
}

func (t *SlangMapper) mapPackageDecl(file *ast.File) *Node {
	var children []*Node
	// "package" node is the very first node, header comments are appended before
	packageNode := t.createExpectedToken(file.Package, token.PACKAGE.String(), "")
	if packageNode != nil {
		children = t.appendCommentOrMissingToken(children, 0, packageNode.offset)
		children = append(children, packageNode)
	}
	children = t.appendNode(children, t.mapIdent(file.Name, "Name"))
	return t.createNativeNode(nil, children, "File.Package")
}

func (t *SlangMapper) mapBasicLit(astNode *ast.BasicLit, fieldName string) *Node {
	if astNode == nil {
		return nil
	}

	return t.createExpectedToken(astNode.Pos(), astNode.Value, fieldName+"(BasicLit)")
}

func (t *SlangMapper) appendNode(children []*Node, child *Node) []*Node {
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

func (t *SlangMapper) createAdditionalInitAndCond(astInit ast.Stmt, astCond ast.Expr) *Node {
	var children []*Node
	children = t.appendNode(children, t.mapStmt(astInit, "Init"))
	children = t.appendNode(children, t.mapExpr(astCond, "Cond"))
	return t.createNativeNode(nil, children, "InitAndCond")
}

func (t *SlangMapper) appendCommentOrMissingToken(children []*Node, offset, endOffset int) []*Node {
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

func (t *SlangMapper) appendNodeCheckOrder(parentList []*Node, child *Node) []*Node {
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

func (t *SlangMapper) appendNodeList(parentList []*Node, children []*Node, nativeNode string) []*Node {
	// TODO provide the next Token offset, so the last separator can be part of the children
	return t.appendNode(parentList, t.createNativeNode(nil, children, nativeNode))
}

func (t *SlangMapper) createNativeNode(astNode ast.Node, children []*Node, nativeNode string) *Node {
	slangField := make(map[string]interface{})
	slangField["children"] = children
	slangField["nativeKind"] = nativeNode

	return t.createNode(astNode, children, nativeNode, "Native", slangField)
}

func (t *SlangMapper) createNode(astNode ast.Node, children []*Node, nativeNode, slangType string, slangField map[string]interface{}) *Node {
	if len(children) > 0 {
		return &Node{
			Children:  children,
			offset:    children[0].offset,
			endOffset: children[len(children)-1].endOffset,
			SlangType: slangType,
			TextRange: &TextRange{
				StartLine:   children[0].TextRange.StartLine,
				StartColumn: children[0].TextRange.StartColumn,
				EndLine:     children[len(children)-1].TextRange.EndLine,
				EndColumn:   children[len(children)-1].TextRange.EndColumn,
			},
			SlangField: slangField,
		}
	} else if slangField != nil && astNode != nil {
		offset := t.file.Offset(astNode.Pos())
		endOffset := t.file.Offset(astNode.End())
		return t.createLeafNode(offset, endOffset, nativeNode, slangType, slangField)
	} else if astNode != nil {
		offset := t.file.Offset(astNode.Pos())
		endOffset := t.file.Offset(astNode.End())
		return t.createToken(offset, endOffset, nativeNode)
	} else {
		return nil
	}
}

var missingKeywordToken = map[byte]string{
	',': ",", ';': ";", '.': ".", '[': "[", ']': "]", '=': "=", ':': ":",
	't': "type", 'r': "range", 'e': "else", 'c': "chan", '<': "<-"}

func (t *SlangMapper) appendMissingToken(children []*Node, offset, endOffset int) []*Node {
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

func (t *SlangMapper) createTokenFromPosAstToken(pos token.Pos, tok token.Token, nativeNode string) *Node {
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

	return t.createExpectedToken(pos, tok.String(), nativeNode)
}

func (t *SlangMapper) handleSwitchCase(casePos token.Pos, isDefault bool, children []*Node) []*Node {
	tok := token.CASE
	if isDefault {
		tok = token.DEFAULT
	}
	children = t.appendNode(children, t.createTokenFromPosAstToken(casePos, tok, "Case"))
	return children
}

func (t *SlangMapper) createExpectedToken(pos token.Pos, expectedValue string, nativeNode string) *Node {
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

func (t *SlangMapper) computeEndOffsetSupportingMultiLineToken(offset int, value string) (int, string) {
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

func (t *SlangMapper) createToken(offset, endOffset int, nativeNode string) *Node {
	slangField := make(map[string]interface{})
	slangField["nativeKind"] = nativeNode

	return t.createLeafNode(offset, endOffset, nativeNode, "Native", slangField)
}

func (t *SlangMapper) createLeafNode(offset, endOffset int, nativeNode, slangType string, slangField map[string]interface{}) *Node {
	if offset < 0 || endOffset < offset || endOffset > len(t.fileContent) {
		location := t.location(offset, endOffset)
		panic("Invalid token" + location)
	}
	if endOffset == offset {
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

	slangToken := &Token{
		TextRange: &TextRange{
			StartLine:   startLine,
			StartColumn: startColumn,
			EndLine:     endLine,
			EndColumn:   endColumn,
		},
		Value: t.fileContent[offset:endOffset],
	}

	t.tokens = append(t.tokens, slangToken)

	return &Node{
		Token:     slangToken,
		offset:    offset,
		endOffset: endOffset,
		SlangType: slangType,
		TextRange: &TextRange{
			StartLine:   startLine,
			StartColumn: startColumn,
			EndLine:     endLine,
			EndColumn:   endColumn,
		},
		SlangField: slangField,
	}
}

func (t *SlangMapper) toPosition(offset int) token.Position {
	position := t.file.Position(t.file.Pos(offset))
	if t.paranoiac && !position.IsValid() {
		panic("Invalid offset" + t.location(offset, offset))
	}
	return position
}

func (t *SlangMapper) location(offset, endOffset int) string {
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

func (t *SlangMapper) mapReturnStmt(astNode *ast.ReturnStmt, fieldName string) *Node {
	if astNode == nil {
		return nil
	}
	var children []*Node
	slangField := make(map[string]interface{})
	returnToken := t.createTokenFromPosAstToken(astNode.Return, token.RETURN, "Return")
	slangField["keyword"] = returnToken.Token.TextRange
	children = t.appendNode(children, returnToken)

	if len(astNode.Results) == 0 {
		slangField["body"] = nil
	} else if len(astNode.Results) == 1 {
		body := t.mapExpr(astNode.Results[0], "["+strconv.Itoa(0)+"]")
		slangField["body"] = body
		children = t.appendNode(children, body)
	} else {
		//Slang does not support multiple body, map the whole node to native
		for i := 0; i < len(astNode.Results); i++ {
			children = t.appendNode(children, t.mapExpr(astNode.Results[i], "["+strconv.Itoa(i)+"]"))
		}
		return t.createNativeNode(astNode, children, fieldName+"(ReturnStmt)")
	}

	return t.createNode(astNode, children, fieldName+"(ReturnStmt)", "Return", slangField)
}

func (t *SlangMapper) mapIdent(astNode *ast.Ident, fieldName string) *Node {
	if astNode == nil {
		return nil
	}
	slangField := make(map[string]interface{})
	var slangType string

	switch astNode.Name {
	case "true", "false", "nil":
		slangType = "Litteral"
		slangField["value"] = astNode.Name
	default:
		slangType = "Identifier"
		slangField["name"] = astNode.Name
	}
	var children []*Node
	return t.createNode(astNode, children, fieldName+"(Ident)", slangType, slangField)
}
