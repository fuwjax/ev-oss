- Grammar = Line '\n' Grammar | '\n' Grammar | ''
-- A Grammar is a set of newline separated Lines.
--  blank lines are ignored.
GL\nG
G\nG
G
- Line = Rule | Comment | Pattern
-- A Line is either a Rule, Comment, or regex Pattern
LR
LC
LP
- Rule = Symbol Expression
-- A Rule declaration is a capital letter symbol followed by an Expression
R2X
- Expression = Symbol Expression | Digit Expression | Value Expression | ''
--  any capital letters in the Expression will be interpreted as symbol references,
--  any digit is interpreted as a regular expression reference, and
--  any other character is interpreted as a literal
X2X
X3X
X4X
X
- Symbol = [A-Z]
-- A symbol starts a Rule or is a reference within an Expression
--  any symbol referenced but not declared is a syntax error
2A-Z
- Digit = [0-9]
-- A digit starts a Pattern or is a reference within an Expression
--  any digit referenced but not declared is a syntax error
30-9
- Value = [^\n0-9A-Z\\]
-- A value is anything that isn't a newline, symbol or digit
4^\n\\0-9A-Z
- Comment = '-' Ignored
-- A Comment is a dash followed by Ignored text
C-I
- Ignored = [^\n] Ignored | ''
-- Ignored text is a possibly empty set of characters till the end of line
I5I
I
5^\n
- Pattern = Digit Qualified | Digit '^' Qualified
-- A Pattern is a Digit followed by a Qualified character class regex
--  the Qualified is interpreted as a regex character class literal,
--  and is not processed for symbol or regex references
P3Q
P3^Q
- Escape = '\\' [^]
-- Escapes are replacement patterns for control and reserved characters.
--  They are interpreted by the parent interpreter, but must
--  include \\, \n, \r, and \t at a minimum.
E\\6
6^
- Qualified = One_char Qualified | Wide_range Qualified | ''
QWQ
QOQ
Q
- One_char = [^\\] | Escape
O7
OE
7^\\
- Wide_range= One_char '-' One_char
WO-O
