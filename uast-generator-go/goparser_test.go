package main

import (
	"encoding/json"
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
	example_with_compound_assignment = `package main
func main() {
	a += 1	
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

type TestNode struct {
	kinds      []Kind
	children   int
	nativeNode string
	token      Token
}

func newTestNode(node *Node) TestNode {
	testNode := TestNode{
		kinds:      node.Kinds,
		nativeNode: node.NativeNode,
		children:   len(node.Children),
	}

	if node.Token != nil {
		testNode.token = *node.Token
	}

	return testNode
}

func Test_mapFile(t *testing.T) {
	uast := uastFromString(t, example_hello_world, "")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{COMPILATION_UNIT},
		nativeNode: "(File)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	actual = newTestNode(uast.Children[1])
	expected = TestNode{
		kinds:      []Kind{DECL_LIST},
		nativeNode: "Decls([]Decl)",
		children:   2,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapFuncDecl(t *testing.T) {
	uast := uastFromString(t, example_hello_world, "Decls/[1](FuncDecl)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{FUNCTION},
		nativeNode: "[1](FuncDecl)",
		children:   4,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapFuncDecl_forward_declaration(t *testing.T) {
	uast := uastFromString(t, "package main\nfunc forward_declaration() int64",
		"Decls/[0](FuncDecl)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{FUNCTION},
		nativeNode: "[0](FuncDecl)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapFuncDecl_Name(t *testing.T) {
	uast := uastFromString(t, example_hello_world, "Decls/[1](FuncDecl)/Name")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{IDENTIFIER},
		nativeNode: "Name(Ident)",
		token:      Token{Value: "main", Line: 3, Column: 6},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapFuncDecl_complete(t *testing.T) {
	uast := uastFromString(t, example_with_complete_function,
		"Decls/[1](FuncDecl)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{FUNCTION},
		nativeNode: "[1](FuncDecl)",
		children:   5,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	actual = newTestNode(uast.Children[0])
	expected = TestNode{
		kinds:      []Kind{KEYWORD},
		nativeNode: "Type.Func",
		token:      Token{Value: "func", Line: 3, Column: 1},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	actual = newTestNode(uast.Children[1])
	expected = TestNode{
		kinds:      []Kind{PARAMETER_LIST},
		nativeNode: "Recv(FieldList)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	actual = newTestNode(uast.Children[2])
	expected = TestNode{
		kinds:      []Kind{IDENTIFIER},
		nativeNode: "Name(Ident)",
		token:      Token{Value: "fun", Line: 3, Column: 13},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	funcType := uast.Children[3]
	actual = newTestNode(funcType)
	expected = TestNode{
		nativeNode: "Type(FuncType)",
		children:   2,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	params := funcType.Children[0]
	actual = newTestNode(params)
	expected = TestNode{
		kinds:      []Kind{PARAMETER_LIST},
		nativeNode: "Params(FieldList)",
		children:   7,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intParams := params.Children[1]
	actual = newTestNode(intParams)
	expected = TestNode{
		nativeNode: "[0](Field)",
		children:   2,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intParamsNames := intParams.Children[0]
	actual = newTestNode(intParamsNames)
	expected = TestNode{
		nativeNode: "Names([]*Ident)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	n1 := intParamsNames.Children[0]
	actual = newTestNode(n1)
	expected = TestNode{
		kinds:      []Kind{PARAMETER, IDENTIFIER},
		nativeNode: "[0](Ident)",
		token:      Token{Value: "n1", Line: 3, Column: 17},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intParamsType := intParams.Children[1]
	actual = newTestNode(intParamsType)
	expected = TestNode{
		kinds:      []Kind{TYPE, IDENTIFIER},
		nativeNode: "Type(Ident)",
		token:      Token{Value: "int", Line: 3, Column: 24},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	stringParams := params.Children[3]
	actual = newTestNode(stringParams)
	expected = TestNode{
		nativeNode: "[1](Field)",
		children:   2,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	stringParamsNames := stringParams.Children[0]
	actual = newTestNode(stringParamsNames)
	expected = TestNode{
		nativeNode: "Names([]*Ident)",
		children:   1,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	s1 := stringParamsNames.Children[0]
	actual = newTestNode(s1)
	expected = TestNode{
		kinds:      []Kind{PARAMETER, IDENTIFIER},
		nativeNode: "[0](Ident)",
		token:      Token{Value: "s1", Line: 3, Column: 29},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	stringParamsType := stringParams.Children[1]
	actual = newTestNode(stringParamsType)
	expected = TestNode{
		kinds:      []Kind{TYPE, IDENTIFIER},
		nativeNode: "Type(Ident)",
		token:      Token{Value: "string", Line: 3, Column: 32},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	results := funcType.Children[1]
	actual = newTestNode(results)
	expected = TestNode{
		kinds:      []Kind{RESULT_LIST},
		nativeNode: "Results(FieldList)",
		children:   5,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intResults := results.Children[1]
	actual = newTestNode(intResults)
	expected = TestNode{
		nativeNode: "[0](Field)",
		children:   2,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intResultsNames := intResults.Children[0]
	actual = newTestNode(intResultsNames)
	expected = TestNode{
		nativeNode: "Names([]*Ident)",
		children:   1,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	n := intResultsNames.Children[0]
	actual = newTestNode(n)
	expected = TestNode{
		kinds:      []Kind{RESULT, IDENTIFIER},
		nativeNode: "[0](Ident)",
		token:      Token{Value: "n", Line: 3, Column: 50},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	intResultsType := intResults.Children[1]
	actual = newTestNode(intResultsType)
	expected = TestNode{
		kinds:      []Kind{TYPE, IDENTIFIER},
		nativeNode: "Type(Ident)",
		token:      Token{Value: "int", Line: 3, Column: 52},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapBlockStmt(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{BLOCK},
		nativeNode: "Body(BlockStmt)",
		children:   4,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapAssignStmt(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{ASSIGNMENT, DECLARATION, STATEMENT},
		nativeNode: "[0](AssignStmt)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapAssignStmt2(t *testing.T) {
	uast := uastFromString(t, example_with_compound_assignment,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{ASSIGNMENT, COMPOUND_ASSIGNMENT, STATEMENT},
		nativeNode: "[0](AssignStmt)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapExprList(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)/Lhs")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{ASSIGNMENT_TARGET},
		nativeNode: "Lhs([]Expr)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapExpr_Ident(t *testing.T) {
	uast := uastFromString(t, example_with_two_assignments,
		"Decls/[0](FuncDecl)/Body/[0](AssignStmt)/Lhs/[0](Ident)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{IDENTIFIER},
		nativeNode: "[0](Ident)",
		children:   0,
		token:      Token{Value: "a", Line: 3, Column: 2},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapExpr_BasicLit(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[0](AssignStmt)/Rhs/[0](BasicLit)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{LITERAL, STRING_LITERAL},
		nativeNode: "[0](BasicLit)",
		children:   0,
		token:      Token{Value: "\"hello, world\"", Line: 4, Column: 9},
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapExprStmt(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[1](ExprStmt)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{EXPRESSION, STATEMENT},
		nativeNode: "[1](ExprStmt)",
		children:   1,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapCallExpr(t *testing.T) {
	uast := uastFromString(t, example_hello_world,
		"Decls/[1](FuncDecl)/Body/[1](ExprStmt)/X(CallExpr)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{CALL},
		nativeNode: "X(CallExpr)",
		children:   4,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
}

func Test_mapIfStmt(t *testing.T) {
	uast := uastFromString(t, example_with_if,
		"Decls/[0](FuncDecl)/Body/[1](IfStmt)")

	actual := newTestNode(uast)
	expected := TestNode{
		kinds:      []Kind{IF, STATEMENT},
		nativeNode: "[1](IfStmt)",
		children:   3,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}
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

	actual := newTestNode(body)
	expected := TestNode{
		kinds:      []Kind{BLOCK},
		nativeNode: "Body(BlockStmt)",
		children:   21,
	}

	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	// remove the '{' and '}' child
	statements := body.Children[1 : len(body.Children)-1]

	for i, stmt := range statements {
		uast := uastQuery(t, stmt, "Rhs([]Expr)/[0]")
		actual := newTestNode(uast)
		expected := TestNode{
			kinds:      []Kind{BINARY_EXPRESSION},
			nativeNode: "[0](BinaryExpr)",
			children:   3,
		}

		if !reflect.DeepEqual(expected, actual) {
			t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
		}

		if expected, actual := expectedOperators[i], uast.Children[1].Token.Value; expected != actual {
			t.Fatalf("got '%v'; expected '%v'", actual, expected)
		}
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

func Test_checkKindsForInterfaceAndStruct(t *testing.T) {
	var expected []Kind

	source := `package main
type A interface { foo() }
type B struct    { a int }
func bar(x interface{}) {
}
`
	declarations := uastFromString(t, source, "Decls")

	typeA := uastQuery(t, declarations, "[0](GenDecl)/Specs/[0](TypeSpec)")
	actual := typeA.Kinds
	expected = []Kind{CLASS}
	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	typeB := uastQuery(t, declarations, "[1](GenDecl)/Specs/[0](TypeSpec)")
	actual = typeB.Kinds
	expected = []Kind{CLASS}
	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
	}

	x := uastQuery(t, declarations, "[2](FuncDecl)/Type/Params/[0](Field)/Type(InterfaceType)")
	actual = x.Kinds
	expected = []Kind{TYPE}
	if !reflect.DeepEqual(expected, actual) {
		t.Fatalf("got: %#v\nexpected: %#v", actual, expected)
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
		t.Fatalf("Got %v; expected %v", actual, expected)
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
	return uastQuery(t, uast, nodeQueryPath)
}

func uastQuery(t *testing.T, parent *Node, nodeQueryPath string) *Node {
	if len(nodeQueryPath) == 0 {
		return parent
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
			t.Fatalf(fmt.Sprintf("Invalid nodeQueryPath '%s' best match '%s'\n"+
				"The element '%s' not found as child of this node:\n%s",
				nodeQueryPath, bestMatch, pathElem, string(data)))
		}
		node = newNode
	}
	return node
}
