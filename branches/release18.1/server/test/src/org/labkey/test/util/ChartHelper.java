/*
 * Copyright (c) 2013-2017 LabKey Corporation
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
package org.labkey.test.util;

import org.labkey.test.Locator;
import org.labkey.test.WebDriverWrapper;

import java.util.Map;

public class ChartHelper
{
    protected WebDriverWrapper _test;

    public ChartHelper(WebDriverWrapper test)
    {
        _test = test;
    }

    /**
     * on a page with a drt, clicks the (row)th edit button and set the named
     *
     */
    public void editDrtRow(int row, Map<String, String> nameAndValue)
    {
        DataRegionTable.DataRegion(_test.getDriver()).find().updateRow(row, nameAndValue);
    }
}