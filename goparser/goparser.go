package main

import (
	"fmt"
	"go/parser"
	"go/token"
	"go/ast"
	"encoding/json"
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
	IF_STMT           Kind = "IF_STMT"
	DECL_LIST         Kind = "DECL_LIST"
	ASSIGNMENT        Kind = "ASSIGNMENT"
	ASSIGNMENT_TARGET Kind = "ASSIGNMENT_TARGET"
	ASSIGNMENT_VALUE  Kind = "ASSIGNMENT_VALUE"
	TOKEN             Kind = "TOKEN"
	IDENTIFIER        Kind = "IDENTIFIER"
	SELECTOR_EXPR     Kind = "SELECTOR_EXPR"
	LITERAL           Kind = "LITERAL"
	EXPR_LIST         Kind = "EXPR_LIST"
	EXPR_STMT         Kind = "EXPR_STMT"
	UNSUPPORTED       Kind = "UNSUPPORTED"
)

type Token struct {
	Value  string `json:"value,omitempty"`
	Line   int    `json:"line"`
	Column int    `json:"column"`
	offset int // TODO remove this
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
		return IF_STMT
	case Kind:
		return v
	default:
		return Kind(nativeNode(k))
	}
}

func kinds(rawItems ... interface{}) []Kind {
	items := make([]Kind, len(rawItems))
	for i, v := range rawItems {
		items[i] = kind(v)
	}
	return items
}

func children(items ... *Node) []*Node {
	return items
}

type NodeList interface {
	At(i int) ast.Node
	Len() int
	NativeNode() string
}

type ExprList []ast.Expr

func (items ExprList) At(i int) ast.Node  { return items[i] }
func (items ExprList) Len() int           { return len(items) }
func (items ExprList) NativeNode() string { return nativeNode([]ast.Expr{}) }

type StmtList []ast.Stmt

func (items StmtList) At(i int) ast.Node  { return items[i] }
func (items StmtList) Len() int           { return len(items) }
func (items StmtList) NativeNode() string { return nativeNode([]ast.Stmt{}) }

type DeclList []ast.Decl

func (items DeclList) At(i int) ast.Node  { return items[i] }
func (items DeclList) Len() int           { return len(items) }
func (items DeclList) NativeNode() string { return nativeNode([]ast.Decl{}) }

func makeNodeFromList(kind Kind, nodeList NodeList) *Node {
	children := children()
	for i := 0; i < nodeList.Len(); i++ {
		if uastNode := mapNode(nodeList.At(i)); uastNode != nil {
			children = append(children, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   children,
		NativeNode: nodeList.NativeNode(),
	}
}

func MapFile(file *ast.File) *Node {
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
	return &Node{
		Kinds:      kinds(funcDecl),
		Children:   children(mapExpr(funcDecl.Name), mapStmt(funcDecl.Body)),
		NativeNode: nativeNode(funcDecl),
	}
}

func mapBlockStmt(blockStmt *ast.BlockStmt) *Node {
	return &Node{
		Kinds:      kinds(blockStmt),
		Children:   children(mapStmtList(kind(blockStmt.List), blockStmt.List)),
		NativeNode: nativeNode(blockStmt),
	}
}

func mapStmtList(kind Kind, stmtList []ast.Stmt) *Node {
	return makeNodeFromList(kind, StmtList(stmtList))
}

func mapStmt(astNode ast.Stmt) *Node {
	switch v := astNode.(type) {
	case *ast.AssignStmt:
		return mapAssignStmt(v)
	case *ast.ExprStmt:
		return mapExprStmt(v)
	case *ast.IfStmt:
		return mapIfStmt(v)
	case *ast.BlockStmt:
		return mapBlockStmt(v)
	default:
		return mapUnsupported(v)
	}
}

func mapIfStmt(stmt *ast.IfStmt) *Node {
	return &Node{
		Kinds: kinds(kind(stmt)),
		Children: children(
			mapStmt(stmt.Init),
			mapExpr(stmt.Cond),
			mapStmt(stmt.Body),
			mapStmt(stmt.Else),
		),
		NativeNode: nativeNode(stmt),
	}
}

func mapAssignStmt(stmt *ast.AssignStmt) *Node {
	return &Node{
		Kinds: kinds(ASSIGNMENT),
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
		Kinds: kinds(kind(expr)),
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
		Kinds: kinds(kind(expr)),
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
		Kinds:      kinds(SELECTOR_EXPR),
		Children:   children(mapExpr(expr.X), mapIdent(expr.Sel)),
		NativeNode: nativeNode(expr),
	}
}

func mapIdent(ident *ast.Ident) *Node {
	return &Node{
		Kinds:      kinds(IDENTIFIER),
		Token:      mapTokenPos(ident.Name, ident.Pos()),
		NativeNode: nativeNode(ident),
	}
}

func mapBasicLit(lit *ast.BasicLit) *Node {
	return &Node{
		Kinds:      kinds(LITERAL),
		Token:      mapTokenPos(lit.Value, lit.Pos()),
		NativeNode: nativeNode(lit),
	}
}

func mapToken(tok token.Token, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(TOKEN),
		Token:      mapTokenPos(tok.String(), pos),
		NativeNode: nativeNode(tok),
	}
}

func mapLiteralToken(kind Kind, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(kind),
		Token:      mapTokenPos(kind.String(), pos),
	}
}

func mapExprStmt(stmt *ast.ExprStmt) *Node {
	return &Node{
		Kinds:      kinds(EXPR_STMT),
		Children:   children(mapExpr(stmt.X)),
		NativeNode: nativeNode(stmt),
	}
}

func mapCallExpr(callExpr *ast.CallExpr) *Node {
	return &Node{
		Kinds: kinds(CALL),
		Children: children(
			mapExpr(callExpr.Fun),
			mapLiteralToken(LPAREN, callExpr.Lparen),
			mapExprList(ARGS_LIST, callExpr.Args),
			mapLiteralToken(RPAREN, callExpr.Rparen),
		),
	}
}

func mapTokenPos(tok string, pos token.Pos) *Token {
	return &Token{Value: tok, Line: 1, Column: 1, offset: int(pos)}
}

func nativeNode(x interface{}) string {
	return fmt.Sprintf("%T", x)
}

func PrintJson(node *Node) {
	b, err := json.MarshalIndent(node, "", "  ")
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(string(b))
}

func ReadAstFile(filename string) *ast.File {
	fileSet := token.NewFileSet()
	astFile, err := parser.ParseFile(fileSet, filename, nil, parser.ParseComments)
	if err != nil {
		panic(err)
	}
	return astFile
}
