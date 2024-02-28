# PINSCompiler
A Java compiler made for PINS Language (a made up language) as part of undergraduate Compilers class.

## Usage
"example" files in the root directory are examples of valid source codes in PINS Language.

To compile source file: `PINS example`.

Use `--exec <phase>` to execute only phases up to selected phase. Options are: LEX, SYN, AST, NAME, TYP, FRM, IMC, INT.


## Compiler phases
1. Lexical analysis - break down the source code into tokens and remove comments and whitespace
2. Syntax analysis - parse tokens employing the context-free grammar described below
3. Abstract syntax - create an abstract syntax tree (AST) to represent the syntactic structure
4. Semantic analysis - check types, scope (using double pass), names
5. Stack frame generation - create stack frames for function calls (allocate memory on call stack for local variables, parameters, return addresses)
6. Intermediate code generation - translate AST into IM (intermediate code) that is more platform independent and suitable for optimization
7. Interpreter - execute generated intermediate code

## PINS Language
Defined in `pins.pdf` (in Slovenian).
Supported types are logical, integer, string and arrays (multi-dimensional).
To create a new scope, use `expression { WHERE definitons}`. A name is visible in entire scope.
Standard library is also implemented (print_int, print_str, print_log, rand_int, seed).


## Context-free grammar (CFG)
```
source -> defs .

defs -> def defs2 .
defs2 -> ';' def defs2 .
defs2 -> .

def -> type_def .
def -> fun_def .
def -> var_def .

type_def -> typ id ':' type .
type -> id .
type -> logical .
type -> integer .
type -> string .
type -> arr '['int_const']' type .

fun_def -> fun id '('params')' ':' type '=' expr .

params -> param params2 .
params2 -> ',' param params2 .
params2 -> .

param -> id ':' type .

expr -> logical_ior_expr expr2 .
expr2 -> .
expr2 -> '{' WHERE defs '}' .

logical_ior_expr -> logical_and_expr logical_ior_expr2 .
logical_ior_expr2 -> '|' logical_and_expr logical_ior_expr2 .
logical_ior_expr2 -> .

logical_and_expr -> compare_expr logical_and_expr2 .
logical_and_expr2 -> '&' compare_expr logical_and_expr2 .
logical_and_expr2 -> .

compare_expr -> add_expr compare_expr2 .
compare_expr2 -> '==' add_expr .
compare_expr2 -> '!=' add_expr .
compare_expr2 -> '<=' add_expr .
compare_expr2 -> '>=' add_expr .
compare_expr2 -> '<' add_expr .
compare_expr2 -> '>' add_expr .
compare_expr2 -> .

add_expr -> mul_expr add_expr2 .
add_expr2 -> '+' mul_expr add_expr2 .
add_expr2 -> '-' mul_expr add_expr2 .
add_expr2 -> .

mul_expr -> pre_expr mul_expr2 .
mul_expr2 -> '*' pre_expr mul_expr2 .
mul_expr2 -> '/' pre_expr mul_expr2 .
mul_expr2 -> '%' pre_expr mul_expr2 .
mul_expr2 -> .

pre_expr -> '+'pre_expr .
pre_expr -> '-'pre_expr .
pre_expr -> '!'pre_expr .
pre_expr -> post_expr .

post_expr -> atom_expr post_expr2 .
post_expr2 -> '[' expr ']' post_expr2 .
post_expr2 -> .

atom_expr -> log_constant .
atom_expr -> int_constant .
atom_expr -> str_constant .
atom_expr -> id atom_expr2 .
atom_expr2 -> .
atom_expr2 -> '(' exprs ')' .


atom_expr -> '{' atom_expr3 .
atom_expr3 -> if expr then expr atom_expr4 .
atom_expr4 -> '}' .
atom_expr4 -> else expr '}' .
atom_expr3 -> while expr ':' expr '}' .
atom_expr3 -> for id '=' expr ',' expr ',' expr ':' expr '}' .
atom_expr3 -> expr '=' expr '}' .

atom_expr -> '(' exprs ')' .

exprs -> expr exprs2 .
exprs2 -> ',' expr exprs2 .
exprs2 -> .

var_def -> var id ':' type .
```
