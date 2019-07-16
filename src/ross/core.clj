(ns ross.core
  (:gen-class)
  (:require [remus]))

(defn parse-url-list [lines]
  (clojure.string/split lines #"\n"))


;; Some feeds are using "publishedDate", some are using "updatedDate."
;; Reconcile this by taking either as "date".
(defn normalize-date-key [feed-entry]
  (dissoc (assoc feed-entry :date (or (:published-date feed-entry)
                                      (:updated-date feed-entry)))
          :published-date
          :updated-date))


(defn newer-than-n-days-ago? [num-of-days now entry]
  (>= num-of-days
      (/ (- (.getTime now)
            (.getTime (:date entry)))
         (* 1000 60 60 24))))


(defn simple-format-date [date]
  (let [formatter (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.format formatter date)))


(defn format-entry [entry]
  (clojure.string/join "\n" [(:feed-title entry)
                              (simple-format-date (:date entry))
                              (:title entry)
                              (:link entry)]))


(defn process-url [url num-of-days]
  (let [feed (:feed (remus/parse-url url))
        all-keys (keys (first (:entries feed)))
        title (:title feed)
        entries (->> (:entries feed)
                     (map (fn [entry]
                            (select-keys entry
                                         '(:published-date :updated-date :title :link))))
                     (map normalize-date-key)
                     (filter (partial  newer-than-n-days-ago? num-of-days (java.util.Date.)))
                     (map #(assoc % :feed-title (:title feed))))
        formatted-entries (map format-entry entries)]
    formatted-entries))


(defn main [& args]
  (if (empty? args)
    (println "Usage: ross <FEED_LIST_FILE>")
    (let [num-of-days (if (second args)
                        (read-string (second args))
                        7)
          urls (parse-url-list (slurp  (first args)))
          results (agent [] :error-handler println)]
      (doseq [url urls]
        (send-off results
                  (fn [state] (conj state (process-url url num-of-days)))))
      (await results)
      (print (clojure.string/join "\n\n" (flatten @results)))
      (println)
      nil)))


(defn -main [& args]
  (apply main args)
  (shutdown-agents)
  (flush))
