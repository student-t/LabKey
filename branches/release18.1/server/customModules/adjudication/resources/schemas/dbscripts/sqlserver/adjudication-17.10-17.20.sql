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

/* adjudication-17.10-17.11.sql */

CREATE TABLE adjudication.CaseDocuments
(
  RowId INT IDENTITY(1, 1) NOT NULL,

  Container ENTITYID NOT NULL,
  Created DATETIME,
  Modified DATETIME,
  CreatedBy INTEGER,
  ModifiedBy INTEGER,

  CaseId INT,
  DocumentName NVARCHAR(250),
  Document VARBINARY(MAX),

  CONSTRAINT PK_CaseDocuments PRIMARY KEY (RowId),
);