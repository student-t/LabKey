/*
 * Copyright (c) 2016 LabKey Corporation
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
ANIMID AS Id,
TEST_DAT,
DAY1ACTIVITY AS dayOneActivity,
DAY1EMOTIONALITY AS dayOneEmotionality,
DAY2ACTIVITY AS dayTwoActivity,
DAY2EMOTIONALITY AS dayTwoEmotionality,
VIGILANT AS vigilant,
GENTLE AS gentle,
CONFIDENT AS confident,
NERVOUS AS nervous,
OBJECTID as objectid,
DATE_TIME
FROM cnprcSrc.ZBIO_BEHAVIORAL_ASSESSMENT
WHERE TEST_DAT IS NOT NULL AND TEST_DAT > to_date('01-01-1900', 'DD-MM-YYYY');