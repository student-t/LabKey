/*
 * Copyright (c) 2016-2017 LabKey Corporation
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
package org.labkey.test.pages.ehr;

import org.labkey.test.Locator;
import org.labkey.test.components.Component;
import org.labkey.test.pages.LabKeyPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public abstract class BaseColonyOverviewPage extends LabKeyPage
{
    public BaseColonyOverviewPage(WebDriver driver)
    {
        super(driver);
    }

    protected void clickTab(String tab)
    {
        _ext4Helper.clickExt4Tab(tab);
    }

    protected WebElement getActiveTabPanel()
    {
        return Locator.tagWithClass("div", "x4-tabpanel-child").notHidden().findElement(getDriver());
    }

    protected abstract class OverviewTab extends Component
    {
        private final WebElement el;

        protected OverviewTab(WebElement el)
        {
            this.el = el;
        }

        @Override
        public final WebElement getComponentElement()
        {
            return el;
        }
    }
}