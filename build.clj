(require 'table2qb.core)
(ns table2qb.core)

(def prov-context
  (with-open [f (io/reader "metadata/prov_context.json")]
    (read-json f)))

;; consider table2qb process as one activity for now, but we could
;; use dct:hasPart and create sub-activities for each pipeline step
;; https://www.w3.org/2001/sw/wiki/PROV-FAQ#How_can_I_define_a_sub_activity.3F
(def prov-activity
  {
   "@id" (or (environ/env :build-url) "unknown-build"),
   "@type" "activity",
   "startedAtTime" (str (java.time.LocalDateTime/now)),
   "label" (or (environ/env :job-name) "unknown-job"),
   })


(defn components-pipeline-prov [infile outdir]
   (components-pipeline infile outdir)
   (let [outfiles ((fn [f] [(str f ".csv") (str f ".json")]) "components")]
     [[infile] (map (fn [f] (clojure.string/join "/" [outdir f])) outfiles)]))

(defn codelist-pipeline-prov [infile outdir name path]
  (codelist-pipeline infile outdir name path)
  (let [outfiles ((fn [f] [(str f ".csv") (str f ".json")]) path)]
    [[infile] (map (fn [f] (clojure.string/join "/" [outdir f])) outfiles)]))

(defn data-pipeline-prov [infile outdir name path]
  (data-pipeline infile outdir name path)
  (let
      [outfiles (concat
                 (flatten (map (fn [f] [(str f ".csv") (str f ".json")])
                              ["component-specifications" "observations"]))
                 ["used-codes-codelistpath.json",
                  "used-codes-codes.json"])]
    [[infile] (map (fn [f] (clojure.string/join "/" [outdir f])) outfiles)]))

(def inout
  (reduce (fn [res1 res2] [(concat (first res1) (first res2)) (concat (first (rest res1)) (first (rest res2)))])
          [(components-pipeline-prov "metadata/components.csv" "out")
           (codelist-pipeline-prov "out/prep/countries.csv" "out" "Countries" "countries")
           (data-pipeline-prov "out/prep/CN8_Non-EU_cod_2012-2016.csv" "out" "HMRC Overseas Trade by CN8" "hmrc-ots-cn8")
           ]))

(def prov-sources
  (map
   (fn [f]
     {"@id" (str (or (environ/env :build-url) "unknown-build") "artifact/" f),
      "@type" "entity",
      "label" f,
      "wasUsedBy" (get prov-activity "@id")})
   (first inout)))

(def prov-outputs
  (map
   (fn [f]
     {"@id" (str (or (environ/env :build-url) "unknown-build") "artifact/" f),
      "@type" "entity",
      "label" f,
      "wasGeneratedBy" (get prov-activity "@id")})
   (first (rest inout))))

(with-open [f (io/writer "out/prov.jsonld")]
  (write-json f {"@context" prov-context,
                 "@graph" (cons (assoc prov-activity "endedAtTime" (str (java.time.LocalDateTime/now)))
                           (concat prov-sources prov-outputs))
                 }))
