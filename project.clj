(defproject stallmannotes "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
                 [org.clojure/clojure "1.10.1"]
                 [adamwynne/feedparser-clj "0.5.2"]
                 [twitter-api "1.8.0"]
                 [hickory "0.7.1"]
                 [environ "1.2.0"]
                 [http-kit "2.5.1"]
                 [compojure "1.6.2"]                 ]
  :plugins [[lein-environ "1.2.0"]]
  :main ^:skip-aot stallmannotes.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot      :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
