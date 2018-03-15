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

	actual := toJson(uastFromString(t, sample, ""))

	expected := `{"kinds": ["COMPILATION_UNIT"], "nativeNode": "(File)", "children": [
  {"kinds": ["PACKAGE"], "nativeNode": "File.Package", "children": [
    {"kinds": ["KEYWORD"], "token": {"value":"package","line":1,"column":1}, "nativeNode": ""},
    {"kinds": ["IDENTIFIER"], "token": {"value":"main","line":1,"column":9}, "nativeNode": "Name(Ident)"}
  ]},
  {"kinds": ["DECL_LIST"], "nativeNode": "Decls([]Decl)", "children": [
    {"kinds": ["FUNCTION"], "nativeNode": "[0](FuncDecl)", "children": [
      {"kinds": ["KEYWORD"], "token": {"value":"func","line":3,"column":1}, "nativeNode": "Type.Func"},
      {"kinds": ["IDENTIFIER"], "token": {"value":"fun","line":3,"column":6}, "nativeNode": "Name(Ident)"},
      {"nativeNode": "Type(FuncType)", "children": [
        {"kinds": ["PARAMETER_LIST"], "nativeNode": "Params(FieldList)", "children": [
          {"token": {"value":"(","line":3,"column":9}, "nativeNode": "Opening"},
          {"token": {"value":")","line":3,"column":10}, "nativeNode": "Closing"}
        ]},
        {"kinds": ["RESULT_LIST"], "nativeNode": "Results(FieldList)", "children": [
          {"nativeNode": "[0](Field)", "children": [
            {"kinds": ["TYPE","IDENTIFIER"], "token": {"value":"string","line":3,"column":12}, "nativeNode": "Type(Ident)"}
          ]}
        ]}
      ]},
      {"kinds": ["BLOCK"], "nativeNode": "Body(BlockStmt)", "children": [
        {"token": {"value":"{","line":3,"column":19}, "nativeNode": "Lbrace"},
        {"kinds": ["ASSIGNMENT","STATEMENT"], "nativeNode": "[0](AssignStmt)", "children": [
          {"kinds": ["ASSIGNMENT_TARGET"], "nativeNode": "Lhs([]Expr)", "children": [
            {"kinds": ["IDENTIFIER"], "token": {"value":"a","line":4,"column":2}, "nativeNode": "[0](Ident)"}
          ]},
          {"token": {"value":":=","line":4,"column":4}, "nativeNode": "Tok"},
          {"kinds": ["ASSIGNMENT_VALUE"], "nativeNode": "Rhs([]Expr)", "children": [
            {"kinds": ["LITERAL","STRING"], "token": {"value":"\"hello \\\"world\\\"\"","line":4,"column":7}, "nativeNode": "[0](BasicLit)"}
          ]}
        ]},
        {"kinds": ["STATEMENT"], "nativeNode": "[1](ReturnStmt)", "children": [
          {"kinds": ["KEYWORD"], "token": {"value":"return","line":5,"column":2}, "nativeNode": "Return"},
          {"kinds": ["IDENTIFIER"], "token": {"value":"a","line":5,"column":9}, "nativeNode": "[0](Ident)"}
        ]},
        {"token": {"value":"}","line":6,"column":1}, "nativeNode": "Rbrace"}
      ]}
    ]}
  ]},
  {"kinds": ["EOF"], "token": {"line":7,"column":1}, "nativeNode": ""}
]}`

	if expected != actual {
		t.Fatalf("got JSON: \n%v\nExpected JSON: \n%v", actual, expected)
	}
}
