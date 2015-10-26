- #start rules
GIR
- #ignore [ \n\t\r]
I1
I
1[ \n\t\r]

- rules:= directive rules | rule rules | token rules | ''
RDIR
RUIR
RTIR
R
- directive:= start | ignore
DB
DW
- start:= '#' 'start' expression
B#IstartIX
- ignore:= '#' 'ignore' expression
W#IignoreIX
- rule:= symbol ':=' expression
USI:=IX
- token:= symbol '=' expression
TSI=IX
- expression:= symbol expression | literal expression | class expression | ''
XSIX
XLIX
XCIX
X

- symbol= [_A-Za-z] symboltail
S2Q
2[_A-Za-z]
- symboltail= [_A-Za-z0-9] symboltail | ''
Q3Q
Q
3[_A-Za-z0-9]
- literal= ['] single [']
L'P'
- single= [^'\\] single | escape single | ''
PEP
P4P
P
4[^'\\]
- escape= '\\' [^]
E\0
- class= '[' chars ']' | '[^' chars ']'
C[V]
C[^V]
- chars= char chars | range chars | ''
VOV
VAV
V
- char= [^\\\]\-\^] | escape
O5
OE
5[^\\\[\-\^]
-range= char '-' char
AO-O
