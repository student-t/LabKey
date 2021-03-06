/*
 * Copyright (c) 2014-2017 LabKey Corporation
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

/* genotyping-14.20-14.21.sql */

/* Consider: setting up these fields to not null */

ALTER TABLE genotyping.Species ADD FullName VARCHAR(45);
GO
ALTER TABLE genotyping.Species ADD CONSTRAINT Unique_Species_FullName UNIQUE (FullName);
UPDATE genotyping.Species SET Name='mamu', FullName='Macaca mulatta (rhesus)' WHERE Name = 'rhesus macaques';

ALTER TABLE genotyping.Species ALTER COLUMN Name VARCHAR(10);
GO
ALTER TABLE genotyping.Species ADD CONSTRAINT Unique_Species_Name UNIQUE (Name);
INSERT INTO genotyping.Species (Name, FullName) VALUES ('mane', 'Macaca nemestrina (pigtail)');
INSERT INTO genotyping.Species (Name, FullName) VALUES ('mafa', 'Macaca fascicularis (cynomolgus)');