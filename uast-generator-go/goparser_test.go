package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"go/ast"
	"go/token"
	"reflect"
	"strings"
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
	uast := uastFromString(t, example_hello_world, "")

	expectKinds(t, uast, []Kind{COMPILATION_UNIT})
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "(File)")
	expectToken(t, uast, nil)

	expectKinds(t, uast.Children[1], []Kind{DECL_LIST})
}

func Test_mapFuncDecl(t *testing.T) {
	uast := uastFromString(t, example_hello_world, "Decls/[1](FuncDecl)")

	expectKinds(t, uast, []Kind{FUNCTION})
	expectChildrenCount(t, uast, 4)
	expectNativeNode(t, uast, "[1](FuncDecl)")
	expectToken(t, uast, nil)
}

func Test_mapFuncDecl_forward_declaration(t *testing.T) {
	uast := uastFromString(t, "package main\nfunc forward_declaration() int64",
		"Decls/[0](FuncDecl)")

	expectKinds(t, uast, []Kind{FUNCTION})
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "[0](FuncDecl)")
	expectToken(t, uast, nil)
}

func Test_mapFuncDecl_Name(t *testing.T) {
	uast := uastFromString(t, example_hello_world, "Decls/[1](FuncDecl)/Name")

	expectKinds(t, uast, []Kind{IDENTIFIER})
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "Name(Ident)")
	expectToken(t, uast, &Token{Line: 3, Column: 6, Value: "main"})
}

func Test_mapFuncDecl_complete(t *testing.T) {
	uast := uastFromString(t, example_with_complete_function,
		"Decls/[1](FuncDecl)")

	expectKinds(t, uast, []Kind{FUNCTION})
	expectChildrenCount(t, uast, 5)
	expectNativeNode(t, uast, "[1](FuncDecl)")

	expectKinds(t, uast.Children[0], []Kind{KEYWORD})
	expectKinds(t, uast.Children[1], []Kind{PARAMETER_LIST})
	expectKinds(t, uast.Children[2], []Kind{IDENTIFIER})
	expectKinds(t, uast.Children[4], []Kind{BLOCK})

	funcType := uast.Children[3]
	expectKinds(t, funcType, nil)
	expectChildrenCount(t, funcType, 2)
	expectNativeNode(t, funcType, "Type(FuncType)")

	params := funcType.Children[0]
	expectKinds(t, params, []Kind{PARAMETER_LIST})
	expectChildrenCount(t, params, 7)
	expectNativeNode(t, params, "Params(FieldList)")

	intParams := params.Children[1]
	expectKinds(t, intParams, nil)
	expectChildrenCount(t, intParams, 2)
	expectNativeNode(t, intParams, "[0](Field)")

	intParamsNames := intParams.Children[0]
	expectChildrenCount(t, intParamsNames, 3)
	expectNativeNode(t, intParamsNames, "Names([]*Ident)")

	n1 := intParamsNames.Children[0]
	expectChildrenCount(t, n1, 0)
	expectKinds(t, n1, []Kind{PARAMETER, IDENTIFIER})
	expectToken(t, n1, &Token{Line: 3, Column: 17, Value: "n1"})

	intParamsType := intParams.Children[1]
	expectKinds(t, intParamsType, []Kind{TYPE, IDENTIFIER})
	expectToken(t, intParamsType, &Token{Line: 3, Column: 24, Value: "int"})
	expectChildrenCount(t, intParamsType, 0)
	expectNativeNode(t, intParamsType, "Type(Ident)")

	stringParams := params.Children[3]
	expectKinds(t, stringParams, nil)
	expectNativeNode(t, stringParams, "[1](Field)")
	expectChildrenCount(t, stringParams, 2)

	stringParamsNames := stringParams.Children[0]
	expectChildrenCount(t, stringParamsNames, 1)
	expectNativeNode(t, stringParamsNames, "Names([]*Ident)")

	s1 := stringParamsNames.Children[0]
	expectChildrenCount(t, s1, 0)
	expectKinds(t, s1, []Kind{PARAMETER, IDENTIFIER})
	expectToken(t, s1, &Token{Line: 3, Column: 29, Value: "s1"})

	stringParamsType := stringParams.Children[1]
	expectKinds(t, stringParamsType, []Kind{TYPE, IDENTIFIER})
	expectToken(t, stringParamsType, &Token{Line: 3, Column: 32, Value: "string"})
	expectChildrenCount(t, stringParamsType, 0)
	expectNativeNode(t, stringParamsType, "Type(Ident)")

	results := funcType.Children[1]
	expectKinds(t, results, []Kind{RESULT_LIST})
	expectChildrenCount(t, results, 5)
	expectNativeNode(t, results, "Results(FieldList)")

	intResults := results.Children[1]
	expectKinds(t, intResults, nil)
	expectChildrenCount(t, intResults, 2)
	expectNativeNode(t, intResults, "[0](Field)")

	intResultsNames := intResults.Children[0]
	expectChildrenCount(t, intResultsNames, 1)
	expectNativeNode(t, intResultsNames, "Names([]*Ident)")

	n := intResultsNames.Children[0]
	expectChildrenCount(t, n, 0)
	expectKinds(t, n, []Kind{RESULT, IDENTIFIER})
	expectToken(t, n, &Token{Line: 3, Column: 50, Value: "n"})

	intResultsType := intResults.Children[1]
	expectKinds(t, intResultsType, []Kind{TYPE, IDENTIFIER})
	expectChildrenCount(t, intResultsType, 0)
	expectNativeNode(t, intResultsType, "Type(Ident)")
}

func Test_mapBlockStmt(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body")

	expectKinds(t, uast, []Kind{BLOCK})
	expectChildrenCount(t, uast, 4)
	expectNativeNode(t, uast, "Body(BlockStmt)")
	expectToken(t, uast, nil)
}

func Test_mapAssignStmt(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)")

	expectKinds(t, uast, []Kind{ASSIGNMENT, STATEMENT})
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "[0](AssignStmt)")
	expectToken(t, uast, nil)
}

func Test_mapExprList(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)/Lhs")

	expectKinds(t, uast, []Kind{ASSIGNMENT_TARGET})
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "Lhs([]Expr)")
	expectToken(t, uast, nil)
}

func Test_mapExpr_Ident(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)/Lhs/[0](Ident)")

	expectKinds(t, uast, []Kind{IDENTIFIER})
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "[0](Ident)")
	expectToken(t, uast, &Token{Line: 3, Column: 2, Value: "a"})
}

func Test_mapExpr_BasicLit(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[0](AssignStmt)/Rhs/[0](BasicLit)")

	expectKinds(t, uast, []Kind{LITERAL, STRING_LITERAL})
	expectChildrenCount(t, uast, 0)
	expectNativeNode(t, uast, "[0](BasicLit)")
	expectToken(t, uast, &Token{Line: 4, Column: 9, Value: "\"hello, world\""})
}

func Test_mapExprStmt(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[1](ExprStmt)")

	expectKinds(t, uast, []Kind{EXPRESSION, STATEMENT})
	expectChildrenCount(t, uast, 1)
	expectNativeNode(t, uast, "[1](ExprStmt)")
	expectToken(t, uast, nil)
}

func Test_mapCallExpr(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[1](ExprStmt)/X(CallExpr)")

	expectKinds(t, uast, []Kind{CALL})
	expectChildrenCount(t, uast, 4)
	expectNativeNode(t, uast, "X(CallExpr)")
	expectToken(t, uast, nil)
}

func Test_mapIfStmt(t *testing.T) {
	uast := uastFromString(t, example_with_if,
		"Decls/[0](FuncDecl)/Body/[1](IfStmt)")

	expectKinds(t, uast, []Kind{IF, STATEMENT})
	expectChildrenCount(t, uast, 3)
	expectNativeNode(t, uast, "[1](IfStmt)")
	expectToken(t, uast, nil)
}

func Test_mapBinaryOperator(t *testing.T) {
	source := `package main
func main() {
	v1 := true || false
	v2 := true && false
	v3 := 1 == 2
	v4 := 1 != 2
	v5 := 1 < 2
	v6 := 1 <= 2
	v7 := 1 > 2
	v8 := 1 >= 2
	v9 := 1 + 2
	v10 := 1 - 2
	v11 := 1 | 2
	v12 := 1 ^ 2
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
	body := uastFromString(t, source,
		"Decls/[0](FuncDecl)/Body")

	expectChildrenCount(t, body, len(expectedOperators)+2)
	// remove the '{' and '}' child
	statements := body.Children[1 : len(body.Children)-1]

	for i, stmt := range statements {
		uast, err := uastQuery(stmt, "Rhs([]Expr)/[0]")
		if err != nil {
			t.Fatalf(err.Error())
		}
		expectKinds(t, uast, []Kind{BINARY_EXPRESSION})
		expectChildrenCount(t, uast, 3)
		expectNativeNode(t, uast, "[0](BinaryExpr)")
		expectEquals(t, uast.Children[1].Token.Value, expectedOperators[i])
	}
}

func Test_mapCaseClause(t *testing.T) {
	source := `package main
func main(i int) {
	switch i {
		case 1,2,3: fmt.Println("a")
		case 3,4,5: fmt.Printlnt("b")
	}
}
`
	switchStmt := uastFromString(t, source,
		"Decls/[0](FuncDecl)/Body/[0](SwitchStmt)")

	conditionCounter := 0
	for _, switchChild := range switchStmt.Children {
		if switchChild.hasKind(CASE) {
			caseClause := switchChild
			for _, child := range caseClause.Children {
				if child.hasKind(CONDITION) {
					conditionCounter++
				}
			}
		}
	}
	if conditionCounter != 6 {
		t.Fatalf("got %v conditions; expected 6", conditionCounter)
	}
}

func Test_highlightingKinds(t *testing.T) {
	source := `package main
func main(i int) {
	fmt.Println(
      "a",
      'b',
    )
    return 3
}
`
	uast := uastFromString(t, source, "")

	var actualSlice []string
	uast.visit(func(node *Node) {
		if node.Token != nil {
			actualSlice = append(actualSlice, fmt.Sprintf("%s:%v", node.Token.Value, node.Kinds))
		}
	})
	actual := strings.Join(actualSlice, " ")
	expected := "package:[KEYWORD] main:[IDENTIFIER] " +
		"func:[KEYWORD] main:[IDENTIFIER] (:[] i:[PARAMETER IDENTIFIER] int:[TYPE IDENTIFIER] ):[] {:[]" +
		" fmt:[IDENTIFIER] .:[] Println:[IDENTIFIER] (:[LPAREN]" +
		" \"a\":[LITERAL STRING_LITERAL] ,:[]" +
		" 'b':[LITERAL STRING_LITERAL] ,:[]" +
		" ):[RPAREN] " +
		"return:[KEYWORD] 3:[LITERAL] }:[] :[EOF]"
	if expected != actual {
		t.Fatalf("Invalid highlighting kinds, got:\n%v\n\nexpect:\n%v", actual, expected)
	}
}

func Test_emptyStatement(t *testing.T) {
	source := `package main
func main() {
	goto useless_label
useless_label:
}
`
	uast := uastFromString(t, source, "Decls/[0](FuncDecl)/Body/[1](LabeledStmt)")
	actual := len(uast.Children)
	expected := 2 // 'useless_label' ':'
	if expected != actual {
		t.Fatalf("Got:%v expect:%v", actual, expected)
	}
}

func (n *Node) visit(visitor func(node *Node)) {
	visitor(n)
	for _, child := range n.Children {
		child.visit(visitor)
	}
}

func (n *Node) hasKind(kind Kind) bool {
	for _, k := range n.Kinds {
		if k == kind {
			return true
		}
	}
	return false
}

func astFromString(source string) (fileSet *token.FileSet, astFile *ast.File) {
	fileSet, astFile, err := readAstString("main.go", source)
	if err != nil {
		panic(err)
	}
	return
}

func uastFromString(t *testing.T, source string, nodeQueryPath string) *Node {
	fileSet, astNode := astFromString(source)
	uast := toUast(fileSet, astNode, source)
	node, err := uastQuery(uast, nodeQueryPath)
	if err != nil {
		t.Fatalf(err.Error())
	}
	return node
}

func uastQuery(parent *Node, nodeQueryPath string) (*Node, error) {
	if len(nodeQueryPath) == 0 {
		return parent, nil
	}
	node := parent
	var bestMatch string
	for _, pathElem := range strings.Split(nodeQueryPath, "/") {
		var newNode *Node
		for _, child := range node.Children {
			name := child.NativeNode
			if name != pathElem {
				// remove the type from the name, ex: "Name(Ident)" => "Name"
				typeStart := strings.IndexByte(name, '(')
				if typeStart != -1 {
					name = name[0:typeStart]
				}
			}
			if name == pathElem {
				newNode = child
				if len(bestMatch) > 0 {
					bestMatch += "/"
				}
				bestMatch += pathElem
				break
			}
		}
		if newNode == nil {
			data, _ := json.MarshalIndent(node, "", "  ")
			return nil, errors.New(fmt.Sprintf("Invalid nodeQueryPath '%s' best match '%s'\n"+
				"The element '%s' not found as child of this node:\n%s",
				nodeQueryPath, bestMatch, pathElem, string(data)))
		}
		node = newNode
	}
	return node, nil
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
