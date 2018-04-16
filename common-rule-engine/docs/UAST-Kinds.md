# UAST Kinds
_(do not edit, this page is generated)_

**Summary**
* [Kinds Hierarchy](#kinds-hierarchy)
* [Kinds Properties](#kinds-properties)

## Kinds Hierarchy

* [Annotation](#annotation)
* [AnnotationType](#annotationtype)
* [ArrayAccessExpression](#arrayaccessexpression) { [ArrayObjectExpression](#arrayobjectexpression), [ArrayKeyExpression](#arraykeyexpression) }
* [Assert](#assert)
* [Assignment](#assignment) { [AssignmentTargetList](#assignmenttargetlist), [AssignmentTarget](#assignmenttarget), [AssignmentOperator](#assignmentoperator), [AssignmentValueList](#assignmentvaluelist), [AssignmentValue](#assignmentvalue) }
    * [CompoundAssignment](#compoundassignment)
        * [AndAssignment](#andassignment)
        * [AndNotAssignment](#andnotassignment)
        * [DivideAssignment](#divideassignment)
        * [LeftShiftAssignment](#leftshiftassignment)
        * [MinusAssignment](#minusassignment)
        * [MultiplyAssignment](#multiplyassignment)
        * [OrAssignment](#orassignment)
        * [PlusAssignment](#plusassignment)
        * [RemainderAssignment](#remainderassignment)
        * [RightShiftAssignment](#rightshiftassignment)
        * [UnsignedRightShiftAssignment](#unsignedrightshiftassignment)
        * [XorAssignment](#xorassignment)
* [Block](#block)
* [Call](#call) { [Arguments](#arguments), [Argument](#argument) }
* [Cast](#cast)
* [Catch](#catch)
* [Class](#class)
* [Comment](#comment)
    * [StructuredComment](#structuredcomment)
* [CompilationUnit](#compilationunit) { [Eof](#eof) }
* [ConditionalExpression](#conditionalexpression) { [Condition](#condition), [Then](#then), [Else](#else) }
* [ConditionalJump](#conditionaljump)
    * [If](#if) { [IfKeyword](#ifkeyword), [Condition](#condition), [Then](#then), [ElseKeyword](#elsekeyword), [Else](#else) }
    * [Loop](#loop)
        * [DoWhile](#dowhile)
        * [For](#for) { [ForKeyword](#forkeyword), [ForInit](#forinit), [Condition](#condition), [ForUpdate](#forupdate), [Body](#body) }
        * [Foreach](#foreach)
        * [While](#while) { [Condition](#condition), [Body](#body) }
    * [Switch](#switch) { [Case](#case), [Condition](#condition), [DefaultCase](#defaultcase), [Block](#block) }
    * [Try](#try)
* [Enum](#enum)
* [Expression](#expression)
    * [BinaryExpression](#binaryexpression)
        * [Add](#add)
        * [BitwiseAnd](#bitwiseand)
        * [BitwiseAndNot](#bitwiseandnot)
        * [BitwiseOr](#bitwiseor)
        * [BitwiseXor](#bitwisexor)
        * [Divide](#divide)
        * [Equal](#equal)
        * [GreaterOrEqual](#greaterorequal)
        * [GreaterThan](#greaterthan)
        * [LeftShift](#leftshift)
        * [LessOrEqual](#lessorequal)
        * [LessThan](#lessthan)
        * [LogicalAnd](#logicaland)
        * [LogicalOr](#logicalor)
        * [Multiply](#multiply)
        * [NotEqual](#notequal)
        * [Remainder](#remainder)
        * [RightShift](#rightshift)
        * [Subtract](#subtract)
    * [FunctionLiteral](#functionliteral)
    * [LeftOperand](#leftoperand)
    * [RightOperand](#rightoperand)
* [Function](#function) { [FunctionName](#functionname), [ResultList](#resultlist), [ParameterList](#parameterlist), [Parameter](#parameter), [Body](#body) }
* [Identifier](#identifier)
* [Import](#import) { [ImportEntry](#importentry) }
* [Initializer](#initializer)
* [Keyword](#keyword)
* [Label](#label)
* [Literal](#literal)
    * [BinaryLiteral](#binaryliteral)
    * [BooleanLiteral](#booleanliteral)
    * [CharLiteral](#charliteral)
    * [DecimalLiteral](#decimalliteral)
    * [FloatLiteral](#floatliteral)
    * [HexLiteral](#hexliteral)
    * [IntLiteral](#intliteral)
    * [NullLiteral](#nullliteral)
    * [OctalLiteral](#octalliteral)
    * [StringLiteral](#stringliteral)
* [MemberSelect](#memberselect)
* [Operand](#operand)
* [Operator](#operator)
* [Package](#package)
* [ParenthesizedExpression](#parenthesizedexpression) { [LeftParenthesis](#leftparenthesis), [Expression](#expression), [RightParenthesis](#rightparenthesis) }
* [Statement](#statement)
    * [EmptyStatement](#emptystatement)
* [TypeArguments](#typearguments) { [TypeArgument](#typeargument) }
* [TypeParameters](#typeparameters) { [TypeParameter](#typeparameter) }
* [TypeTest](#typetest)
* [UnaryExpression](#unaryexpression)
    * [BitwiseComplement](#bitwisecomplement)
    * [ChannelDirection](#channeldirection)
    * [LogicalComplement](#logicalcomplement)
    * [Pointer](#pointer)
    * [PostfixDecrement](#postfixdecrement)
    * [PostfixIncrement](#postfixincrement)
    * [PrefixDecrement](#prefixdecrement)
    * [PrefixIncrement](#prefixincrement)
    * [Reference](#reference)
    * [UnaryMinus](#unaryminus)
    * [UnaryPlus](#unaryplus)
* [UnconditionalJump](#unconditionaljump)
    * [Break](#break) { [BranchLabel](#branchlabel) }
    * [Continue](#continue) { [BranchLabel](#branchlabel) }
    * [Fallthrough](#fallthrough)
    * [Goto](#goto) { [BranchLabel](#branchlabel) }
    * [Return](#return)
    * [Throw](#throw)
* [Unsupported](#unsupported)
* [VariableDeclaration](#variabledeclaration) { [VariableName](#variablename), [Type](#type) }
    * [ConstantDeclaration](#constantdeclaration)
    * [Parameter](#parameter)

## Kinds Properties

### Add
Key | ADD
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### AndAssignment
Key | AND_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### AndNotAssignment
Key | AND_NOT_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Go


### Annotation
Key | ANNOTATION
--- | ---
Languages | Java


### AnnotationType
Key | ANNOTATION_TYPE
--- | ---
Languages | Java


### Argument
Key | ARGUMENT
--- | ---
Languages | Java, Go


### Arguments
Key | ARGUMENTS
--- | ---
Languages | Java, Go


### ArrayAccessExpression
Key | ARRAY_ACCESS_EXPRESSION
--- | ---
Components | [ArrayObjectExpression](#arrayobjectexpression), [ArrayKeyExpression](#arraykeyexpression)
Languages | Java, Go


### ArrayKeyExpression
Key | ARRAY_KEY_EXPRESSION
--- | ---
Languages | Java, Go


### ArrayObjectExpression
Key | ARRAY_OBJECT_EXPRESSION
--- | ---
Languages | Java, Go


### Assert
Key | ASSERT
--- | ---
Languages | Java


### Assignment
Key | ASSIGNMENT
--- | ---
Direct sub-kinds | [CompoundAssignment](#compoundassignment)
All sub-kinds | [AndAssignment](#andassignment), [AndNotAssignment](#andnotassignment), [CompoundAssignment](#compoundassignment), [DivideAssignment](#divideassignment), [LeftShiftAssignment](#leftshiftassignment), [MinusAssignment](#minusassignment), [MultiplyAssignment](#multiplyassignment), [OrAssignment](#orassignment), [PlusAssignment](#plusassignment), [RemainderAssignment](#remainderassignment), [RightShiftAssignment](#rightshiftassignment), [UnsignedRightShiftAssignment](#unsignedrightshiftassignment), [XorAssignment](#xorassignment)
Components | [AssignmentTargetList](#assignmenttargetlist), [AssignmentTarget](#assignmenttarget), [AssignmentOperator](#assignmentoperator), [AssignmentValueList](#assignmentvaluelist), [AssignmentValue](#assignmentvalue)
Languages | Java, Go


### AssignmentOperator
Key | ASSIGNMENT_OPERATOR
--- | ---
Languages | Java, Go


### AssignmentTarget
Key | ASSIGNMENT_TARGET
--- | ---
Languages | Java, Go


### AssignmentTargetList
Key | ASSIGNMENT_TARGET_LIST
--- | ---
Languages | Go


### AssignmentValue
Key | ASSIGNMENT_VALUE
--- | ---
Languages | Java, Go


### AssignmentValueList
Key | ASSIGNMENT_VALUE_LIST
--- | ---
Languages | Go


### BinaryExpression
Key | BINARY_EXPRESSION
--- | ---
Extends | [Expression](#expression)
Direct sub-kinds | [Add](#add), [BitwiseAnd](#bitwiseand), [BitwiseAndNot](#bitwiseandnot), [BitwiseOr](#bitwiseor), [BitwiseXor](#bitwisexor), [Divide](#divide), [Equal](#equal), [GreaterOrEqual](#greaterorequal), [GreaterThan](#greaterthan), [LeftShift](#leftshift), [LessOrEqual](#lessorequal), [LessThan](#lessthan), [LogicalAnd](#logicaland), [LogicalOr](#logicalor), [Multiply](#multiply), [NotEqual](#notequal), [Remainder](#remainder), [RightShift](#rightshift), [Subtract](#subtract)
Languages | Java, Go


### BinaryLiteral
Key | BINARY_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java


### BitwiseAnd
Key | BITWISE_AND
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### BitwiseAndNot
Key | BITWISE_AND_NOT
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Go


### BitwiseComplement
Key | BITWISE_COMPLEMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### BitwiseOr
Key | BITWISE_OR
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### BitwiseXor
Key | BITWISE_XOR
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### Block
Key | BLOCK
--- | ---
Languages | Java, Go


### Body
Key | BODY
--- | ---
Languages | Java, Go


### BooleanLiteral
Key | BOOLEAN_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### BranchLabel
Key | BRANCH_LABEL
--- | ---
Languages | Java, Go


### Break
Key | BREAK
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Components | [BranchLabel](#branchlabel)
Languages | Java, Go


### Call
Key | CALL
--- | ---
Components | [Arguments](#arguments), [Argument](#argument)
Languages | Java, Go


### Case
Key | CASE
--- | ---
Languages | Java, Go


### Cast
Key | CAST
--- | ---
Languages | Java


### Catch
Key | CATCH
--- | ---
Languages | Java


### ChannelDirection
Key | CHANNEL_DIRECTION
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Go


### CharLiteral
Key | CHAR_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### Class
Key | CLASS
--- | ---
Languages | Java, Go


### Comment
Key | COMMENT
--- | ---
Direct sub-kinds | [StructuredComment](#structuredcomment)
Languages | Java, Go


### CompilationUnit
Key | COMPILATION_UNIT
--- | ---
Components | [Eof](#eof)
Languages | Java, Go


### CompoundAssignment
Key | COMPOUND_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment)
Direct sub-kinds | [AndAssignment](#andassignment), [AndNotAssignment](#andnotassignment), [DivideAssignment](#divideassignment), [LeftShiftAssignment](#leftshiftassignment), [MinusAssignment](#minusassignment), [MultiplyAssignment](#multiplyassignment), [OrAssignment](#orassignment), [PlusAssignment](#plusassignment), [RemainderAssignment](#remainderassignment), [RightShiftAssignment](#rightshiftassignment), [UnsignedRightShiftAssignment](#unsignedrightshiftassignment), [XorAssignment](#xorassignment)
Languages | Go


### Condition
Key | CONDITION
--- | ---
Languages | Java, Go


### ConditionalExpression
Key | CONDITIONAL_EXPRESSION
--- | ---
Components | [Condition](#condition), [Then](#then), [Else](#else)
Languages | Java


### ConditionalJump
Key | CONDITIONAL_JUMP
--- | ---
Direct sub-kinds | [If](#if), [Loop](#loop), [Switch](#switch), [Try](#try)
All sub-kinds | [DoWhile](#dowhile), [For](#for), [Foreach](#foreach), [If](#if), [Loop](#loop), [Switch](#switch), [Try](#try), [While](#while)
Languages | 


### ConstantDeclaration
Key | CONSTANT_DECLARATION
--- | ---
Extends | [VariableDeclaration](#variabledeclaration)
Languages | Java, Go


### Continue
Key | CONTINUE
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Components | [BranchLabel](#branchlabel)
Languages | Java, Go


### DecimalLiteral
Key | DECIMAL_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### DefaultCase
Key | DEFAULT_CASE
--- | ---
Languages | Java, Go


### Divide
Key | DIVIDE
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### DivideAssignment
Key | DIVIDE_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### DoWhile
Key | DO_WHILE
--- | ---
Extends | [ConditionalJump](#conditionaljump), [Loop](#loop)
Languages | Java


### Else
Key | ELSE
--- | ---
Languages | Java, Go


### ElseKeyword
Key | ELSE_KEYWORD
--- | ---
Languages | Java, Go


### EmptyStatement
Key | EMPTY_STATEMENT
--- | ---
Extends | [Statement](#statement)
Languages | Java, Go


### Enum
Key | ENUM
--- | ---
Languages | Java


### Eof
Key | EOF
--- | ---
Languages | Java, Go


### Equal
Key | EQUAL
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### Expression
Key | EXPRESSION
--- | ---
Direct sub-kinds | [BinaryExpression](#binaryexpression), [FunctionLiteral](#functionliteral), [LeftOperand](#leftoperand), [RightOperand](#rightoperand)
All sub-kinds | [Add](#add), [BinaryExpression](#binaryexpression), [BitwiseAnd](#bitwiseand), [BitwiseAndNot](#bitwiseandnot), [BitwiseOr](#bitwiseor), [BitwiseXor](#bitwisexor), [Divide](#divide), [Equal](#equal), [FunctionLiteral](#functionliteral), [GreaterOrEqual](#greaterorequal), [GreaterThan](#greaterthan), [LeftOperand](#leftoperand), [LeftShift](#leftshift), [LessOrEqual](#lessorequal), [LessThan](#lessthan), [LogicalAnd](#logicaland), [LogicalOr](#logicalor), [Multiply](#multiply), [NotEqual](#notequal), [Remainder](#remainder), [RightOperand](#rightoperand), [RightShift](#rightshift), [Subtract](#subtract)
Languages | Java, Go


### Fallthrough
Key | FALLTHROUGH
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Languages | Go


### FloatLiteral
Key | FLOAT_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### For
Key | FOR
--- | ---
Extends | [ConditionalJump](#conditionaljump), [Loop](#loop)
Components | [ForKeyword](#forkeyword), [ForInit](#forinit), [Condition](#condition), [ForUpdate](#forupdate), [Body](#body)
Languages | Java, Go


### ForInit
Key | FOR_INIT
--- | ---
Languages | Java, Go


### ForKeyword
Key | FOR_KEYWORD
--- | ---
Languages | Java, Go


### ForUpdate
Key | FOR_UPDATE
--- | ---
Languages | Java, Go


### Foreach
Key | FOREACH
--- | ---
Extends | [ConditionalJump](#conditionaljump), [Loop](#loop)
Languages | Java, Go


### Function
Key | FUNCTION
--- | ---
Components | [FunctionName](#functionname), [ResultList](#resultlist), [ParameterList](#parameterlist), [Parameter](#parameter), [Body](#body)
Languages | Java, Go


### FunctionLiteral
Key | FUNCTION_LITERAL
--- | ---
Extends | [Expression](#expression)
Languages | Java, Go


### FunctionName
Key | FUNCTION_NAME
--- | ---
Languages | Java, Go


### Goto
Key | GOTO
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Components | [BranchLabel](#branchlabel)
Languages | Go


### GreaterOrEqual
Key | GREATER_OR_EQUAL
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### GreaterThan
Key | GREATER_THAN
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### HexLiteral
Key | HEX_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### Identifier
Key | IDENTIFIER
--- | ---
Languages | Java, Go


### If
Key | IF
--- | ---
Extends | [ConditionalJump](#conditionaljump)
Components | [IfKeyword](#ifkeyword), [Condition](#condition), [Then](#then), [ElseKeyword](#elsekeyword), [Else](#else)
Languages | Java, Go


### IfKeyword
Key | IF_KEYWORD
--- | ---
Languages | Java, Go


### Import
Key | IMPORT
--- | ---
Components | [ImportEntry](#importentry)
Languages | Java, Go


### ImportEntry
Key | IMPORT_ENTRY
--- | ---
Languages | Java, Go


### Initializer
Key | INITIALIZER
--- | ---
Languages | Java


### IntLiteral
Key | INT_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Go


### Keyword
Key | KEYWORD
--- | ---
Languages | Java, Go


### Label
Key | LABEL
--- | ---
Languages | Java, Go


### LeftOperand
Key | LEFT_OPERAND
--- | ---
Extends | [Expression](#expression)
Languages | Java, Go


### LeftParenthesis
Key | LEFT_PARENTHESIS
--- | ---
Languages | Java, Go


### LeftShift
Key | LEFT_SHIFT
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### LeftShiftAssignment
Key | LEFT_SHIFT_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### LessOrEqual
Key | LESS_OR_EQUAL
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### LessThan
Key | LESS_THAN
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### Literal
Key | LITERAL
--- | ---
Direct sub-kinds | [BinaryLiteral](#binaryliteral), [BooleanLiteral](#booleanliteral), [CharLiteral](#charliteral), [DecimalLiteral](#decimalliteral), [FloatLiteral](#floatliteral), [HexLiteral](#hexliteral), [IntLiteral](#intliteral), [NullLiteral](#nullliteral), [OctalLiteral](#octalliteral), [StringLiteral](#stringliteral)
Languages | Go


### LogicalAnd
Key | LOGICAL_AND
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### LogicalComplement
Key | LOGICAL_COMPLEMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### LogicalOr
Key | LOGICAL_OR
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### Loop
Key | LOOP
--- | ---
Extends | [ConditionalJump](#conditionaljump)
Direct sub-kinds | [DoWhile](#dowhile), [For](#for), [Foreach](#foreach), [While](#while)
Languages | Go


### MemberSelect
Key | MEMBER_SELECT
--- | ---
Languages | Java, Go


### MinusAssignment
Key | MINUS_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### Multiply
Key | MULTIPLY
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### MultiplyAssignment
Key | MULTIPLY_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### NotEqual
Key | NOT_EQUAL
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### NullLiteral
Key | NULL_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### OctalLiteral
Key | OCTAL_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### Operand
Key | OPERAND
--- | ---
Languages | Java, Go


### Operator
Key | OPERATOR
--- | ---
Languages | Java, Go


### OrAssignment
Key | OR_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### Package
Key | PACKAGE
--- | ---
Languages | Java, Go


### Parameter
Key | PARAMETER
--- | ---
Extends | [VariableDeclaration](#variabledeclaration)
Languages | Java, Go


### ParameterList
Key | PARAMETER_LIST
--- | ---
Languages | Java, Go


### ParenthesizedExpression
Key | PARENTHESIZED_EXPRESSION
--- | ---
Components | [LeftParenthesis](#leftparenthesis), [Expression](#expression), [RightParenthesis](#rightparenthesis)
Languages | Java, Go


### PlusAssignment
Key | PLUS_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### Pointer
Key | POINTER
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Go


### PostfixDecrement
Key | POSTFIX_DECREMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### PostfixIncrement
Key | POSTFIX_INCREMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### PrefixDecrement
Key | PREFIX_DECREMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java


### PrefixIncrement
Key | PREFIX_INCREMENT
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java


### Reference
Key | REFERENCE
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Go


### Remainder
Key | REMAINDER
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### RemainderAssignment
Key | REMAINDER_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### ResultList
Key | RESULT_LIST
--- | ---
Languages | Java, Go


### Return
Key | RETURN
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Languages | Java, Go


### RightOperand
Key | RIGHT_OPERAND
--- | ---
Extends | [Expression](#expression)
Languages | Java, Go


### RightParenthesis
Key | RIGHT_PARENTHESIS
--- | ---
Languages | Java, Go


### RightShift
Key | RIGHT_SHIFT
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### RightShiftAssignment
Key | RIGHT_SHIFT_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


### Statement
Key | STATEMENT
--- | ---
Direct sub-kinds | [EmptyStatement](#emptystatement)
Languages | Java, Go


### StringLiteral
Key | STRING_LITERAL
--- | ---
Extends | [Literal](#literal)
Languages | Java, Go


### StructuredComment
Key | STRUCTURED_COMMENT
--- | ---
Extends | [Comment](#comment)
Languages | Java, Go


### Subtract
Key | SUBTRACT
--- | ---
Extends | [BinaryExpression](#binaryexpression), [Expression](#expression)
Languages | Java, Go


### Switch
Key | SWITCH
--- | ---
Extends | [ConditionalJump](#conditionaljump)
Components | [Case](#case), [Condition](#condition), [DefaultCase](#defaultcase), [Block](#block)
Languages | Java, Go


### Then
Key | THEN
--- | ---
Languages | Java, Go


### Throw
Key | THROW
--- | ---
Extends | [UnconditionalJump](#unconditionaljump)
Languages | Java, Go


### Try
Key | TRY
--- | ---
Extends | [ConditionalJump](#conditionaljump)
Languages | Java


### Type
Key | TYPE
--- | ---
Languages | Java, Go


### TypeArgument
Key | TYPE_ARGUMENT
--- | ---
Languages | Java


### TypeArguments
Key | TYPE_ARGUMENTS
--- | ---
Components | [TypeArgument](#typeargument)
Languages | Java


### TypeParameter
Key | TYPE_PARAMETER
--- | ---
Languages | Java


### TypeParameters
Key | TYPE_PARAMETERS
--- | ---
Components | [TypeParameter](#typeparameter)
Languages | Java


### TypeTest
Key | TYPE_TEST
--- | ---
Languages | Java


### UnaryExpression
Key | UNARY_EXPRESSION
--- | ---
Direct sub-kinds | [BitwiseComplement](#bitwisecomplement), [ChannelDirection](#channeldirection), [LogicalComplement](#logicalcomplement), [Pointer](#pointer), [PostfixDecrement](#postfixdecrement), [PostfixIncrement](#postfixincrement), [PrefixDecrement](#prefixdecrement), [PrefixIncrement](#prefixincrement), [Reference](#reference), [UnaryMinus](#unaryminus), [UnaryPlus](#unaryplus)
Languages | Java, Go


### UnaryMinus
Key | UNARY_MINUS
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### UnaryPlus
Key | UNARY_PLUS
--- | ---
Extends | [UnaryExpression](#unaryexpression)
Languages | Java, Go


### UnconditionalJump
Key | UNCONDITIONAL_JUMP
--- | ---
Direct sub-kinds | [Break](#break), [Continue](#continue), [Fallthrough](#fallthrough), [Goto](#goto), [Return](#return), [Throw](#throw)
Languages | 


### UnsignedRightShiftAssignment
Key | UNSIGNED_RIGHT_SHIFT_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java


### Unsupported
Key | UNSUPPORTED
--- | ---
Languages | Go


### VariableDeclaration
Key | VARIABLE_DECLARATION
--- | ---
Direct sub-kinds | [ConstantDeclaration](#constantdeclaration), [Parameter](#parameter)
Components | [VariableName](#variablename), [Type](#type)
Languages | Java, Go


### VariableName
Key | VARIABLE_NAME
--- | ---
Languages | Java, Go


### While
Key | WHILE
--- | ---
Extends | [ConditionalJump](#conditionaljump), [Loop](#loop)
Components | [Condition](#condition), [Body](#body)
Languages | Java


### XorAssignment
Key | XOR_ASSIGNMENT
--- | ---
Extends | [Assignment](#assignment), [CompoundAssignment](#compoundassignment)
Languages | Java, Go


