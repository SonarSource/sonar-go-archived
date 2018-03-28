// SonarQube Go Plugin
// Copyright (C) 2018-2018 SonarSource SA
// mailto:info AT sonarsource DOT com
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 3 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, write to the Free Software Foundation,
// Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

package main

import (
	"bytes"
	"encoding/json"
)

func toJson(node *Node) string {
	var buf bytes.Buffer
	marshalIndent(&buf, node, "  ", "  ")
	return string(buf.Bytes())
}

func marshalIndent(dst *bytes.Buffer, node *Node, prefix, indent string) {
	if node == nil {
		return
	}

	dst.WriteByte('{')

	if len(node.Kinds) > 0 {
		dst.WriteString("\"kinds\": ")
		writeObject(dst, node.Kinds)
		dst.WriteString(", ")
	}

	if node.Token != nil {
		dst.WriteString("\"token\": ")
		writeObject(dst, node.Token)
		dst.WriteString(", ")
	}

	dst.WriteString("\"nativeNode\": \"" + node.NativeNode + "\"")

	if len(node.Children) > 0 {
		dst.WriteString(", \"children\": [\n" + prefix)
		size := len(node.Children)
		for i := 0; i < size-1; i++ {
			child := node.Children[i]
			marshalIndent(dst, child, prefix+indent, indent)
			dst.WriteString(",\n" + prefix)
		}
		marshalIndent(dst, node.Children[size-1], prefix+indent, indent)
		dst.WriteString("\n" + prefix[0:len(prefix)-len(indent)] + "]")
	}
	dst.WriteString("}")
}

func writeObject(dst *bytes.Buffer, obj interface{}) {
	b, err := json.Marshal(obj)
	if err != nil {
		panic(err)
	}
	dst.Write(b)
}
