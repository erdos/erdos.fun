[Return to index](index.md)

# namespace erdos.fun

Functional programming utilities

__public vars:__ 

## _macro_ *fncase* 

_arguments:_ `[& clauses]`

## _macro_ *def-* 

_arguments:_ `[name & body]`

## _macro_ *defatom* 

_arguments:_ `[name]`, `[name val]`, `[name docs val]`

## _function_ *fixpt* 

_arguments:_ `[f x]`

Calls f(x), f(f(x)), ... until the same value is returned.

## _macro_ *fnx* 

_arguments:_ `[& body]`

Same as (fn [x] ...)

## _macro_ *cond!* 

_arguments:_ `[& clauses]`

Like clojure.core/cond but throws exception when no clause matched.

## _function_ *comp<* 

_arguments:_ `[& fs]`

## _macro_ *fnxy* 

_arguments:_ `[& body]`

Same as (fn [x y] ...)

## _macro_ *fn->* 

_arguments:_ `[& calls]`

## _macro_ *defatom=* 

_arguments:_ `[name expr]`

Define a var as an atom. Reloads atom value when one of the dereffed atoms change. Beware not to make circular references.

## _function_ *genkwd* 

Like gensym but for keywords. Do not use within loops for keywords are memoized permanently.

## _macro_ *fn-memo* 

_arguments:_ `[head & body]`

Creates a memoized function that may be recursive.
   You can use the given name or (recur) to create a
   recursive memoized function.

   Usage: (fn-memo name [args..] body..)
       or (fn-memo [args..] body..)

## _macro_ *fn->>* 

_arguments:_ `[& calls]`


