package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"sort"
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

	sizeComments := len(comments)
	if sizeComments != 0 {
		for i := 0; i < sizeComments-1; i++ {
			dst.WriteString(strings.Repeat(indent, 3))
			marshallComment(dst, comments[i], strings.Repeat(indent, 3))
			dst.WriteString(",\n")
		}
		dst.WriteString(strings.Repeat(indent, 3))
		marshallComment(dst, comments[sizeComments-1], strings.Repeat(indent, 3))
		dst.WriteString("\n")
	}

	dst.WriteString(strings.Repeat(indent, 2) + "],\n")

	dst.WriteString(strings.Repeat(indent, 2) + "\"tokens\": [\n")
	sizeTokens := len(tokens)
	if sizeTokens != 0 {
		for i := 0; i < sizeTokens-1; i++ {
			dst.WriteString(strings.Repeat(indent, 3))
			writeObjectSlang(dst, tokens[i])
			dst.WriteString(",\n")
		}
		dst.WriteString(strings.Repeat(indent, 3))
		writeObjectSlang(dst, tokens[sizeTokens-1])
		dst.WriteString("\n")
	}
	dst.WriteString(strings.Repeat(indent, 2) + "]\n")

	dst.WriteString(indent + "},\n")
}

func marshallComment(dst *bytes.Buffer, comment *Node, prefix string) {
	text := comment.Token.Value
	var contentText string

	textRange := comment.TextRange
	textContentRange := TextRange(*textRange)
	textContentRange.StartColumn = textContentRange.StartColumn + 2

	if strings.HasPrefix(text, "//") {
		contentText = text[2:]
	} else if strings.HasPrefix(text, "/*") {
		contentText = text[2 : len(text)-2]
		textContentRange.EndColumn = textContentRange.EndColumn - 2
	} else {
		panic("Unknown comment content: " + text)
	}

	dst.WriteString(prefix + "{\"text\":")
	writeObjectSlang(dst, text)
	dst.WriteString(", \"contentText\":")
	writeObjectSlang(dst, contentText)
	dst.WriteString(", \"range\":")
	writeObjectSlang(dst, textRange)
	dst.WriteString(", \"contentRange\": ")
	writeObjectSlang(dst, textContentRange)
	dst.WriteString("}")
}

func marshalIndentSlang(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		dst.WriteString(prefix + "null")
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
		//Sort the fields to report them in the same order
		sortedField := sortSlangField(node.SlangField)

		for _, kv := range sortedField {
			dst.WriteString(",\"" + kv.Key + "\":")

			switch obj := kv.Value.(type) {
			case *Node:
				dst.WriteString("\n")
				marshalIndentSlang(dst, obj, prefix+indent, indent)
			case []*Node:
				dst.WriteString("[\n")
				size := len(obj)
				for i := 0; i < size-1; i++ {
					marshalIndentSlang(dst, obj[i], prefix+indent, indent)
					dst.WriteString(",\n")
				}
				marshalIndentSlang(dst, obj[size-1], prefix+indent, indent)
				dst.WriteString("\n" + prefix + "]")
			default:
				writeObjectSlang(dst, obj)
			}
		}
		dst.WriteString("}")
	} else {
		dst.WriteString("}")
	}
}

type KeyValue struct {
	Key   string
	Value interface{}
}

func sortSlangField(slangField map[string]interface{}) []KeyValue {
	var sortedField []KeyValue
	for k, v := range slangField {
		sortedField = append(sortedField, KeyValue{k, v})
	}

	sort.Slice(sortedField, func(i, j int) bool {
		return sortedField[i].Key > sortedField[j].Key
	})

	return sortedField
}

func writeObjectSlang(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}

	dst.Write(b)
}

func (t TextRange) MarshalJSON() ([]byte, error) {
	return []byte(fmt.Sprintf("\"%d:%d:%d:%d\"", t.StartLine, t.StartColumn, t.EndLine, t.EndColumn)), nil
}
