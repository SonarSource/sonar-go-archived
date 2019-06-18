package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"strings"
)

func toJsonSlang(node *Node, comments []*Node, tokens []*Token) string {
	var buf bytes.Buffer
	indent := "  "
	buf.WriteString("{ \n")
	marshallSlangMetaData(&buf, comments, tokens, indent)
	buf.WriteString(indent + "\"tree\":\n")
	marshalIndentSlang(&buf, node, "    ", indent)
	buf.WriteString("\n} \n")
	return string(buf.Bytes())
}

func marshallSlangMetaData(dst *bytes.Buffer, comments []*Node, tokens []*Token, indent string) {
	dst.WriteString(indent + "\"treeMetaData\": {\n")
	dst.WriteString(strings.Repeat(indent, 2) + "\"comments\": [\n")
	for _, element := range comments {
		dst.WriteString(strings.Repeat(indent, 3))
		marshallComment(dst, element, strings.Repeat(indent, 3))
	}
	dst.WriteString(strings.Repeat(indent, 2) + "],\n")

	dst.WriteString(strings.Repeat(indent, 2) + "\"tokens\": [\n")
	for _, element := range tokens {
		dst.WriteString(strings.Repeat(indent, 3))
		writeObjectSlang(dst,element)
		dst.WriteString(",\n")
	}
	dst.WriteString("\n" + strings.Repeat(indent, 2) + "]\n")

	dst.WriteString(indent + "},\n")
}

func marshallComment(dst *bytes.Buffer, comment *Node, prefix string) {
	text := comment.Token.Value
	var contentText string

	textRange := comment.TextRange
	textContentRange := TextRange(*textRange)
	textContentRange.StartColumn = textContentRange.StartColumn + 2

	if 	strings.HasPrefix(text, "//") {
		contentText = text[2:]
	} else if strings.HasPrefix(text,"/*") {
		contentText = text[2:len(text)-2]
		textContentRange.EndColumn = textContentRange.EndColumn - 2
	} else {
		panic("Unknown comment content: " + text)
	}

	dst.WriteString(prefix + "{\"text\":\"" + text + "\", \"contentText\":\"" + contentText + "\", \"range\":")
	writeObjectSlang(dst, textRange)
	dst.WriteString(", \"contentRange\": ")
	writeObjectSlang(dst, textContentRange)
	dst.WriteString("}\n")
}

func marshalIndentSlang(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		return
	}

	dst.WriteString(prefix + "{")
	dst.WriteString("\"@type\": ")
	writeObjectSlang(dst, node.SlangType)

	if node.TextRange != nil {
		dst.WriteString(", \"metaData\": ")
		writeObjectSlang(dst, node.TextRange)
	}

	if len(node.SlangField) != 0 {
		for k, v := range node.SlangField {
			dst.WriteString(",\"" + k + "\":")

			switch obj := v.(type) {
			case *Node:
				marshalIndentSlang(dst, obj, prefix+indent, indent)
			case []*Node:
				dst.WriteString( "[\n")
				size := len(obj)
				for i := 0; i < size-1; i++ {
					marshalIndentSlang(dst, obj[i], prefix+indent, indent)
					dst.WriteString(",\n")
				}
				marshalIndentSlang(dst, obj[size-1], prefix+indent, indent)
				dst.WriteString("\n" + prefix + "]")
			default:
				writeObjectSlang(dst, v)
			}
		}
		dst.WriteString( "}")
	} else {
		dst.WriteString("}")
	}



	/*
	if len(node.Children) > 0 {
		for k, v := range node.SlangField {
			dst.WriteString(k)
		}
		dst.WriteString(", \"children\": "+fmt.Sprintf("%d", len(node.SlangField))+"[{\n" + prefix)
		size := len(node.Children)
		for i := 0; i < size-1; i++ {
			child := node.Children[i]
			marshalIndentSlang(dst, child, prefix+indent, indent)
			dst.WriteString(",\n" + prefix)
		}
		lastChildren := node.Children[size-1]
		marshalIndentSlang(dst, lastChildren, prefix+indent, indent)
		dst.WriteString("\n" + prefix[0:len(prefix)-len(indent)] + "}]")
	}*/
}

func writeObjectSlang(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}
	dst.Write(b)
}

func (t TextRange) MarshalJSON() ([]byte, error) {
	return []byte(fmt.Sprintf("\"%d:%d:%d:%d\"", t.StartLine,t.StartColumn,t.EndLine,t.EndColumn)), nil
}