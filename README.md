# erdos.require-all

A Clojure library designed to look up resources from the classpath.

It can be used to simplify dependency injection. You just put the extra Clojure
files on the classpath and the system loads them automatically. For example, have 
`defmethod` calls in the custom Clojure files and they automatically take effect 
without manually require-ing the namespaces.

[![Clojars Project](https://img.shields.io/clojars/v/io.github.erdos/require-all.svg)](https://clojars.org/io.github.erdos/require-all)

## Usage

First, add the project to your `project.clj` file: `[io.github.erdos/require-all "0.1.2"]`

Then need to require the library itself.

``` clojure
(:require [erdos.require-all :refer [require-all]])
```

Now just call `(require-all project.custom.prefix)`

### Require namespaces

Call `(require-all custom-prefix)` to require all namespaces with a given prefix.

For example, `(require-all myapp.db)` may require both `myapp.db.postgres` and `myapp.db.oracle` from the classpath.

### Lookup resources

The `list-all-resources` function can be use to get a list of all resource `URL` objects matching a pattern.

- `(list-all-resources :prefix "files/core")` will list all files under a path recursively.
- `(list-all-resources :suffix ".edn")` will list all files with `edn` extension.
- `(list-all-resources :suffix-ci ".edn")` is a case-insensitive version of te above.
- `(list-all-resources :predicate p)` will pass every file to the `p` predicate function and return only matching items.


## License

Copyright © 2018 Janos Erdos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
