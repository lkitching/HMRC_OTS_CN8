(require 'table2qb.core)
(ns table2qb.core)
(components-pipeline "metadata/components.csv" "out")
(codelist-pipeline "metadata/flow-directions.csv" "out" "Flow directions" "flow-directions")
(codelist-pipeline "metadata/units.csv" "out" "Measurement units" "measurement-units")
(data-pipeline "in/CN8_Non-EU_cod_2012.csv" "out" "HMRC CN8" "hmrc-cn8")
