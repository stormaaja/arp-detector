(defproject arp-detector "0.1"
  :description "ARP changes detector"
  :url "https://github.com/stormaaja/arp-detector"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main ^:skip-aot arp-detector.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
