[Return to index](index.md)

# namespace erdos.fun.walk



__public vars:__ 

## _function_ *quote?* 

_arguments:_ `[x]`

Returns true iff argument is a quoted form.

## _dynamic_ **macro-whitelist** 

## _function_ *code-seq* 

_arguments:_ `[root]`

Returns a lazy seq of code itms in expression

## _function_ *data?* 

_arguments:_ `[xs]`

Returns true if argument evaluates to itself

## _function_ *safe-macroexpand-1* 

_arguments:_ `[expr]`

See: safe-macroexpand

## _function_ *code-seq-children* 

_arguments:_ `[root]`

## _function_ *safe-macroexpand-all* 

_arguments:_ `[form]`

See: safe-macroexpand

## _function_ *code-seq-calls* 

_arguments:_ `[root]`

Returns all function/macro calls from given expression

## _function_ *safe-macroexpand* 

_arguments:_ `[form]`

Like clojure.core/macroexpand but does not expand
  quoted forms.

## _var_ *macro-whitelist-toplevel* 

Set of top level macro expression names

## _function_ *data-coll?* 

_arguments:_ `[xs]`

Returns true iff argument is collection but not seq

## _function_ *scalar?* 

_arguments:_ `[xs]`

Returns if argument is not a composite value.


