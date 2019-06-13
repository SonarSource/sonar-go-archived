package main

/*
import (
	"go/ast"
	"go/token"
)

type SlangNode struct {
	SlangTree string
	Children []*NodeElement
	Metadata *TreeMetaData
}

type NodeElement struct {
	Name string
	Node *SlangNode
}

type TreeMetaData struct {
	TextRange *TextRange
	CommentsInside []*Comment
	Tokens []*SlangToken
	LinesOfCode []int
}

type TextRange struct {
	Start *TextPointer
	End *TextPointer
}

type TextPointer struct {
	Line int
	LineOffset int
}

type Comment struct {
	Text string
	ContentText string
	ContentRange *TextRange
}

type SlangToken struct {
	Text string
	TokenType string
}
*/
/*
func toSlangAst(fileSet *token.FileSet, astFile *ast.File, fileContent string) *SlangNode {
	return convert(*astFile)
}


func convert(astNode ast.File) *SlangNode {
  switch node := astNode.(type) {
	case *ast.IfStmt:
		return createIfTree(astNode)
	default:
		return createNativeTree(astNode)
	}
}


func createIfTree(astNode ast.IfStmt) *SlangNode {
	metaData := createMetaData(astNode)

	return &SlangNode{
		SlangTree: "If",
		Children: nil, //TODO
		Metadata: metaData,
	}
}


func createNativeTree(astNode ast.File) *SlangNode {
	metaData := createMetaData(astNode)

	return &SlangNode{
		SlangTree: "Native",
		Children: nil, //TODO
		Metadata: metaData,
	}
}

func createMetaData(astNode ast.File) *TreeMetaData {
	startPointer := &TextPointer{Line: 1, LineOffset: 2} //TODO
	endPointer := &TextPointer{Line: 1, LineOffset: 2} //TODO

	textRange := &TextRange{
		Start: startPointer,
		End: endPointer,
	}

	return &TreeMetaData{
		TextRange: textRange,
		CommentsInside: nil, //TODO
		Tokens: nil, //TODO
		LinesOfCode: nil,//TODO
	}
}
*/
