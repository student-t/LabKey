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

/* oconnorexperiments-14.20-14.21.sql */

ALTER TABLE OConnorExperiments.ExperimentType ADD Enabled BIT DEFAULT 1;
GO
UPDATE OConnorExperiments.ExperimentType SET Enabled = 1;
ALTER TABLE OConnorExperiments.ExperimentType ALTER COLUMN Enabled BIT NOT NULL;