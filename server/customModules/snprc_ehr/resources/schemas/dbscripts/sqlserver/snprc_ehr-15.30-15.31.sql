/*
 * Copyright (c) 2015-2016 LabKey Corporation
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


CREATE PROCEDURE snprc_ehr.handleUpgrade AS
    BEGIN
    IF NOT EXISTS(SELECT column_name
            FROM information_schema.columns
            WHERE table_name='package' and table_schema='snprc_ehr' and column_name='objectid')
        BEGIN
        -- Run variants of scripts from trunk

        ALTER TABLE snprc_ehr.package ADD objectid nvarchar(4000);
        ALTER TABLE snprc_ehr.package_category ADD objectid nvarchar(4000);
        ALTER TABLE snprc_ehr.package_category_junction ADD objectid nvarchar(4000);
        END
    END;

GO

EXEC snprc_ehr.handleUpgrade
GO

DROP PROCEDURE snprc_ehr.handleUpgrade
GO