/*
 * Copyright (c) 2015 LabKey Corporation
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

 ALTER TABLE snprc_ehr.package_category DROP COLUMN name;

 ALTER TABLE snprc_ehr.package ADD Created DATETIME;
 ALTER TABLE snprc_ehr.package ADD CreatedBy USERID;
 ALTER TABLE snprc_ehr.package ADD Modified DATETIME;
 ALTER TABLE snprc_ehr.package ADD ModifiedBy USERID;

 ALTER TABLE snprc_ehr.package_category ADD Created DATETIME;
 ALTER TABLE snprc_ehr.package_category ADD CreatedBy USERID;
 ALTER TABLE snprc_ehr.package_category ADD Modified DATETIME;
 ALTER TABLE snprc_ehr.package_category ADD ModifiedBy USERID;

 ALTER TABLE snprc_ehr.package_category_junction ADD Created DATETIME;
 ALTER TABLE snprc_ehr.package_category_junction ADD CreatedBy USERID;
 ALTER TABLE snprc_ehr.package_category_junction ADD Modified DATETIME;
 ALTER TABLE snprc_ehr.package_category_junction ADD ModifiedBy USERID;