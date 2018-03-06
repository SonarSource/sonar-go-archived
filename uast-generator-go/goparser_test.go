package main

import (
	"go/ast"
	"go/parser"
	"go/token"
	"reflect"
	"testing"
)

const (
	example_hello_world = `package main
import "fmt"
func main() {
	msg := "hello, world"
    fmt.Println(msg)
}`
	example_with_two_assignments = `package main
func main() {
	a, b := 1, 2
	a, b = b, a
}
`
	example_with_if = `package main
func main() {
	a, b := 1, 2
    if a < b {
		a, b = b, a
    }
}
func forward_declaration() int64
`
)

func Test_mapFile(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	uast := toUast(fileSet, astFile)

	expectKinds(t, uast, kinds(COMPILATION_UNIT))
	expectChildrenCount(t, uast, 1)
	expectNativeNode(t, uast, "*ast.File")
	expectToken(t, uast, &Token{Line: 1, Column: 1, Value: "main"})

	expectKinds(t, uast.Children[0], kinds(DECL_LIST))
}

func Test_mapFuncDecl(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	funcDecl := astFile.Decls[1].(*ast.FuncDecl)
	uast := mapNode(funcDecl)
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(FUNCTION))
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "*ast.FuncDecl")
	expectToken(t, uast, nil)
}

func Test_mapFuncDecl_forward_declaration(t *testing.T) {
	funcDecl := astFile.Decls[2].(*ast.FuncDecl)
	uast := mapNode(funcDecl)
	fixPositions(uast, fileSet)

	if expected := kinds(FUNCTION); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 1; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}

	if expected := "*ast.FuncDecl"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapFuncDecl_Name(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	funcDecl := astFile.Decls[1].(*ast.FuncDecl)
	uast := mapNode(funcDecl).Children[0]
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(IDENTIFIER))
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "*ast.Ident")
	expectToken(t, uast, &Token{Line: 3, Column: 6, Value: "main"})
}

func Test_mapBlockStmt(t *testing.T) {
	fileSet, astFile := astFromString(example_with_two_assignments)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt)
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(BLOCK))
	expectChildrenCount(t, uast, 2)
	expectNativeNode(t, uast, "*ast.BlockStmt")
	expectToken(t, uast, nil)
}

func Test_mapAssignStmt(t *testing.T) {
	fileSet, astFile := astFromString(example_with_two_assignments)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt))
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(ASSIGNMENT, STATEMENT))
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "*ast.AssignStmt")
	expectToken(t, uast, nil)
}

func Test_mapExprList(t *testing.T) {
	fileSet, astFile := astFromString(example_with_two_assignments)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	uast := mapExprList(EXPR_LIST, blockStmt.List[0].(*ast.AssignStmt).Lhs)
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(EXPR_LIST))
	expectChildrenCount(t, uast, 2)
	expectNativeNode(t, uast, "[]ast.Expr")
	expectToken(t, uast, nil)
}

func Test_mapExpr_Ident(t *testing.T) {
	fileSet, astFile := astFromString(example_with_two_assignments)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt).Lhs[0])
	fixPositions(uast, fileSet)

	if uast == nil {
		t.Fatalf("got nil; expected an identifier")
	}

	expectKinds(t, uast, kinds(IDENTIFIER))
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "*ast.Ident")
	expectToken(t, uast, &Token{Line: 3, Column: 2, Value: "a"})
}

func Test_mapExpr_BasicLit(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt).Rhs[0])
	fixPositions(uast, fileSet)

	if uast == nil {
		t.Fatalf("got nil; expected a literal")
	}

	expectKinds(t, uast, kinds(LITERAL))
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "*ast.BasicLit")
	expectToken(t, uast, &Token{Line: 4, Column: 9, Value: "\"hello, world\""})
}

func Test_mapExprStmt(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[1].(*ast.ExprStmt))
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(EXPRESSION, STATEMENT))
	expectChildrenCount(t, uast, 1)
	expectNativeNode(t, uast, "*ast.ExprStmt")
	expectToken(t, uast, nil)
}

func Test_mapCallExpr(t *testing.T) {
	fileSet, astFile := astFromString(example_hello_world)
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[1].(*ast.ExprStmt).X.(*ast.CallExpr))
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(CALL))
	expectChildrenCount(t, uast, 4)
	expectNativeNode(t, uast, "")
	expectToken(t, uast, nil)
}

func Test_mapIfStmt(t *testing.T) {
	fileSet, astFile := astFromString(example_with_if)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[1].(*ast.IfStmt))
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(IF_STMT, STATEMENT))
	expectChildrenCount(t, uast, 4)
	expectNativeNode(t, uast, "*ast.IfStmt")
	expectToken(t, uast, nil)
}

func astFromString(source string) (*token.FileSet, *ast.File) {
	fileSet := token.NewFileSet()
	sourceFileName := "main.go"
	astFile, err := parser.ParseFile(fileSet, sourceFileName, source, parser.ParseComments)
	if err != nil {
		panic(err)
	}
	return fileSet, astFile
}

func expectKinds(t *testing.T, actual *Node, expected []Kind) {
	if !reflect.DeepEqual(expected, actual.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", actual.Kinds, expected)
	}
}

func expectChildrenCount(t *testing.T, actual *Node, expected int) {
	if expected != len(actual.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(actual.Children), expected)
	}
}

func expectNativeNode(t *testing.T, actual *Node, expected string) {
	if expected != actual.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", actual.NativeNode, expected)
	}
}

func expectToken(t *testing.T, actual *Node, expected *Token) {
	if actual.Token == nil && expected == nil {
		return
	}

	if actual.Token == nil {
		t.Fatalf("got nil Token; expected %v", expected)
	}

	if expected == nil {
		t.Fatalf("got %v; expected nil Token", actual.Token)
	}

	// copy only fields we want to compare
	tok := &Token{
		Line: actual.Token.Line,
		Column: actual.Token.Column,
		Value: actual.Token.Value,
	}
	if !reflect.DeepEqual(expected, tok) {
		t.Fatalf("got %v; expected %v", tok, expected)
	}
}
