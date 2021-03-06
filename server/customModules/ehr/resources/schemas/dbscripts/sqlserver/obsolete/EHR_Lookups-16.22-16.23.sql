/*
 * Copyright (c) 2015-2017 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */

CREATE TABLE ehr_lookups.calendar (
    TargetDateTime DATETIME NOT NULL,
    TargetDate DATE NOT NULL,
    DayAfter DATE NOT NULL,
    Month INT NOT NULL,
    Day int NOT NULL,
    Year int NOT NULL
);

-- Populate via Java code instead of hard-coding thousands of date rows
EXEC core.executeJavaUpgradeCode 'populateCalendar';