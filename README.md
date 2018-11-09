# erdos.require-all

A Clojure library designed to require all namespaces from the sources folder.

It can be used to simplify dependency injection. You just put the extra Clojure
files on the classpath and the system loads them automatically.

For example, have `defmethod` calls in the custom Clojure files and they
automatically take effect without manually require-ing the namespaces.

## Usage

First, add the project to your `project.clj` file: `[io.github.erdos/require-all "0.1.0"]`

Then need to require the library itself.

``` clojure
(:require [erdos.require-all :refer [require-all]])
```

Now just call `(require-all project.custom.prefix)`

It will automatically import all namespaces that start with `project.custom.prefix`.

## License

Copyright © 2018 Janos Erdos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
