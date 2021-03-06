/*
 * Copyright (c) 2011 LabKey Corporation
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

-- Create schema, tables, indexes, and constraints used for SkylineToolsStore module here
-- All SQL VIEW definitions should be created in skylinetoolsstore-create.sql and dropped in skylinetoolsstore-drop.sql

CREATE TABLE skylinetoolsstore.Rating
(
    -- standard fields
    _ts TIMESTAMP DEFAULT now(),
    RowId SERIAL,
    CreatedBy USERID,
    Created TIMESTAMP,
    Modified TIMESTAMP,

    -- other fields
    Rating INT DEFAULT 1,
    ToolID INT NOT NULL,
    Review TEXT NULL,
    Title VARCHAR(60),

    CONSTRAINT PK_Rating PRIMARY KEY (RowId),
    CONSTRAINT FK_Rating_SkylineTool FOREIGN KEY (ToolID) REFERENCES skylinetoolssotre.SkylineTool(RowId)
);
