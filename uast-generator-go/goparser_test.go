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
`
	example_with_complete_function = `package main
type id int
func (a id) fun(n1, n2 int, s1 string, b1 bool) (n int, s string) {
	return 1, "x"
}
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
	example_with_forward_declaration := "package main\nfunc forward_declaration() int64"
	fileSet, astFile := astFromString(example_with_forward_declaration)
	funcDecl := astFile.Decls[0].(*ast.FuncDecl)
	uast := mapNode(funcDecl)
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(FUNCTION))
	expectChildrenCount(t, uast, 2)
	expectNativeNode(t, uast, "*ast.FuncDecl")
	expectToken(t, uast, nil)
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

func Test_mapFuncDecl_complete(t *testing.T) {
	fileSet, astFile := astFromString(example_with_complete_function)
	funcDecl := astFile.Decls[1].(*ast.FuncDecl)
	uast := mapNode(funcDecl)
	fixPositions(uast, fileSet)

	expectKinds(t, uast, kinds(FUNCTION))
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "*ast.FuncDecl")

	expectKinds(t, uast.Children[0], kinds(IDENTIFIER))
	expectKinds(t, uast.Children[2], kinds(BLOCK))

	funcType := uast.Children[1]
	expectKinds(t, funcType, kinds(&ast.FuncType{}))
	expectChildrenCount(t, funcType, 2)

	params := funcType.Children[0]
	expectKinds(t, params, kinds(PARAMETER_LIST))
	expectChildrenCount(t, params, 3)
	expectNativeNode(t, params, "*ast.FieldList")

	intParams := params.Children[0]
	expectKinds(t, intParams, kinds(&ast.Field{}))
	expectChildrenCount(t, intParams, 2)
	expectNativeNode(t, intParams, "*ast.Field")

	intParamsNames := intParams.Children[0]
	expectChildrenCount(t, intParamsNames, 2)
	expectNativeNode(t, intParamsNames, "[]*ast.Ident")

	n1 := intParamsNames.Children[0]
	expectChildrenCount(t, n1, 0)
	expectKinds(t, n1, kinds(IDENTIFIER, PARAMETER))
	expectToken(t, n1, &Token{Line: 3, Column: 17, Value: "n1"})

	intParamsType := intParams.Children[1]
	expectKinds(t, intParamsType, kinds(IDENTIFIER))
	expectChildrenCount(t, intParamsType, 0)
	expectNativeNode(t, intParamsType, "*ast.Ident")

	stringParams := params.Children[1]
	expectKinds(t, stringParams, kinds(&ast.Field{}))
	expectChildrenCount(t, stringParams, 2)
	expectNativeNode(t, stringParams, "*ast.Field")

	stringParamsNames := stringParams.Children[0]
	expectChildrenCount(t, stringParamsNames, 1)
	expectNativeNode(t, stringParamsNames, "[]*ast.Ident")

	s1 := stringParamsNames.Children[0]
	expectChildrenCount(t, s1, 0)
	expectKinds(t, s1, kinds(IDENTIFIER, PARAMETER))
	expectToken(t, s1, &Token{Line: 3, Column: 29, Value: "s1"})

	stringParamsType := stringParams.Children[1]
	expectKinds(t, stringParamsType, kinds(IDENTIFIER))
	expectChildrenCount(t, stringParamsType, 0)
	expectNativeNode(t, stringParamsType, "*ast.Ident")

	results := funcType.Children[1]
	expectKinds(t, results, kinds(RESULT_LIST))
	expectChildrenCount(t, results, 2)
	expectNativeNode(t, results, "*ast.FieldList")

	intResults := results.Children[0]
	expectKinds(t, intResults, kinds(&ast.Field{}))
	expectChildrenCount(t, intResults, 2)
	expectNativeNode(t, intResults, "*ast.Field")

	intResultsNames := intResults.Children[0]
	expectChildrenCount(t, intResultsNames, 1)
	expectNativeNode(t, intResultsNames, "[]*ast.Ident")

	n := intResultsNames.Children[0]
	expectChildrenCount(t, n, 0)
	expectKinds(t, n, kinds(IDENTIFIER, RESULT))
	expectToken(t, n, &Token{Line: 3, Column: 50, Value: "n"})

	intResultsType := intResults.Children[1]
	expectKinds(t, intResultsType, kinds(IDENTIFIER))
	expectChildrenCount(t, intResultsType, 0)
	expectNativeNode(t, intResultsType, "*ast.Ident")
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

func Test_mapBinaryOperator(t *testing.T) {
	source := `package main
func main() {
	v1 := true || false
	v2 := true && false
	// rel op
	v3 := 1 == 2
	v4 := 1 != 2
	v5 := 1 < 2
	v6 := 1 <= 2
	v7 := 1 > 2
	v8 := 1 >= 2
	// add_op
	v9 := 1 + 2
	v10 := 1 - 2
	v11 := 1 | 2
	v12 := 1 ^ 2
	// mul_op
	v13 := 1 * 2
	v14 := 1 / 2
	v15 := 1 % 2
	v16 := 1 << 2
	v17 := 1 >> 2
	v18 := 1 & 2
	v19 := 1 &^ 2
}
`
	expectedOperators := []string{"||", "&&", "==", "!=", "<", "<=", ">", ">=", "+", "-", "|", "^", "*", "/", "%", "<<", ">>", "&", "&^"}
	fileSet, astFile := astFromString(source)
	blockStmt := astFile.Decls[0].(*ast.FuncDecl).Body
	for i := 0; i < len(blockStmt.List); i++ {
		binaryExpr := blockStmt.List[i].(*ast.AssignStmt).Rhs[0]
		uast := mapNode(binaryExpr)
		fixPositions(uast, fileSet)

		expectKinds(t, uast, kinds(BINARY_EXPRESSION))
		expectChildrenCount(t, uast, 3)
		expectNativeNode(t, uast, "*ast.BinaryExpr")
		expectEquals(t, uast.Children[1].Token.Value, expectedOperators[i])
	}
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

func expectEquals(t *testing.T, actual string, expected string) {
	if expected != actual {
		t.Fatalf("got '%v'; expected '%v'", actual, expected)
	}
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
		Line:   actual.Token.Line,
		Column: actual.Token.Column,
		Value:  actual.Token.Value,
	}
	if !reflect.DeepEqual(expected, tok) {
		t.Fatalf("got %v; expected %v", tok, expected)
	}
}
