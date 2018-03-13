package main

//go:generate go run generate_source.go

import (
	"fmt"
	"go/ast"
	"go/parser"
	"go/token"
	"io"
	"os"
)

type Kind string

func (k Kind) String() string {
	return string(k)
}

const (
	COMPILATION_UNIT  Kind = "COMPILATION_UNIT"
	FUNCTION          Kind = "FUNCTION"
	BLOCK             Kind = "BLOCK"
	LPAREN            Kind = "LPAREN"
	RPAREN            Kind = "RPAREN"
	ARGS_LIST         Kind = "ARGS_LIST"
	CALL              Kind = "CALL"
	IF                Kind = "IF"
	ELSE              Kind = "ELSE"
	CONDITION         Kind = "CONDITION"
	DECL_LIST         Kind = "DECL_LIST"
	STATEMENT         Kind = "STATEMENT"
	ASSIGNMENT        Kind = "ASSIGNMENT"
	ASSIGNMENT_TARGET Kind = "ASSIGNMENT_TARGET"
	ASSIGNMENT_VALUE  Kind = "ASSIGNMENT_VALUE"
	TOKEN             Kind = "TOKEN"
	IDENTIFIER        Kind = "IDENTIFIER"
	SELECTOR_EXPR     Kind = "SELECTOR_EXPR"
	LITERAL           Kind = "LITERAL"
	EXPR_LIST         Kind = "EXPR_LIST"
	EXPRESSION        Kind = "EXPRESSION"
	PARAMETER_LIST    Kind = "PARAMETER_LIST"
	PARAMETER         Kind = "PARAMETER"
	RESULT_LIST       Kind = "RESULT_LIST"
	RESULT            Kind = "RESULT"
	BINARY_EXPRESSION Kind = "BINARY_EXPRESSION"
	SWITCH            Kind = "SWITCH"
	CASE              Kind = "CASE"
	UNSUPPORTED       Kind = "UNSUPPORTED"
)

type Token struct {
	Value  string    `json:"value,omitempty"`
	Line   int       `json:"line"`
	Column int       `json:"column"`
	pos    token.Pos // TODO remove this
}

type Node struct {
	Kinds      []Kind  `json:"kinds"`
	Token      *Token  `json:"token,omitempty"`
	NativeNode string  `json:"nativeNode,omitempty"`
	Children   []*Node `json:"children,omitempty"`
}

func kind(k interface{}) Kind {
	switch v := k.(type) {
	case *ast.File:
		return COMPILATION_UNIT
	case *ast.FuncDecl:
		return FUNCTION
	case []ast.Decl:
		return DECL_LIST
	case *ast.BlockStmt:
		return BLOCK
	case *ast.IfStmt:
		return IF
	case *ast.Ident:
		return IDENTIFIER
	case *ast.AssignStmt:
		return ASSIGNMENT
	case *ast.BasicLit:
		return LITERAL
	case *ast.ExprStmt:
		return EXPRESSION
	case *ast.BinaryExpr:
		return BINARY_EXPRESSION
	case *ast.CallExpr:
		return CALL
	case *ast.SelectorExpr:
		return SELECTOR_EXPR
	case *ast.SwitchStmt:
		return SWITCH
	case *ast.CaseClause:
		return CASE
	case token.Token:
		return TOKEN
	case Kind:
		return v
	default:
		return Kind(nativeNode(k))
	}
}

func kinds(rawItems ...interface{}) []Kind {
	items := make([]Kind, len(rawItems))
	for i, v := range rawItems {
		items[i] = kind(v)
	}
	return items
}

func children(items ...*Node) []*Node {
	return items
}

type NodeList interface {
	At(i int) ast.Node
	Len() int
	NativeNode() string
}

func childrenFromNodeList(nodeList NodeList) []*Node {
	children := children()
	for i := 0; i < nodeList.Len(); i++ {
		if uastNode := mapNode(nodeList.At(i)); uastNode != nil {
			children = append(children, uastNode)
		}
	}
	return children
}

func makeNodeFromList(kind Kind, nodeList NodeList) *Node {
	return &Node{
		Kinds:      kinds(kind),
		Children:   childrenFromNodeList(nodeList),
		NativeNode: nodeList.NativeNode(),
	}
}

func toUast(fileSet *token.FileSet, astFile *ast.File) *Node {
	node := mapFile(astFile)
	fixPositions(node, fileSet)
	return node
}

func mapFile(file *ast.File) *Node {
	return &Node{
		Kinds:      kinds(file),
		Children:   children(mapDeclList(kind(file.Decls), file.Decls)),
		Token:      mapTokenPos(file.Name.Name, file.Pos()),
		NativeNode: nativeNode(file),
	}
}

func mapDeclList(kind Kind, declList []ast.Decl) *Node {
	return makeNodeFromList(kind, DeclList(declList))
}

func mapDecl(decl ast.Decl) *Node {
	switch v := decl.(type) {
	case *ast.FuncDecl:
		return mapFuncDecl(v)
	default:
		return mapUnsupported(v)
	}
}

func mapFuncDecl(funcDecl *ast.FuncDecl) *Node {
	children := children(mapExpr(funcDecl.Name), mapFuncType(funcDecl.Type))
	if funcDecl.Body != nil {
		children = append(children, mapStmt(funcDecl.Body))
	}
	return &Node{
		Kinds:      kinds(funcDecl),
		Children:   children,
		NativeNode: nativeNode(funcDecl),
	}
}

func mapFuncType(funcType *ast.FuncType) *Node {
	return &Node{
		Kinds:      kinds(funcType),
		Children:   children(mapParamsList(funcType.Params), mapResultsList(funcType.Results)),
		NativeNode: nativeNode(funcType),
	}
}

func mapParamsList(fields *ast.FieldList) *Node {
	return mapFieldList(PARAMETER_LIST, PARAMETER, fields)
}

func mapResultsList(fields *ast.FieldList) *Node {
	return mapFieldList(RESULT_LIST, RESULT, fields)
}

func mapFieldList(listKind, itemKind Kind, fieldList *ast.FieldList) *Node {
	node := &Node{
		Kinds:      kinds(listKind),
		NativeNode: nativeNode(fieldList),
	}
	if fieldList != nil {
		node.Children = childrenFromNodeList(FieldList(fieldList.List))
		for _, field := range node.Children {
			// see mapField: the first child of a field has the names as children
			for _, child := range field.Children[0].Children {
				child.Kinds = append(child.Kinds, itemKind)
			}
		}
	}
	return node
}

func mapField(field *ast.Field) *Node {
	return &Node{
		Kinds:      kinds(field),
		Children:   children(mapIdentList(field.Names), mapExpr(field.Type)),
		NativeNode: nativeNode(field),
	}
}

func mapIdentList(idents []*ast.Ident) *Node {
	return makeNodeFromList(kind(idents), IdentList(idents))
}

func mapBlockStmt(blockStmt *ast.BlockStmt) *Node {
	return &Node{
		Kinds:      kinds(blockStmt),
		Children:   childrenFromNodeList(StmtList(blockStmt.List)),
		NativeNode: nativeNode(blockStmt),
	}
}

func mapStmt(astNode ast.Stmt) *Node {
	var node *Node
	switch v := astNode.(type) {
	case *ast.AssignStmt:
		node = mapAssignStmt(v)
	case *ast.ExprStmt:
		node = mapExprStmt(v)
	case *ast.IfStmt:
		node = mapIfStmt(v)
	case *ast.BlockStmt:
		return mapBlockStmt(v)
	case *ast.SwitchStmt:
		return mapSwitchStmt(v)
	case *ast.CaseClause:
		return mapCaseClause(v)
	default:
		return mapUnsupported(v)
	}

	node.Kinds = append(node.Kinds, STATEMENT)

	return node
}

func mapCaseClause(clause *ast.CaseClause) *Node {
	children := []*Node{mapToken(token.CASE, clause.Case)}
	for _, cond := range clause.List {
		uastCond := mapExpr(cond)
		uastCond.Kinds = append(uastCond.Kinds, CONDITION)
		children = append(children, uastCond)
	}
	children = append(children, mapToken(token.COLON, clause.Colon))
	children = append(children, childrenFromNodeList(StmtList(clause.Body))...)
	return &Node{
		Kinds:    kinds(clause),
		Children: children,
	}
}

func mapSwitchStmt(stmt *ast.SwitchStmt) *Node {
	children := []*Node{mapToken(token.SWITCH, stmt.Switch)}
	if stmt.Init != nil {
		children = append(children, mapStmt(stmt.Init))
	}
	children = append(children, mapExpr(stmt.Tag))
	// case clauses are direct children of switch statement, body is skipped
	children = append(children, mapStmt(stmt.Body).Children...)
	return &Node{
		Kinds:    kinds(stmt),
		Children: children,
	}
}

func mapIfStmt(stmt *ast.IfStmt) *Node {
	var conditionChildren []*Node
	if stmt.Init != nil {
		conditionChildren = children(mapStmt(stmt.Init), mapExpr(stmt.Cond))
	} else {
		conditionChildren = children(mapExpr(stmt.Cond))
	}
	condition := &Node{
		Kinds:    kinds(CONDITION),
		Children: conditionChildren,
	}
	elseStmt := mapStmt(stmt.Else)
	elseStmt.Kinds = append(elseStmt.Kinds, ELSE)
	return &Node{
		Kinds: kinds(stmt),
		Children: children(
			condition,
			mapStmt(stmt.Body),
			elseStmt,
		),
		NativeNode: nativeNode(stmt),
	}
}

func mapAssignStmt(stmt *ast.AssignStmt) *Node {
	return &Node{
		Kinds: kinds(stmt),
		Children: children(
			mapExprList(ASSIGNMENT_TARGET, stmt.Lhs),
			mapToken(stmt.Tok, stmt.TokPos),
			mapExprList(ASSIGNMENT_VALUE, stmt.Rhs),
		),
		NativeNode: nativeNode(stmt),
	}
}

func mapExprList(kind Kind, exprList []ast.Expr) *Node {
	return makeNodeFromList(kind, ExprList(exprList))
}

func mapNode(astNode ast.Node) *Node {
	switch v := astNode.(type) {
	case ast.Expr:
		return mapExpr(v)
	case ast.Stmt:
		return mapStmt(v)
	case ast.Decl:
		return mapDecl(v)
	case *ast.Field:
		return mapField(v)
	default:
		return mapUnsupported(astNode)
	}
}

func mapUnsupported(node ast.Node) *Node {
	return &Node{
		Kinds:      kinds(UNSUPPORTED),
		Children:   children(),
		NativeNode: nativeNode(node),
	}
}

func mapExpr(astNode ast.Expr) *Node {
	switch v := astNode.(type) {
	case *ast.Ident:
		return mapIdent(v)
	case *ast.BasicLit:
		return mapBasicLit(v)
	case *ast.SelectorExpr:
		return mapSelectorExpr(v)
	case *ast.CallExpr:
		return mapCallExpr(v)
	case *ast.ParenExpr:
		return mapParenExpr(v)
	case *ast.BinaryExpr:
		return mapBinaryExpr(v)
	default:
		return mapUnsupported(v)
	}
}

func mapBinaryExpr(expr *ast.BinaryExpr) *Node {
	return &Node{
		Kinds: kinds(expr),
		Children: children(
			mapExpr(expr.X),
			mapToken(expr.Op, expr.OpPos),
			mapExpr(expr.Y),
		),
		NativeNode: nativeNode(expr),
	}
}

func mapParenExpr(expr *ast.ParenExpr) *Node {
	return &Node{
		Kinds: kinds(expr),
		Children: children(
			mapLiteralToken(LPAREN, expr.Lparen),
			mapExpr(expr.X),
			mapLiteralToken(RPAREN, expr.Rparen),
		),
		NativeNode: nativeNode(expr),
	}
}

func mapSelectorExpr(expr *ast.SelectorExpr) *Node {
	return &Node{
		Kinds:      kinds(expr),
		Children:   children(mapExpr(expr.X), mapIdent(expr.Sel)),
		NativeNode: nativeNode(expr),
	}
}

func mapIdent(ident *ast.Ident) *Node {
	return &Node{
		Kinds:      kinds(ident),
		Token:      mapTokenPos(ident.Name, ident.Pos()),
		NativeNode: nativeNode(ident),
	}
}

func mapBasicLit(lit *ast.BasicLit) *Node {
	return &Node{
		Kinds:      kinds(lit),
		Token:      mapTokenPos(lit.Value, lit.Pos()),
		NativeNode: nativeNode(lit),
	}
}

func mapToken(tok token.Token, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(tok),
		Token:      mapTokenPos(tok.String(), pos),
		NativeNode: nativeNode(tok),
	}
}

func mapLiteralToken(kind Kind, pos token.Pos) *Node {
	return &Node{
		Kinds: kinds(kind),
		Token: mapTokenPos(kind.String(), pos),
	}
}

func mapExprStmt(stmt *ast.ExprStmt) *Node {
	return &Node{
		Kinds:      kinds(stmt),
		Children:   children(mapExpr(stmt.X)),
		NativeNode: nativeNode(stmt),
	}
}

func mapCallExpr(callExpr *ast.CallExpr) *Node {
	return &Node{
		Kinds: kinds(callExpr),
		Children: children(
			mapExpr(callExpr.Fun),
			mapLiteralToken(LPAREN, callExpr.Lparen),
			mapExprList(ARGS_LIST, callExpr.Args),
			mapLiteralToken(RPAREN, callExpr.Rparen),
		),
	}
}

func mapTokenPos(tok string, pos token.Pos) *Token {
	return &Token{Value: tok, Line: 1, Column: 1, pos: pos}
}

func nativeNode(x interface{}) string {
	return fmt.Sprintf("%T", x)
}

func PrintJson(node *Node) {
	fmt.Println(toJson(node))
}

func readAstFile(filename string) (*token.FileSet, *ast.File) {
	fileSet := token.NewFileSet()
	var src io.Reader = nil
	if filename == "-" {
		src = os.Stdin
	}
	astFile, err := parser.ParseFile(fileSet, filename, src, parser.ParseComments)
	if err != nil {
		panic(err)
	}
	return fileSet, astFile
}

func fixPositions(node *Node, fileSet *token.FileSet) {
	if node.Token != nil {
		position := fileSet.Position(node.Token.pos)
		node.Token.Line = position.Line
		node.Token.Column = position.Column
	}
	for _, child := range node.Children {
		fixPositions(child, fileSet)
	}
}
