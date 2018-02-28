package main

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
	COMPILATION_UNIT Kind = "COMPILATION_UNIT"
	FUNCTION         Kind = "FUNCTION"
	LPAREN           Kind = "LPAREN"
	RPAREN           Kind = "RPAREN"
	ARGS_LIST        Kind = "ARGS_LIST"
	CALL             Kind = "CALL"
	FUNC_DECL_BODY   Kind = "FUNC_DECL_BODY"
	DECL_LIST        Kind = "DECL_LIST"
	ASSIGNMENT       Kind = "ASSIGNMENT"
	TOKEN            Kind = "TOKEN"
	IDENTIFIER       Kind = "IDENTIFIER"
	SELECTOR_EXPR    Kind = "SELECTOR_EXPR"
	LITERAL          Kind = "LITERAL"
	EXPR_LIST        Kind = "EXPR_LIST"
	EXPR_STMT        Kind = "EXPR_STMT"
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
		return Kind(nativeValue(k))
	}
}

func kinds(rawItems ... interface{}) []Kind {
	items := make([]Kind, len(rawItems))
	for i, v := range rawItems {
		items[i] = kind(v)
	}
	return items
}

func mapFile(file *ast.File) *Node {
	return &Node{
		Kinds:      kinds(file),
		Children:   []*Node{mapDeclList(file.Decls)},
		Position:   mapPos(file.Name.NamePos),
		Value:      file.Name.String(),
		NativeNode: nativeValue(file),
	}
}

func mapDeclList(decls []ast.Decl) *Node {
	uastNodeList := []*Node{}

	for _, astNode := range decls {
		if uastNode := mapDecl(astNode); uastNode != nil {
			uastNodeList = append(uastNodeList, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind(decls)),
		Children:   uastNodeList,
		NativeNode: nativeValue(decls),
	}
}

func mapDecl(decl ast.Decl) *Node {
	switch v := decl.(type) {
	case *ast.FuncDecl:
		return mapFuncDecl(v)
	default:
		return nil
	}
}

func mapFuncDecl(funcDecl *ast.FuncDecl) *Node {
	return &Node{
		Kinds:      kinds(funcDecl),
		Children:   []*Node{mapExpr(funcDecl.Name), mapBlockStmt(funcDecl.Body)},
		NativeNode: nativeValue(funcDecl),
	}
}

func mapBlockStmt(blockStmt *ast.BlockStmt) *Node {
	return &Node{
		Kinds:      kinds(blockStmt),
		Children:   []*Node{makeNodeWithChildren(IDENTIFIER, mapStmt, blockStmt.List)},
		NativeNode: nativeValue(blockStmt),
	}
}

func makeNodeWithChildren(kind Kind, mapper func(astNode ast.Stmt) *Node, stmts []ast.Stmt) *Node {
	children := []*Node{}
	for _, v := range stmts {
		if uastNode := mapper(v); uastNode != nil {
			children = append(children, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   children,
		NativeNode: nativeValue(stmts),
	}
}

func mapStmt(astNode ast.Stmt) *Node {
	switch v := astNode.(type) {
	case *ast.AssignStmt:
		return mapAssignStmt(v)
	case *ast.ExprStmt:
		return mapExprStmt(v)
	default:
		return nil
	}
}

func mapAssignStmt(stmt *ast.AssignStmt) *Node {
	return &Node{
		Kinds:      kinds(ASSIGNMENT),
		Children:   []*Node{mapExprList(EXPR_LIST, stmt.Lhs), mapToken(stmt.Tok, stmt.TokPos), mapExprList(EXPR_LIST, stmt.Rhs)},
		NativeNode: nativeValue(stmt),
	}
}

func mapExprList(kind Kind, exprList []ast.Expr) *Node {
	uastNodeList := []*Node{}

	for _, astNode := range exprList {
		if uastNode := mapExpr(astNode); uastNode != nil {
			uastNodeList = append(uastNodeList, uastNode)
		}
	}

	return &Node{
		Kinds:      kinds(kind),
		Children:   uastNodeList,
		NativeNode: nativeValue(exprList),
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
	default:
		return nil
	}
}

func mapSelectorExpr(expr *ast.SelectorExpr) *Node {
	return &Node{
		Kinds:      kinds(SELECTOR_EXPR),
		Children:   []*Node{mapExpr(expr.X), mapIdent(expr.Sel)},
		NativeNode: nativeValue(expr),
	}
}

func mapIdent(ident *ast.Ident) *Node {
	return &Node{
		Kinds:      kinds(IDENTIFIER),
		Position:   mapPos(ident.NamePos),
		Value:      ident.Name,
		NativeNode: nativeValue(ident),
	}
}

func mapBasicLit(lit *ast.BasicLit) *Node {
	return &Node{
		Kinds:      kinds(LITERAL),
		Position:   mapPos(lit.ValuePos),
		Value:      lit.Value,
		NativeNode: nativeValue(lit),
	}
}

func mapToken(tok token.Token, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(TOKEN),
		Position:   mapPos(pos),
		Value:      tok.String(),
		NativeNode: nativeValue(tok),
	}
}

func mapLiteralToken(kind Kind, pos token.Pos) *Node {
	return &Node{
		Kinds:      kinds(kind),
		Position:   mapPos(pos),
		NativeNode: nativeValue(kind),
	}
}

func mapExprStmt(stmt *ast.ExprStmt) *Node {
	return &Node{
		Kinds:      kinds(EXPR_STMT),
		Children:   []*Node{mapExpr(stmt.X)},
		NativeNode: nativeValue(stmt),
	}
}

func mapCallExpr(callExpr *ast.CallExpr) *Node {
	return &Node{
		Kinds: kinds(CALL),
		Children: []*Node{
			mapExpr(callExpr.Fun),
			mapLiteralToken(LPAREN, callExpr.Lparen),
			mapExprList(ARGS_LIST, callExpr.Args),
			mapLiteralToken(RPAREN, callExpr.Rparen),
		},
	}
}

func mapPos(pos token.Pos) Position {
	return Position{Start: 1, End: 1, offset: int(pos)}
}

func nativeValue(x interface{}) string {
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
