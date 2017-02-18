# laplace.hooke

A Clojure library designed to intercept Clojure function calls
and collect information about its parameters.

The main use case for the library is performing statistical
analyses that help improve the software performance by using memoization
and discover bottlenecks and possible error causes by correlations,
regression, Bayesian analysis or outlier detection.

The library uses [listora.measure](https://github.com/listora/measure)
to get function execution times too.

The monitoring system is done asynchronously so a minimal impact
on the system's performance is expected.

In the future, Elasticsearch and other collectors 
should be put into its own library/plugin so that they are injected
by Leiningen according to the needs of each project.


## Usage

Include the dependency in your `project.clj`

- TODO upload to Clojars

Attach a collector so that you can send events.

The naive collector just prints out to console

```clojure
(laplace.hooke.logger/add-logger)
```

The Elasticsearch collector is also implemented. Use your host, TCP port and
cluster name. Elasticsearch must already be running:

```clojure
(laplace.hooke.elasticsearch/add-local-elasticsearch "localhost" 9300 "elasticsearch")
```

Now set up the hooks for your namespace. For example, in the namespace `user` we
create a dummy function

```clojure
(defn example [a b & c])
```

and then we set up the hooks for the namespace

```clojure
(laplace.hooke.core/add-hooks 'user)
```

By invoking the function, the library sends the parameter and execution time information
to the collector you used. In case of the logger collector, the REPL asynchronously gets

```
(example 1 2)
=>
. user.example : (a 1 b 2)
{:elapsed 5.0E-6}
```

If you used the Elasticsearch collector, the newly indexed document, upon querying with
```curl -X GET 'http://localhost:9200/laplacehook_/_search/?size=1000&pretty=1'``` is

```json
"hits": [
      {
        "_index": "laplacehook_",
        "_type": "user/example",
        "_id": "AVpSRVjMH0p0EeP2WQss",
        "_score": 1.0,
        "_source": {
          "elapsed": 5.0E-6,
          "a": 1,
          "b": 2
        }
      }
    ]
```


## TODO

- upload to Clojars
- individual function hooks
- Leiningen plugin
- Namespace/function exclusions
- Argument exclusions/obfuscations

## License

Copyright © 2017 Javier Arriero

Distributed under the Eclipse Public License either version 1.0.
