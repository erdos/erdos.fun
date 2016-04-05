# namespace erdos.fun.docs

Generating markdown documentation from source.

__public vars:__ 

## _function_ *process-index* 

_arguments:_ `[bib]`

Returns a map containin info to build an index.md file.
  Argument `bib`: seq of values returned from `process-ns`
  Returning map keys:
  - file: index file name
  - lines: seq of lines to insert to file

## _function_ *process-var* 

_arguments:_ `[v]`

## _string_ *output-dir* 

## _function_ *process-var-lines* 

_arguments:_ `[v]`

## _function_ *words* 

_arguments:_ `[& args]`

## _function_ *process* 

_arguments:_ `[]`, `[root]`

## _function_ *lines* 

_arguments:_ `[& args]`

## _function_ *process-ns-lines* 

_arguments:_ `[ns]`

## _function_ *process-ns* 

_arguments:_ `[ns]`

## _function_ *list-files* 

_arguments:_ `[]`

## _function_ *link* 

_arguments:_ `[name id href]`, `[name href]`

## _string_ *project-dir* 

## _function_ *process-index-lines* 

_arguments:_ `[bib]`

## _function_ *var->type* 

_arguments:_ `[v]`

Returns a human readable string containing information on the type of argument.

## _function_ *list-namespaces* 

_arguments:_ `[dir]`, `[]`

Get a list of project namespaces.


