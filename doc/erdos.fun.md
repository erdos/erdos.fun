# namespace erdos.fun

Functional programming utilities

__public vars:__ 

## _macro_ *fncase* 

## _macro_ *def-* 

## _macro_ *defatom* 

## _function_ *fixpt* 

Calls f(x), f(f(x)), ... until the same value is returned.

## _macro_ *fnx* 

Same as (fn [x] ...)

## _macro_ *cond!* 

Like clojure.core/cond but throws exception when no clause matched.

## _function_ *comp<* 

## _macro_ *fnxy* 

Same as (fn [x y] ...)

## _macro_ *fn->* 

## _macro_ *defatom=* 

Define a var as an atom. Reloads atom value when one of the dereffed atoms change. Beware not to make circular references.

## _function_ *genkwd* 

Like gensym but for keywords. Do not use within loops for keywords are memoized permanently.

## _macro_ *fn-memo* 

Creates a memoized function that may be recursive.
   You can use the given name or (recur) to create a
   recursive memoized function.

   Usage: (fn-memo name [args..] body..)
       or (fn-memo [args..] body..)

## _macro_ *fn->>* 


