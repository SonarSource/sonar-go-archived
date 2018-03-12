package main

import (
	"testing"
)

func Test_toJson(t *testing.T) {
	sample := `package main

func fun() string {
	a := "hello \"world\""
	return a
}
`
	actual := toJson(toUast(astFromString(sample)))

	expected := `{"kinds": ["COMPILATION_UNIT"], "token": {"value":"main","line":1,"column":1}, "nativeNode": "*ast.File", "children": [
  {"kinds": ["DECL_LIST"], "nativeNode": "[]ast.Decl", "children": [
    {"kinds": ["FUNCTION"], "nativeNode": "*ast.FuncDecl", "children": [
      {"kinds": ["IDENTIFIER"], "token": {"value":"fun","line":3,"column":6}, "nativeNode": "*ast.Ident"},
      {"kinds": ["*ast.FuncType"], "nativeNode": "*ast.FuncType", "children": [
        {"kinds": ["PARAMETER_LIST"], "nativeNode": "*ast.FieldList"},
        {"kinds": ["RESULT_LIST"], "nativeNode": "*ast.FieldList", "children": [
          {"kinds": ["*ast.Field"], "nativeNode": "*ast.Field", "children": [
            {"kinds": ["[]*ast.Ident"], "nativeNode": "[]*ast.Ident"},
            {"kinds": ["IDENTIFIER"], "token": {"value":"string","line":3,"column":12}, "nativeNode": "*ast.Ident"}
          ]}
        ]}
      ]},
      {"kinds": ["BLOCK"], "nativeNode": "*ast.BlockStmt", "children": [
        {"kinds": ["ASSIGNMENT","STATEMENT"], "nativeNode": "*ast.AssignStmt", "children": [
          {"kinds": ["ASSIGNMENT_TARGET"], "nativeNode": "[]ast.Expr", "children": [
            {"kinds": ["IDENTIFIER"], "token": {"value":"a","line":4,"column":2}, "nativeNode": "*ast.Ident"}
          ]},
          {"kinds": ["TOKEN"], "token": {"value":":=","line":4,"column":4}, "nativeNode": "token.Token"},
          {"kinds": ["ASSIGNMENT_VALUE"], "nativeNode": "[]ast.Expr", "children": [
            {"kinds": ["LITERAL"], "token": {"value":"\"hello \\\"world\\\"\"","line":4,"column":7}, "nativeNode": "*ast.BasicLit"}
          ]}
        ]},
        {"kinds": ["UNSUPPORTED"], "nativeNode": "*ast.ReturnStmt"}
      ]}
    ]}
  ]}
]}`

	if expected != actual {
		t.Fatalf("got JSON: \n%v\nExpected JSON: \n%v", actual, expected)
	}
}
