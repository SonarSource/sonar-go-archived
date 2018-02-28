package goparser

import (
	"fmt"
	"go/parser"
	"go/token"
	"github.com/SonarSource/sonar-go/goparser/test-go/render"
	"go/ast"
	"encoding/json"
)

type Kind string

const (
	COMPILATION_UNIT  Kind = "COMPILATION_UNIT"
	FUNCTION          Kind = "FUNCTION"
	LPAREN            Kind = "LPAREN"
	RPAREN            Kind = "RPAREN"
	ARGS_LIST         Kind = "ARGS_LIST"
	CALL              Kind = "CALL"
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
)

type Position struct {
	Start  int `json:"start"`
	End    int `json:"end"`
	offset int // TODO remove this
}

type Node struct {
	Kinds      []Kind   `json:"kinds"`
	Position   Position `json:"position"`
	Value      string   `json:"value"`
	NativeNode string   `json:"nativeNode"`
	Children   []*Node  `json:"children,omitempty"`
}

func kind(k interface{}) Kind {
	switch v := k.(type) {
	case *ast.File:
		return COMPILATION_UNIT
	case *ast.FuncDecl:
		return FUNCTION
	case []ast.Decl:
		return DECL_LIST
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

func handleUnknownType(o interface{}) {
	switch o.(type) {
	case *ast.GenDecl:
		// ignore
		return
	}
	panic(o)
}

func mapFile(file *ast.File) *Node {
	return &Node{
		Kinds:      kinds(file),
		Children:   children(makeNodeFromDeclList(kind(file.Decls), mapDecl, file.Decls)),
		Position:   mapPos(file.Name.NamePos),
		Value:      file.Name.String(),
		NativeNode: nativeNode(file),
	}
}

func makeNodeFromDeclList(kind Kind, mapper func(decl ast.Decl) *Node, declList []ast.Decl) *Node {
	children := children()
	for _, v := range declList {
		if uastNode := mapper(v); uastNode != nil {
			children = append(children, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   children,
		NativeNode: nativeNode(declList),
	}
}

func mapDecl(decl ast.Decl) *Node {
	switch v := decl.(type) {
	case *ast.FuncDecl:
		return mapFuncDecl(v)
	default:
		handleUnknownType(v)
		return nil
	}
}

func mapFuncDecl(funcDecl *ast.FuncDecl) *Node {
	return &Node{
		Kinds:      kinds(funcDecl),
		Children:   children(mapExpr(funcDecl.Name), mapBlockStmt(funcDecl.Body)),
		NativeNode: nativeNode(funcDecl),
	}
}

func mapBlockStmt(blockStmt *ast.BlockStmt) *Node {
	return &Node{
		Kinds:      kinds(blockStmt),
		Children:   children(makeNodeFromStmtList(kind(blockStmt.List), mapStmt, blockStmt.List)),
		NativeNode: nativeNode(blockStmt),
	}
}

func makeNodeFromStmtList(kind Kind, mapper func(stmt ast.Stmt) *Node, stmtList []ast.Stmt) *Node {
	children := children()
	for _, v := range stmtList {
		if uastNode := mapper(v); uastNode != nil {
			children = append(children, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   children,
		NativeNode: nativeNode(stmtList),
	}
}

func mapStmt(astNode ast.Stmt) *Node {
	switch v := astNode.(type) {
	case *ast.AssignStmt:
		return mapAssignStmt(v)
	case *ast.ExprStmt:
		return mapExprStmt(v)
	default:
		handleUnknownType(v)
		return nil
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
	return makeNodeFromExprList(kind, mapExpr, exprList)
}

func makeNodeFromExprList(kind Kind, mapper func(expr ast.Expr) *Node, exprList []ast.Expr) *Node {
	children := children()
	for _, v := range exprList {
		if uastNode := mapper(v); uastNode != nil {
			children = append(children, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   children,
		NativeNode: nativeNode(exprList),
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
	default:
		handleUnknownType(v)
		return nil
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
		Position:   mapPos(ident.NamePos),
		Value:      ident.Name,
		NativeNode: nativeNode(ident),
	}
}

func mapBasicLit(lit *ast.BasicLit) *Node {
	return &Node{
		Kinds:      kinds(LITERAL),
		Position:   mapPos(lit.ValuePos),
		Value:      lit.Value,
		NativeNode: nativeNode(lit),
	}
}

func mapToken(tok token.Token, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(TOKEN),
		Position:   mapPos(pos),
		Value:      tok.String(),
		NativeNode: nativeNode(tok),
	}
}

func mapLiteralToken(kind Kind, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(kind),
		Position:   mapPos(pos),
		NativeNode: nativeNode(kind),
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

func mapPos(pos token.Pos) Position {
	return Position{Start: 1, End: 1, offset: int(pos)}
}

func nativeNode(x interface{}) string {
	return fmt.Sprintf("%T", x)
}

func printJson(node *Node) {
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

func getSampleAst() *ast.File {
	const sourceContent = `package main
import "fmt"
func main() {
    // This is a comment
    msg := "hello, world\n"
    fmt.Printf( msg )
}
`
	fileSet := token.NewFileSet()
	sourceFileName := "main.go"
	astFile, err := parser.ParseFile(fileSet, sourceFileName, sourceContent, parser.ParseComments)
	if err != nil {
		panic(err)
	}
	return astFile
}

func getSampleUast() *Node {
	return mapFile(getSampleAst())
}

func main() {
	astFile := getSampleAst()
	_ = render.Render(astFile)
	//fmt.Println(render.Render(astFile))

	uast := mapFile(astFile)
	printJson(uast)
}
