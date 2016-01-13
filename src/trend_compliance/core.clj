(ns trend-compliance.core
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.string :as st]
            [clojure-csv.core :as csv]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:gen-class))

(def log-map (atom {}))
(def user-map (atom {}))
(def blank-line-regex #"^\s*$")
(def date-regex #"(?i)\w{3} \w{3} \d{2} \d{2}:\d{2}:\d{2} \d{4}")
(def mac-regex #"(?i)Acct-Session-Id\s\=\s\"(.+)\"")
(def ip-regex #"(?i)Framed-IP-Address\s\=\s(.+)")
(def username-regex #"(?i)User-Name\s\=\s\"(.+)\"")
(def timestamp-regex #"(?i)Timestamp\s\=\s\d+")

(defn create-map [item]
  (let [[ip cp-name domain mac-ad off-scan-status ping prod-name platform os-server version patt-file scan-eng prev-pol cp-des rem-install] item]
    {:ip ip
     :off-scan-status off-scan-status
     :ping ping
     :platform platform}))

(defn trend-installed? [item]
  (if (re-matches #"(?i)not installed" (get item :off-scan-status)) true false))

(defn ping-successful? [item]
  (if (re-matches #".*(?i)ping successful" (get item :ping)) true false))

(defn windows? [item]
  (if (re-matches #"(?i)windows*" (get item :platform)) true false))

(defn uncompliant-map [item]
  (filter (every-pred windows? ping-successful? trend-installed?) item))

(defn find-uncompliant-ip [ip]
  (find @user-map ip))

(defn match-line [l]
  (let [tl (st/trim l)
        blank (re-matches blank-line-regex tl)
        date (re-matches date-regex tl)
        mac (re-matches mac-regex tl)
        ip (re-matches ip-regex tl)
        username (re-matches username-regex tl)
        timestamp (re-matches timestamp-regex tl)]
    (cond
      (seq date) (list :date date)
      (seq mac) (list :mac (re-find #"\S\S:\S\S:\S\S:\S\S:\S\S:\S\S" (second mac)))
      (seq ip) (list :ip (second ip))
      (seq username) (list :username (second username))
      (not (nil? timestamp)) (list :timestamp)
      :else (list))))

(defn create-user-map-from-file [f]
  (with-open [rdr (io/reader f)]
    (let [lines (line-seq rdr)]
      (loop [l (first lines)
             r (rest lines)
             u {}]
        (when l
          (let [match (match-line l)]
            (case (first match)
              :timestamp (do (swap! user-map assoc (:ip u) u)
                             (recur (first r) (rest r) {}))
              :skip (recur (first r) (rest r) u)
              (recur (first r) (rest r) (merge u (apply hash-map match))))))))))

(defn read-csv [f]
  (with-open [rdr (io/reader f)]
    (doall (csv/parse-csv rdr))))

(defn get-ip [item]
  (get item :ip))

(defn check-time-range [item]
  (let [date (:date (second item))
        parse-date (f/parse (f/formatter "EEE MMM dd HH:mm:ss yyyy") date)]
    (if (and (>= (t/hour parse-date) 20) (<= (t/hour parse-date) 22)) item "not between 20:00 - 22:00.")))

(defn -main [& args]
  (println "Files (<path/to/WIRED.csv> <path/to/WIRELSS.csv> <path/to/DETAIL>): ")
    (let [in (read-line)
          file-names (st/split in #" ")
          wired (first file-names)
          wireless (second file-names)
          detail (nth file-names 2)
          wired-ip-list (map get-ip (uncompliant-map (map create-map (read-csv wired))))
          wireless-ip-list (map get-ip (uncompliant-map (map create-map (read-csv wireless))))
          detail-user-map (create-user-map-from-file detail)
          find-ip-list (map find-uncompliant-ip wireless-ip-list)]
          ;; check-time-list (map check-time-range find-ip-list)]
      (println "=============== Wired List: ===============")
      (doall (map pp/pprint wired-ip-list))
      (println "=============== Wireless List: ===============")
      (doall (map pp/pprint wireless-ip-list))
      (println "=============== Wireless List More Detail: ===============")
      (pp/pprint (map check-time-range find-ip-list))))
      ;; (pp/pprint find-ip-list)))
