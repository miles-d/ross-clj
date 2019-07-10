(ns ross.core-test
  (:require [clojure.test :refer :all]
            [ross.core :refer [normalize-date-key format-entry simple-format-date]]))

(deftest normalize-date-key-test
  (testing "it uses any non-empty value of :published-date or :updated-date"
    (is (= (normalize-date-key {:published-date nil
                                :updated-date (java.util.Date. 119 0 1)})
           {:date (java.util.Date. 119 0 1)}))
    (is (= (normalize-date-key {:published-date (java.util.Date. 119 0 1)
                                :updated-date nil})
           {:date (java.util.Date. 119 0 1)}))))


(deftest format-entry-test
  (testing "it puts date, title and link on one line"
    (is (= "Some Feed | 2019-01-01 | Some Title | http://example.com"
           (format-entry {:date (java.util.Date. 119 0 1)
                          :link "http://example.com"
                          :feed-title "Some Feed"
                          :title "Some Title"})))))


(deftest simple-format-date-test
  (testing "it formats instance to yyyy-MM-dd"
    (is (= "2019-01-01"
           (simple-format-date (java.util.Date. 119 0 1))))))
