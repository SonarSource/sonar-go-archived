package main

import (
	"go/ast"
	"go/parser"
	"go/token"
	"reflect"
	"testing"
)

var fileSet, astFile = getSampleAst()

func getSampleAst() (*token.FileSet, *ast.File) {
	const sourceContent = `package main
import "fmt"
func main() {
    // This is a comment
    msg := "hello, world\n"
    fmt.Printf( msg )
	if (len(msg)) > 0 {
		fmt.Println(msg)
    }
}
`
	fileSet := token.NewFileSet()
	sourceFileName := "main.go"
	astFile, err := parser.ParseFile(fileSet, sourceFileName, sourceContent, parser.ParseComments)
	if err != nil {
		panic(err)
	}
	return fileSet, astFile
}

func Test_mapFile(t *testing.T) {
	uast := toUast(fileSet, astFile)
	if expected := kinds(COMPILATION_UNIT); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 1; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if expected := kinds(DECL_LIST); !reflect.DeepEqual(expected, uast.Children[0].Kinds) {
		t.Fatalf("got %v as kinds of first child; expected %v", uast.Children[0].Kinds, expected)
	}

	if expected := 1; expected != uast.Token.Line {
		t.Fatalf("got %v as Token.Line; expected %v", uast.Token.Line, expected)
	}

	if expected := "main"; expected != uast.Token.Value {
		t.Fatalf("got %v as Value; expected %v", uast.Token.Value, expected)
	}

	if expected := "*ast.File"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapFuncDecl(t *testing.T) {
	funcDecl := astFile.Decls[1].(*ast.FuncDecl)
	uast := mapNode(funcDecl)
	fixPositions(uast, fileSet)

	if expected := kinds(FUNCTION); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 2; expected != len(uast.Children) {
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
	funcDecl := astFile.Decls[1].(*ast.FuncDecl)
	uast := mapNode(funcDecl).Children[0]
	fixPositions(uast, fileSet)

	if expected := kinds(IDENTIFIER); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 0; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if expected := 3; expected != uast.Token.Line {
		t.Fatalf("got %v as Token.Line; expected %v", uast.Token.Line, expected)
	}

	if expected := 6; expected != uast.Token.Column {
		t.Fatalf("got %v as Token.Column; expected %v", uast.Token.Column, expected)
	}

	if expected := "main"; expected != uast.Token.Value {
		t.Fatalf("got %v as Value; expected %v", uast.Token.Value, expected)
	}

	if expected := "*ast.Ident"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapBlockStmt(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt)
	fixPositions(uast, fileSet)

	if expected := kinds(BLOCK); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 3; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}

	if expected := "*ast.BlockStmt"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapAssignStmt(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt))
	fixPositions(uast, fileSet)

	if expected := kinds(ASSIGNMENT, STATEMENT); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 3; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}

	if expected := "*ast.AssignStmt"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapExprList(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapExprList(EXPR_LIST, blockStmt.List[0].(*ast.AssignStmt).Lhs)
	fixPositions(uast, fileSet)

	if expected := kinds(EXPR_LIST); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 1; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}

	if expected := "[]ast.Expr"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapExpr_Ident(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt).Lhs[0])
	fixPositions(uast, fileSet)

	if uast == nil {
		t.Fatalf("got nil; expected an identifier")
	}

	if expected := kinds(IDENTIFIER); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 0; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if expected := 5; expected != uast.Token.Line {
		t.Fatalf("got %v as Token.Line; expected %v", uast.Token.Line, expected)
	}

	if expected := 5; expected != uast.Token.Column {
		t.Fatalf("got %v as Token.Column; expected %v", uast.Token.Column, expected)
	}

	if expected := "msg"; expected != uast.Token.Value {
		t.Fatalf("got %v as Value; expected %v", uast.Token.Value, expected)
	}

	if expected := "*ast.Ident"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapExpr_BasicLit(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[0].(*ast.AssignStmt).Rhs[0])
	fixPositions(uast, fileSet)

	if uast == nil {
		t.Fatalf("got nil; expected a literal")
	}

	if expected := kinds(LITERAL); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 0; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if expected := 5; expected != uast.Token.Line {
		t.Fatalf("got %v as Token.Line; expected %v", uast.Token.Line, expected)
	}

	if expected := 12; expected != uast.Token.Column {
		t.Fatalf("got %v as Token.Column; expected %v", uast.Token.Column, expected)
	}

	if expected := "\"hello, world\\n\""; expected != uast.Token.Value {
		t.Fatalf("got %v as Value; expected %v", uast.Token.Value, expected)
	}

	if expected := "*ast.BasicLit"; expected != uast.NativeNode {
		t.Fatalf("got %v as NativeValue; expected %v", uast.NativeNode, expected)
	}
}

func Test_mapExprStmt(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[1].(*ast.ExprStmt))
	fixPositions(uast, fileSet)

	if expected := kinds(EXPRESSION, STATEMENT); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 1; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}
}

func Test_mapCallExpr(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[1].(*ast.ExprStmt).X.(*ast.CallExpr))
	fixPositions(uast, fileSet)

	if expected := kinds(CALL); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 4; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}
}

func Test_mapIfStmt(t *testing.T) {
	blockStmt := astFile.Decls[1].(*ast.FuncDecl).Body
	uast := mapNode(blockStmt.List[2].(*ast.IfStmt))
	fixPositions(uast, fileSet)

	if expected := kinds(IF_STMT, STATEMENT); !reflect.DeepEqual(expected, uast.Kinds) {
		t.Fatalf("got %v as Kinds; expected %v", uast.Kinds, expected)
	}

	if expected := 4; expected != len(uast.Children) {
		t.Fatalf("got %v as number of Children; expected %v", len(uast.Children), expected)
	}

	if uast.Token != nil {
		t.Fatalf("got %v as Token; expected nil", uast.Token)
	}
}
