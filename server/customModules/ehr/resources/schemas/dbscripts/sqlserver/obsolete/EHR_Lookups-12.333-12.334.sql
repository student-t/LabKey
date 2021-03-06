/*
 * Copyright (c) 2013-2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
ALTER TABLE ehr_lookups.ageclass ADD gender varchar(100);

ALTER TABLE ehr_lookups.cage_positions ADD sort_order int;
ALTER TABLE ehr_lookups.rooms ADD sort_order int;
GO

UPDATE ehr_lookups.cage_positions SET sort_order=1 WHERE cage='A1';
UPDATE ehr_lookups.cage_positions SET sort_order=2 WHERE cage='A2';
UPDATE ehr_lookups.cage_positions SET sort_order=3 WHERE cage='A3';
UPDATE ehr_lookups.cage_positions SET sort_order=4 WHERE cage='A4';
UPDATE ehr_lookups.cage_positions SET sort_order=5 WHERE cage='A5';
UPDATE ehr_lookups.cage_positions SET sort_order=6 WHERE cage='A6';
UPDATE ehr_lookups.cage_positions SET sort_order=7 WHERE cage='A7';
UPDATE ehr_lookups.cage_positions SET sort_order=8 WHERE cage='A8';
UPDATE ehr_lookups.cage_positions SET sort_order=9 WHERE cage='A9';
UPDATE ehr_lookups.cage_positions SET sort_order=10 WHERE cage='A10';
UPDATE ehr_lookups.cage_positions SET sort_order=11 WHERE cage='A11';
UPDATE ehr_lookups.cage_positions SET sort_order=12 WHERE cage='A12';
UPDATE ehr_lookups.cage_positions SET sort_order=13 WHERE cage='A13';
UPDATE ehr_lookups.cage_positions SET sort_order=14 WHERE cage='A14';
UPDATE ehr_lookups.cage_positions SET sort_order=15 WHERE cage='A15';
UPDATE ehr_lookups.cage_positions SET sort_order=16 WHERE cage='A16';
UPDATE ehr_lookups.cage_positions SET sort_order=17 WHERE cage='A17';
UPDATE ehr_lookups.cage_positions SET sort_order=18 WHERE cage='A18';
UPDATE ehr_lookups.cage_positions SET sort_order=19 WHERE cage='B1';
UPDATE ehr_lookups.cage_positions SET sort_order=20 WHERE cage='B2';
UPDATE ehr_lookups.cage_positions SET sort_order=21 WHERE cage='B3';
UPDATE ehr_lookups.cage_positions SET sort_order=22 WHERE cage='B4';
UPDATE ehr_lookups.cage_positions SET sort_order=23 WHERE cage='B5';
UPDATE ehr_lookups.cage_positions SET sort_order=24 WHERE cage='B6';
UPDATE ehr_lookups.cage_positions SET sort_order=25 WHERE cage='B7';
UPDATE ehr_lookups.cage_positions SET sort_order=26 WHERE cage='B8';
UPDATE ehr_lookups.cage_positions SET sort_order=27 WHERE cage='B9';
UPDATE ehr_lookups.cage_positions SET sort_order=28 WHERE cage='B10';
UPDATE ehr_lookups.cage_positions SET sort_order=29 WHERE cage='B11';
UPDATE ehr_lookups.cage_positions SET sort_order=30 WHERE cage='B12';
UPDATE ehr_lookups.cage_positions SET sort_order=31 WHERE cage='B13';
UPDATE ehr_lookups.cage_positions SET sort_order=32 WHERE cage='B14';
UPDATE ehr_lookups.cage_positions SET sort_order=33 WHERE cage='B15';
UPDATE ehr_lookups.cage_positions SET sort_order=34 WHERE cage='B16';
UPDATE ehr_lookups.cage_positions SET sort_order=35 WHERE cage='C1';
UPDATE ehr_lookups.cage_positions SET sort_order=36 WHERE cage='C2';
UPDATE ehr_lookups.cage_positions SET sort_order=37 WHERE cage='C3';
UPDATE ehr_lookups.cage_positions SET sort_order=38 WHERE cage='C4';
UPDATE ehr_lookups.cage_positions SET sort_order=39 WHERE cage='C5';
UPDATE ehr_lookups.cage_positions SET sort_order=40 WHERE cage='C6';
UPDATE ehr_lookups.cage_positions SET sort_order=41 WHERE cage='C7';
UPDATE ehr_lookups.cage_positions SET sort_order=42 WHERE cage='C8';
UPDATE ehr_lookups.cage_positions SET sort_order=43 WHERE cage='C9';
UPDATE ehr_lookups.cage_positions SET sort_order=44 WHERE cage='C10';
UPDATE ehr_lookups.cage_positions SET sort_order=45 WHERE cage='C11';
UPDATE ehr_lookups.cage_positions SET sort_order=46 WHERE cage='C12';
UPDATE ehr_lookups.cage_positions SET sort_order=47 WHERE cage='C13';
UPDATE ehr_lookups.cage_positions SET sort_order=48 WHERE cage='C14';
UPDATE ehr_lookups.cage_positions SET sort_order=49 WHERE cage='C15';
UPDATE ehr_lookups.cage_positions SET sort_order=50 WHERE cage='C16';
UPDATE ehr_lookups.cage_positions SET sort_order=51 WHERE cage='C17';
UPDATE ehr_lookups.cage_positions SET sort_order=52 WHERE cage='C18';
UPDATE ehr_lookups.cage_positions SET sort_order=53 WHERE cage='D1';
UPDATE ehr_lookups.cage_positions SET sort_order=54 WHERE cage='D2';
UPDATE ehr_lookups.cage_positions SET sort_order=55 WHERE cage='D3';
UPDATE ehr_lookups.cage_positions SET sort_order=56 WHERE cage='D4';
UPDATE ehr_lookups.cage_positions SET sort_order=57 WHERE cage='D5';
UPDATE ehr_lookups.cage_positions SET sort_order=58 WHERE cage='D6';
UPDATE ehr_lookups.cage_positions SET sort_order=59 WHERE cage='D7';
UPDATE ehr_lookups.cage_positions SET sort_order=60 WHERE cage='D8';
UPDATE ehr_lookups.cage_positions SET sort_order=61 WHERE cage='D9';
UPDATE ehr_lookups.cage_positions SET sort_order=62 WHERE cage='D10';
UPDATE ehr_lookups.cage_positions SET sort_order=63 WHERE cage='D11';
UPDATE ehr_lookups.cage_positions SET sort_order=64 WHERE cage='D12';
UPDATE ehr_lookups.cage_positions SET sort_order=65 WHERE cage='D13';
UPDATE ehr_lookups.cage_positions SET sort_order=66 WHERE cage='D14';
UPDATE ehr_lookups.cage_positions SET sort_order=67 WHERE cage='D15';
UPDATE ehr_lookups.cage_positions SET sort_order=68 WHERE cage='D16';
UPDATE ehr_lookups.cage_positions SET sort_order=69 WHERE cage='E1';
UPDATE ehr_lookups.cage_positions SET sort_order=70 WHERE cage='E2';
UPDATE ehr_lookups.cage_positions SET sort_order=71 WHERE cage='E3';
UPDATE ehr_lookups.cage_positions SET sort_order=72 WHERE cage='E4';
UPDATE ehr_lookups.cage_positions SET sort_order=73 WHERE cage='E5';
UPDATE ehr_lookups.cage_positions SET sort_order=74 WHERE cage='E6';
UPDATE ehr_lookups.cage_positions SET sort_order=75 WHERE cage='E7';
UPDATE ehr_lookups.cage_positions SET sort_order=76 WHERE cage='E8';
UPDATE ehr_lookups.cage_positions SET sort_order=77 WHERE cage='E9';
UPDATE ehr_lookups.cage_positions SET sort_order=78 WHERE cage='E10';
UPDATE ehr_lookups.cage_positions SET sort_order=79 WHERE cage='E11';
UPDATE ehr_lookups.cage_positions SET sort_order=80 WHERE cage='E12';
UPDATE ehr_lookups.cage_positions SET sort_order=81 WHERE cage='E13';
UPDATE ehr_lookups.cage_positions SET sort_order=82 WHERE cage='E14';
UPDATE ehr_lookups.cage_positions SET sort_order=83 WHERE cage='F1';
UPDATE ehr_lookups.cage_positions SET sort_order=84 WHERE cage='F2';
UPDATE ehr_lookups.cage_positions SET sort_order=85 WHERE cage='F3';
UPDATE ehr_lookups.cage_positions SET sort_order=86 WHERE cage='F4';
UPDATE ehr_lookups.cage_positions SET sort_order=87 WHERE cage='F5';
UPDATE ehr_lookups.cage_positions SET sort_order=88 WHERE cage='F6';
UPDATE ehr_lookups.cage_positions SET sort_order=89 WHERE cage='F7';
UPDATE ehr_lookups.cage_positions SET sort_order=90 WHERE cage='F8';
UPDATE ehr_lookups.cage_positions SET sort_order=91 WHERE cage='F9';
UPDATE ehr_lookups.cage_positions SET sort_order=92 WHERE cage='F10';
UPDATE ehr_lookups.cage_positions SET sort_order=93 WHERE cage='F11';
UPDATE ehr_lookups.cage_positions SET sort_order=94 WHERE cage='F12';
UPDATE ehr_lookups.cage_positions SET sort_order=95 WHERE cage='F13';
UPDATE ehr_lookups.cage_positions SET sort_order=96 WHERE cage='F14';
UPDATE ehr_lookups.cage_positions SET sort_order=97 WHERE cage='G1';
UPDATE ehr_lookups.cage_positions SET sort_order=98 WHERE cage='G2';
UPDATE ehr_lookups.cage_positions SET sort_order=99 WHERE cage='G3';
UPDATE ehr_lookups.cage_positions SET sort_order=100 WHERE cage='G4';
UPDATE ehr_lookups.cage_positions SET sort_order=101 WHERE cage='G5';
UPDATE ehr_lookups.cage_positions SET sort_order=102 WHERE cage='G6';
UPDATE ehr_lookups.cage_positions SET sort_order=103 WHERE cage='G7';
UPDATE ehr_lookups.cage_positions SET sort_order=104 WHERE cage='G8';
UPDATE ehr_lookups.cage_positions SET sort_order=105 WHERE cage='G9';
UPDATE ehr_lookups.cage_positions SET sort_order=106 WHERE cage='G10';
UPDATE ehr_lookups.cage_positions SET sort_order=107 WHERE cage='G11';
UPDATE ehr_lookups.cage_positions SET sort_order=108 WHERE cage='H1';
UPDATE ehr_lookups.cage_positions SET sort_order=109 WHERE cage='H2';
UPDATE ehr_lookups.cage_positions SET sort_order=110 WHERE cage='H3';
UPDATE ehr_lookups.cage_positions SET sort_order=111 WHERE cage='H4';
UPDATE ehr_lookups.cage_positions SET sort_order=112 WHERE cage='H5';
UPDATE ehr_lookups.cage_positions SET sort_order=113 WHERE cage='H6';
UPDATE ehr_lookups.cage_positions SET sort_order=114 WHERE cage='H7';
UPDATE ehr_lookups.cage_positions SET sort_order=115 WHERE cage='H8';
UPDATE ehr_lookups.cage_positions SET sort_order=116 WHERE cage='H9';
UPDATE ehr_lookups.cage_positions SET sort_order=117 WHERE cage='H10';
UPDATE ehr_lookups.cage_positions SET sort_order=118 WHERE cage='I1';
UPDATE ehr_lookups.cage_positions SET sort_order=119 WHERE cage='I2';
UPDATE ehr_lookups.cage_positions SET sort_order=120 WHERE cage='I3';
UPDATE ehr_lookups.cage_positions SET sort_order=121 WHERE cage='I4';
UPDATE ehr_lookups.cage_positions SET sort_order=122 WHERE cage='I5';
UPDATE ehr_lookups.cage_positions SET sort_order=123 WHERE cage='I6';
UPDATE ehr_lookups.cage_positions SET sort_order=124 WHERE cage='I7';
UPDATE ehr_lookups.cage_positions SET sort_order=125 WHERE cage='I8';
UPDATE ehr_lookups.cage_positions SET sort_order=126 WHERE cage='I9';
UPDATE ehr_lookups.cage_positions SET sort_order=127 WHERE cage='I10';
UPDATE ehr_lookups.cage_positions SET sort_order=128 WHERE cage='M1';
UPDATE ehr_lookups.cage_positions SET sort_order=129 WHERE cage='M2';
UPDATE ehr_lookups.cage_positions SET sort_order=130 WHERE cage='M3';
UPDATE ehr_lookups.cage_positions SET sort_order=131 WHERE cage='M4';
UPDATE ehr_lookups.cage_positions SET sort_order=132 WHERE cage='M5';
UPDATE ehr_lookups.cage_positions SET sort_order=133 WHERE cage='M6';
UPDATE ehr_lookups.cage_positions SET sort_order=134 WHERE cage='M7';
UPDATE ehr_lookups.cage_positions SET sort_order=135 WHERE cage='M8';
UPDATE ehr_lookups.cage_positions SET sort_order=136 WHERE cage='M9';
UPDATE ehr_lookups.cage_positions SET sort_order=137 WHERE cage='S1';
UPDATE ehr_lookups.cage_positions SET sort_order=138 WHERE cage='S2';
UPDATE ehr_lookups.cage_positions SET sort_order=139 WHERE cage='S3';
UPDATE ehr_lookups.cage_positions SET sort_order=140 WHERE cage='S4';
