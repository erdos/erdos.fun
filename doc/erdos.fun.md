# namespace erdos.fun

Functional programming utilities

__public vars:__ 

## _macro_ *fncase* 

_argument lists_

 - `[& clauses]`

## _macro_ *def-* 

_argument lists_

 - `[name & body]`

## _macro_ *defatom* 

_argument lists_

 - `[name]`

 - `[name val]`

 - `[name docs val]`

## _function_ *fixpt* 

_argument lists_

 - `[f x]`

Calls f(x), f(f(x)), ... until the same value is returned.

## _macro_ *fnx* 

_argument lists_

 - `[& body]`

Same as (fn [x] ...)

## _macro_ *cond!* 

_argument lists_

 - `[& clauses]`

Like clojure.core/cond but throws exception when no clause matched.

## _function_ *comp<* 

_argument lists_

 - `[& fs]`

## _macro_ *fnxy* 

_argument lists_

 - `[& body]`

Same as (fn [x y] ...)

## _macro_ *fn->* 

_argument lists_

 - `[& calls]`

## _macro_ *defatom=* 

_argument lists_

 - `[name expr]`

Define a var as an atom. Reloads atom value when one of the dereffed atoms change. Beware not to make circular references.

## _function_ *genkwd* 

Like gensym but for keywords. Do not use within loops for keywords are memoized permanently.

## _macro_ *fn-memo* 

_argument lists_

 - `[head & body]`

Creates a memoized function that may be recursive.
   You can use the given name or (recur) to create a
   recursive memoized function.

   Usage: (fn-memo name [args..] body..)
       or (fn-memo [args..] body..)

## _macro_ *fn->>* 

_argument lists_

 - `[& calls]`


