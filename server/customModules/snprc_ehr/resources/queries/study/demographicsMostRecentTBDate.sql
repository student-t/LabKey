/*
 * Copyright (c) 2013-2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

 --NOTE: this is joined to demographics such that animals never tested for TB
 --will still get a value for MonthsSinceLastTB
 --Animals never tested will use birthdate as last tb date. tjh
select
  d.Id,
  T2.lastDate as MostRecentTBDate,
  CASE WHEN d.calculated_status = 'Alive' THEN age_in_months(COALESCE (T2.lastDate, d.birth), now())
  ELSE NULL
  END AS MonthsSinceLastTB,

  (6 - age_in_months(COALESCE (T2.lastDate, d.birth), now())) AS MonthsUntilDue,

  (select group_concat(tb.site) as eyeTested FROM study.tb tb WHERE tb.id = d.id and tb.date = T2.lastdate) as eyeTested,
  null as "24H",
  null as "48H",
  null as "72H"

from study.demographics d

LEFT JOIN (select tb.id, max(tb.date) as lastDate
    from study.tb as tb
    inner join study.demographics as d on d.id = tb.id and d.calculated_status = 'Alive'
    WHERE tb.qcstate.publicdata = true group by tb.id) T2
ON (d.id = t2.id)





