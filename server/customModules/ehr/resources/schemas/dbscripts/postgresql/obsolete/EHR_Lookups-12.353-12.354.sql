/*
 * Copyright (c) 2013-2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
ALTER TABLE ehr_lookups.cage_type ADD supportsTunnel bool;
UPDATE ehr_lookups.cage_type SET supportsTunnel = false;
UPDATE ehr_lookups.cage_type SET supportsTunnel = true WHERE cagetype LIKE 'Tunnel - %';
ALTER TABLE ehr_lookups.cage_type DROP COLUMN CageCapacity;

ALTER TABLE ehr_lookups.cage ADD hasTunnel bool;