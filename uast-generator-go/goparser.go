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
	"unicode/utf8"
)

type Kind string

func (k Kind) String() string {
	return string(k)
}

const (
	COMPILATION_UNIT    Kind = "COMPILATION_UNIT"
	COMMENT             Kind = "COMMENT"
	STRUCTURED_COMMENT  Kind = "STRUCTURED_COMMENT"
	PACKAGE             Kind = "PACKAGE"
	EOF                 Kind = "EOF"
	FUNCTION            Kind = "FUNCTION"
	BLOCK               Kind = "BLOCK"
	LPAREN              Kind = "LPAREN"
	RPAREN              Kind = "RPAREN"
	ARGS_LIST           Kind = "ARGS_LIST"
	CALL                Kind = "CALL"
	IF                  Kind = "IF"
	ELSE                Kind = "ELSE"
	CONDITION           Kind = "CONDITION"
	DECLARATION         Kind = "DECLARATION"
	DECL_LIST           Kind = "DECL_LIST"
	CLASS               Kind = "CLASS"
	STATEMENT           Kind = "STATEMENT"
	ASSIGNMENT          Kind = "ASSIGNMENT"
	COMPOUND_ASSIGNMENT Kind = "COMPOUND_ASSIGNMENT"
	ASSIGNMENT_TARGET   Kind = "ASSIGNMENT_TARGET"
	ASSIGNMENT_VALUE    Kind = "ASSIGNMENT_VALUE"
	IDENTIFIER          Kind = "IDENTIFIER"
	TYPE                Kind = "TYPE"
	KEYWORD             Kind = "KEYWORD"
	SELECTOR_EXPR       Kind = "SELECTOR_EXPR"
	LITERAL             Kind = "LITERAL"
	STRING_LITERAL      Kind = "STRING_LITERAL"
	EXPRESSION          Kind = "EXPRESSION"
	PARAMETER_LIST      Kind = "PARAMETER_LIST"
	PARAMETER           Kind = "PARAMETER"
	RESULT_LIST         Kind = "RESULT_LIST"
	RESULT              Kind = "RESULT"
	BINARY_EXPRESSION   Kind = "BINARY_EXPRESSION"
	SWITCH              Kind = "SWITCH"
	CASE                Kind = "CASE"
	DEFAULT_CASE        Kind = "DEFAULT_CASE"
	UNSUPPORTED         Kind = "UNSUPPORTED"
)

type Token struct {
	Value  string `json:"value,omitempty"`
	Line   int    `json:"line"`
	Column int    `json:"column"`
}

type Node struct {
	Kinds      []Kind  `json:"kinds,omitempty"`
	Token      *Token  `json:"token,omitempty"`
	NativeNode string  `json:"nativeNode,omitempty"`
	Children   []*Node `json:"children,omitempty"`
	// internal fields
	offset    int // position of first character belonging to the node
	endOffset int // position of first character immediately after the node
}

func toUast(fileSet *token.FileSet, astFile *ast.File, fileContent string) *Node {
	return NewUastMapper(fileSet, astFile, fileContent).toUast()
}

func PrintJson(node *Node) {
	fmt.Println(toJson(node))
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
	astFile     *ast.File
	fileContent string
	file        *token.File
	comments    []*Node
	commentPos  int
	paranoiac   bool
}

func NewUastMapper(fileSet *token.FileSet, astFile *ast.File, fileContent string) *UastMapper {
	t := &UastMapper{
		astFile:     astFile,
		fileContent: fileContent,
		file:        fileSet.File(astFile.Pos()),
		paranoiac:   true,
	}
	t.comments = t.mapAllComments()
	t.commentPos = 0
	return t
}

func (t *UastMapper) toUast() *Node {
	compilationUnit := t.mapFile(t.astFile, nil, "")
	t.addEof(compilationUnit)
	if t.paranoiac && (compilationUnit.offset < 0 || compilationUnit.endOffset > len(t.fileContent)) {
		panic("Unexpected compilationUnit" + t.location(compilationUnit.offset, compilationUnit.endOffset))
	}
	return compilationUnit
}

func (t *UastMapper) addEof(compilationUnit *Node) {
	offset := len(t.fileContent)
	eofNode := t.createUastToken([]Kind{EOF}, offset, offset, "")
	compilationUnit.Children = t.appendNode(compilationUnit.Children, eofNode)
}

func (t *UastMapper) mapAllComments() []*Node {
	var list []*Node
	for _, commentGroup := range t.astFile.Comments {
		for _, comment := range commentGroup.List {
			var kinds []Kind
			if len(comment.Text) >= 2 && comment.Text[1] == '/' {
				kinds = []Kind{COMMENT}
			} else {
				kinds = []Kind{COMMENT, STRUCTURED_COMMENT}
			}
			node := t.createUastExpectedToken(kinds, comment.Pos(), comment.Text, "")
			list = append(list, node)
		}
	}
	return list
}

func (t *UastMapper) mapPackageDecl(file *ast.File) *Node {
	var children []*Node
	// "package" node is the very first node, header comments are appended before
	packageNode := t.createUastExpectedToken([]Kind{"KEYWORD"}, file.Package, token.PACKAGE.String(), "")
	if packageNode != nil {
		children = t.appendCommentOrMissingToken(children, 0, packageNode.offset)
		children = append(children, packageNode)
	}
	children = t.appendNode(children, t.mapIdent(file.Name, nil, "Name"))
	return t.createUastNode([]Kind{PACKAGE}, nil, children, "File.Package")
}

func (t *UastMapper) computeBasicLitKinds(tok token.Token) []Kind {
	if tok == token.STRING || tok == token.CHAR {
		return []Kind{LITERAL, STRING_LITERAL}
	}
	return []Kind{LITERAL}
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

func (t *UastMapper) mergeNode(children []*Node, kinds []Kind, child *Node) ([]*Node, []Kind) {
	if child != nil {
		kinds = append(kinds, child.Kinds...)
		for _, grandchild := range child.Children {
			children = t.appendNode(children, grandchild)
		}
	}
	return children, kinds
}

func (t *UastMapper) createAdditionalInitAndCond(astInit ast.Stmt, astCond ast.Expr) *Node {
	var children []*Node
	children = t.appendNode(children, t.mapStmt(astInit, nil, "Init"))
	children = t.appendNode(children, t.mapExpr(astCond, nil, "Cond"))
	return t.createUastNode([]Kind{CONDITION}, nil, children, "InitAndCond")
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

func (t *UastMapper) appendNodeList(parentList []*Node, children []*Node, kinds []Kind, nativeNode string) []*Node {
	// TODO provide the next Token offset, so the last separator can be part of the children
	return t.appendNode(parentList, t.createUastNode(kinds, nil, children, nativeNode))
}

func (t *UastMapper) createUastNode(kinds []Kind, astNode ast.Node, children []*Node, nativeNode string) *Node {
	if len(children) > 0 {
		return &Node{
			Kinds:      kinds,
			Children:   children,
			NativeNode: nativeNode,
			offset:     children[0].offset,
			endOffset:  children[len(children)-1].endOffset,
		}
	} else if astNode != nil {
		offset := t.file.Offset(astNode.Pos())
		endOffset := t.file.Offset(astNode.End())
		return t.createUastToken(kinds, offset, endOffset, nativeNode)
	} else {
		return nil
	}
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
		tokenLength := len(missingKeywordToken[t.fileContent[offset]])
		if tokenLength == 0 {
			if t.paranoiac {
				location := t.location(offset, endOffset)
				panic(fmt.Sprintf("Invalid missing token '%s'%s", t.fileContent[offset:endOffset], location))
			}
			tokenLength = endOffset - offset
		}
		missingToken := t.createUastToken(nil, offset, offset+tokenLength, "")
		children = t.appendNodeCheckOrder(children, missingToken)
		offset += tokenLength
		for offset < endOffset && t.fileContent[offset] <= ' ' {
			offset++
		}
	}
	return children
}

func (t *UastMapper) createUastTokenFromPosAstToken(kinds []Kind, pos token.Pos, tok token.Token, nativeNode string) *Node {
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
	if tok.IsKeyword() {
		kinds = append(kinds, KEYWORD)
	}
	return t.createUastExpectedToken(kinds, pos, tok.String(), nativeNode)
}

func (t *UastMapper) handleSwitchCase(casePos token.Pos, isDefault bool, children []*Node, kinds []Kind) ([]*Node, []Kind) {
	tok := token.CASE
	if isDefault {
		tok = token.DEFAULT
		kinds = append(kinds, DEFAULT_CASE)
	}
	children = t.appendNode(children, t.createUastTokenFromPosAstToken(nil, casePos, tok, "Case"))
	return children, kinds
}

func (t *UastMapper) createUastExpectedToken(kinds []Kind, pos token.Pos, expectedValue string, nativeNode string) *Node {
	if pos == token.NoPos {
		return nil
	}
	offset := t.file.Offset(pos)
	endOffset := offset + len(expectedValue)
	node := t.createUastToken(kinds, offset, endOffset, nativeNode)
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

func (t *UastMapper) createUastToken(kinds []Kind, offset, endOffset int, nativeNode string) *Node {
	if offset < 0 || endOffset < offset || endOffset > len(t.fileContent) {
		location := t.location(offset, endOffset)
		panic("Invalid token" + location)
	}
	if endOffset == offset && !(len(kinds) == 1 && kinds[0] == EOF) {
		if t.paranoiac {
			location := t.location(offset, endOffset)
			panic("Invalid empty token" + location)
		}
		return nil
	}
	position := t.toPosition(offset)
	if !position.IsValid() {
		if t.paranoiac {
			location := t.location(offset, endOffset)
			panic("Invalid token position" + location)
		}
		return nil
	}
	line := position.Line
	lineOffset := offset - position.Column + 1
	column := utf8.RuneCountInString(t.fileContent[lineOffset:offset]) + 1

	if offset > 0 && offset == len(t.fileContent) && isEndOfLine(t.fileContent[offset-1]) {
		line++
		column = 1
	}
	return &Node{
		Kinds: kinds,
		Token: &Token{
			Line:   line,
			Column: column,
			Value:  t.fileContent[offset:endOffset],
		},
		offset:     offset,
		endOffset:  endOffset,
		NativeNode: nativeNode,
	}
}

func (t *UastMapper) toPosition(offset int) token.Position {
	position := t.file.Position(t.file.Pos(offset))
	if t.paranoiac && !position.IsValid() {
		panic("Invalid offset" + t.location(offset, offset))
	}
	return position
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

func (t *UastMapper) computeAssignStmtKinds(tok token.Token) []Kind {
	switch tok {
	case token.DEFINE:
		return []Kind{ASSIGNMENT, DECLARATION}
	case token.ASSIGN:
		return []Kind{ASSIGNMENT}
	default:
		return []Kind{ASSIGNMENT, COMPOUND_ASSIGNMENT}
	}
}
