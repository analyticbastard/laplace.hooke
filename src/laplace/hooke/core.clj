(ns laplace.hooke.core
  (require [clojure.core.async :as a]
           [robert.hooke :as h]
           [listora.measure :as m]))

(defprotocol Collector
  (start [this])
  (stop [this])
  (collect [this project version ns* name* params bench-info]))

(def ^:dynamic *project* "")

(def ^:dynamic *version* "")

(def ^:dynamic *ns-exclusions* #{})

(def ^:dynamic *collector*)

(defn send-to-collector! [f args f*]
  (a/go
    (let [meta-f  (meta f*)
          ns*     (-> meta-f :ns str)
          name*   (-> meta-f :name str)
          arglist (->> meta-f :arglists
                       first                                  ;; todo handle multi arity
                       (remove #(= % '&)))
          params  (interleave arglist args)
          bench   (get (a/<! m/measurements) f*)]
      (.collect *collector* *project* *version* ns* name* params bench))))

(defn manipulate-hooks [ns* p]
  (->> ns*
       ns-publics
       (sequence
         (comp
           (filter #(-> % *ns-exclusions* not))
           (filter (comp fn? var-get second))
           (map p)))))

(defn add-hooks [ns*]
  (letfn [(add-hook [var*]
            (let [f* (-> var* second)]
              (h/add-hook f*
                          ::laplace
                          (fn [f & args]
                            (m/profile f* (apply f args))
                            (send-to-collector! f args f*)))))]
    (manipulate-hooks ns* add-hook)))

(defn remove-hooks [ns*]
  (letfn [(remove-hook [var*]
            (h/remove-hook (-> var* second)
                           ::laplace))]
    (manipulate-hooks ns* remove-hook)))
