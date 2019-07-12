(ns ross.core
  (:gen-class)
  (:require [remus]))

(def OLDEST-POST-AGE-DAYS 7)

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
  (clojure.string/join " | " [(:feed-title entry)
                              (simple-format-date (:date entry))
                              (:title entry)
                              (:link entry)]))


(defn process-url [url]
  (let [feed (:feed (remus/parse-url url))
        all-keys (keys (first (:entries feed)))
        ; add title
        title (:title feed)
        entries (->> (:entries feed)
                     (map (fn [entry]
                            (select-keys entry
                                         '(:published-date :updated-date :title :link))))
                     (map normalize-date-key)
                     (filter (partial  newer-than-n-days-ago? OLDEST-POST-AGE-DAYS (java.util.Date.)))
                     (map #(assoc % :feed-title (:title feed))))
        formatted-entries (map format-entry entries)]
    (doall (map println formatted-entries))))


(defn -main [& args]
  (if (empty? args)
    (println "Usage: ross <FEED_LIST_FILE>")
    (doall (map process-url
                (parse-url-list
                  (slurp (first args)))))))
