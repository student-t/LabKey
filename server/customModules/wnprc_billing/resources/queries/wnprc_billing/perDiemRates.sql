/*
 * Copyright (c) 2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

  SELECT
    pdt.id,
    pdt.adate as date,
--       pdt.project,
    pdt.account as debitedAccount,
    cr1.unitCost,
    pdt.quantity,
    cr1.chargeId as chargeId,
    ci1.name as item,
    ci1.category as category,
    pdt.quantity * cr1.unitCost AS totalCost,
    cr1.serviceCode AS serviceCenter,
    NULL AS isMiscCharge
  FROM wnprc_billing.perDiemWithTierRates pdt
  LEFT JOIN ehr_billing.chargeRates cr1 ON (
    CAST(pdt.adate AS DATE) >= CAST(cr1.startDate AS DATE) AND
    (CAST(pdt.adate AS DATE) <= cr1.enddate OR cr1.enddate IS NULL) AND
    cr1.description = 'Per diems')
  LEFT JOIN ehr_billing.chargeableItems ci1 ON ci1.name = cr1.description