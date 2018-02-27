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
	CLASS            Kind = "CLASS"
	FUNCTION         Kind = "FUNCTION"
	BODY             Kind = "BODY"
	COMMENT          Kind = "COMMENT"
	ASSIGNMENT       Kind = "ASSIGNMENT"
	STATEMENT        Kind = "STATEMENT"
	TOKEN            Kind = "TOKEN"
	IDENTIFIER       Kind = "IDENTIFIER"
	EXPR_LIST        Kind = "EXPR_LIST"
)

type Position struct {
	Start  int
	End    int
	Offset int // TODO remove this
}

type Node struct {
	Kinds      []Kind
	Children   []*Node
	Position   Position
	Value      string
	NativeNode string
}

type unknownElement struct {
	value interface{}
}

func (e unknownElement) Error() string {
	return fmt.Sprintf("%v", e.value)
}

func mapFile(file *ast.File) *Node {
	return &Node{
		Kinds:      []Kind{COMPILATION_UNIT},
		Children:   mapDeclList(file.Decls),
		Position:   mapPos(file.Name.NamePos),
		Value:      file.Name.String(),
		NativeNode: nativeValue(file),
	}
}

func mapDeclList(decls []ast.Decl) []*Node {
	uastNodeList := []*Node{}

	for _, astNode := range decls {
		if uastNode, err := mapDecl(astNode); err == nil {
			uastNodeList = append(uastNodeList, uastNode)
		}
	}

	return uastNodeList
}

func mapDecl(decl ast.Decl) (*Node, error) {
	switch v := decl.(type) {
	case *ast.FuncDecl:
		return mapFuncDecl(v), nil
	default:
		return nil, unknownElement{decl}
	}
}

func mapFuncDecl(funcDecl *ast.FuncDecl) *Node {
	return &Node{
		Kinds:      []Kind{FUNCTION},
		Children:   mapBlockStmt(funcDecl.Body),
		Position:   mapPos(funcDecl.Name.NamePos),
		Value:      funcDecl.Name.String(),
		NativeNode: nativeValue(funcDecl),
	}
}

func mapBlockStmt(blockStmt *ast.BlockStmt) []*Node {
	uastNodeList := []*Node{}

	for _, astNode := range blockStmt.List {
		if uastNode, err := mapStmt(astNode); err == nil {
			uastNodeList = append(uastNodeList, uastNode)
		}
	}

	return uastNodeList
}

func mapStmt(astNode ast.Stmt) (*Node, error) {
	switch v := astNode.(type) {
	case *ast.AssignStmt:
		return mapAssignStmt(v), nil
	case *ast.ExprStmt:
		return mapExprStmt(v), nil
	default:
		return nil, unknownElement{astNode}
	}
}

func mapAssignStmt(stmt *ast.AssignStmt) *Node {
	return &Node{
		Kinds:      []Kind{ASSIGNMENT},
		Children:   []*Node{mapExprList(stmt.Lhs), mapToken(stmt.Tok, stmt.TokPos), mapExprList(stmt.Rhs)},
		NativeNode: nativeValue(stmt),
	}
}

func mapExprList(exprList []ast.Expr) *Node {
	uastNodeList := []*Node{}

	for _, astNode := range exprList {
		if uastNode, err := mapExpr(astNode); err == nil {
			uastNodeList = append(uastNodeList, uastNode)
		}
	}

	return &Node{
		Kinds:      []Kind{EXPR_LIST},
		Children:   uastNodeList,
		NativeNode: nativeValue(exprList),
	}
}

func mapExpr(astNode ast.Expr) (*Node, error) {
	switch v := astNode.(type) {
	case *ast.Ident:
		return mapIdent(v), nil
	default:
		return nil, unknownElement{astNode}
	}
}

func mapIdent(ident *ast.Ident) *Node {
	return &Node{
		Kinds:      []Kind{IDENTIFIER},
		Position:   mapPos(ident.NamePos),
		Value:      ident.Name,
		NativeNode: nativeValue(ident),
	}
}

func mapToken(tok token.Token, pos token.Pos) *Node {
	return &Node{
		Kinds:    []Kind{TOKEN},
		Position: mapPos(pos),
		Value:    nativeValue(tok),
	}
}

func mapExprStmt(stmt *ast.ExprStmt) *Node {
	return nil
}

func mapPos(pos token.Pos) Position {
	return Position{Offset: int(pos)}
}

func nativeValue(x interface{}) string {
	return fmt.Sprintf("%T", x)
}

func printJson(node *Node) {
	b, err := json.Marshal(node)
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
	fmt.Println(render.Render(astFile))

	uast := mapFile(astFile)
	printJson(uast)
}
