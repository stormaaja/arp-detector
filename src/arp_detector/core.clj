(ns arp-detector.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.pprint :refer [print-table]]
            [clojure.walk :refer [stringify-keys]]
            )
  (:gen-class))

(def ip-r #"(?:\d{1,3}\.){3}\d{1,3}")

(def not-empty? (comp not empty?))

(def not-nil? (comp not nil?))

(defn parse-arp-row [r]
  (let [splitted (string/split r #" ")]
    {:reverse-name (first splitted)
     :ip (re-find ip-r (second splitted))
     :mac (nth splitted 3)
     :interface (nth splitted 6)}))

(defn parse-arp-table [t]
  (map parse-arp-row (string/split t #"\n")))

(defn get-arp-table []
  (if-let [out (:out (shell/sh "arp" "-a"))]
    (parse-arp-table out)
    '()))

(defn prn-arp [arp]
  (print-table (stringify-keys arp)))

(defn to-mac-ip-map [arp-item]
  {(:mac arp-item) (:ip arp-item)})

(defn to-mac-ip [arp]
  (reduce #(merge (to-mac-ip-map %2) %1) {} arp))

(defn changes-to-str [mac-ip arp-item]
  (let [mac (:mac arp-item)]
    (when-let [ip (get mac-ip (:mac arp-item))]
      (when (not= ip (:ip arp-item))
        (format "%s %s -> %s %s" mac ip mac (:ip arp-item))))))

(defn conj-if [c v]
  (if (not-nil? v) (conj c v) c))

(defn diff [arp1 arp2]
  (let [mac-ip (to-mac-ip arp1)]
    (reduce #(conj-if %1 (changes-to-str mac-ip %2)) '() arp2)))

(defn start-main-loop [arp on-change]
  (loop []
    (Thread/sleep 1000)
    (let [changes (diff arp (get-arp-table))]
      (when (not-empty? changes)
        (on-change changes)))
    (recur)))

(defn -main [& args]
  (let [arp (get-arp-table)]
    (if (empty? arp)
      (prn "ARP table is empty. Is your network up?")
      (do (prn "Original ARP Table:")
          (prn-arp arp)
          (start-main-loop arp #(prn "Changes detected: " %))))))

