/*
 * Copyright (c) 2015-2017 LabKey Corporation
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
package org.labkey.test;

import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.labkey.api.util.FileUtil;
import org.labkey.remoteapi.Connection;
import org.labkey.test.components.html.BootstrapMenu;
import org.labkey.test.components.html.SiteNavBar;
import org.labkey.test.components.labkey.LabKeyAlert;
import org.labkey.test.pages.admin.FolderManagementPage;
import org.labkey.test.pages.core.admin.ProjectSettingsPage;
import org.labkey.test.pages.list.BeginPage;
import org.labkey.test.pages.reports.ManageViewsPage;
import org.labkey.test.selenium.EphemeralWebElement;
import org.labkey.test.util.DataRegionTable;
import org.labkey.test.util.ExperimentalFeaturesHelper;
import org.labkey.test.util.Ext4Helper;
import org.labkey.test.util.ExtHelper;
import org.labkey.test.util.LabKeyExpectedConditions;
import org.labkey.test.util.LogMethod;
import org.labkey.test.util.LoggedParam;
import org.labkey.test.util.PasswordUtil;
import org.labkey.test.util.RelativeUrl;
import org.labkey.test.util.TestLogger;
import org.labkey.test.util.TextSearcher;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.labkey.test.TestProperties.isScriptCheckEnabled;
import static org.labkey.test.WebTestHelper.stripContextPath;
import static org.labkey.test.components.html.RadioButton.RadioButton;
import static org.openqa.selenium.chrome.ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY;
import static org.openqa.selenium.chrome.ChromeDriverService.CHROME_DRIVER_VERBOSE_LOG_PROPERTY;

public abstract class WebDriverWrapper implements WrapsDriver
{
    public final static int WAIT_FOR_JAVASCRIPT = 10000;
    public final static int WAIT_FOR_PAGE = 30000;

    protected boolean _testTimeout = false;

    public int defaultWaitForPage = WAIT_FOR_PAGE;
    public int longWaitForPage = defaultWaitForPage * 5;

    public ExtHelper _extHelper;
    public Ext4Helper _ext4Helper;

    private Stack<String> _locationStack = new Stack<>();
    private String _savedLocation = null;

    public WebDriverWrapper()
    {
        _extHelper = new ExtHelper(this);
        _ext4Helper = new Ext4Helper(this);
    }

    @NotNull
    public final WebDriver getDriver()
    {
        WebDriver driver = getWrappedDriver();
        while (driver instanceof WrapsDriver)
            driver = ((WrapsDriver) driver).getWrappedDriver();
        if (driver == null)
            throw new NullPointerException("WebDriver has not been initialized yet");
        return driver;
    }

    protected Pair<WebDriver, DriverService> createNewWebDriver(BrowserType browserType, File downloadDir)
    {
        return createNewWebDriver(new ImmutablePair<>(null, null), browserType, downloadDir);
    }

    protected Pair<WebDriver, DriverService> createNewWebDriver(@NotNull Pair<WebDriver, DriverService> oldDriverAndService, BrowserType browserType, File downloadDir)
    {
        WebDriver oldWebDriver = oldDriverAndService.getLeft();
        WebDriver newWebDriver = null;
        DriverService oldDriverService = oldDriverAndService.getRight();
        DriverService newDriverService = null;

        switch (browserType)
        {
            case REMOTE: //experimental
            {
                try
                {
                    newWebDriver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), DesiredCapabilities.chrome());
                }
                catch (MalformedURLException e)
                {
                    throw new RuntimeException(e);
                }
                break;
            }
            case IE: //experimental
            {
                if (oldWebDriver != null && !(oldWebDriver instanceof InternetExplorerDriver))
                {
                    oldWebDriver.quit();
                    oldWebDriver = null;
                    if (oldDriverService != null && oldDriverService.isRunning())
                        oldDriverService.stop();
                }
                if (oldWebDriver == null)
                {
                    newWebDriver = new InternetExplorerDriver();
                }
                break;
            }
            case CHROME:
            {
                if (oldWebDriver != null && !(oldWebDriver instanceof ChromeDriver))
                {
                    oldWebDriver.quit();
                    oldWebDriver = null;
                    if (oldDriverService != null && oldDriverService.isRunning())
                        oldDriverService.stop();
                }
                if (oldWebDriver == null)
                {
                    TestProperties.ensureChromedriverExeProperty();
                    if (downloadDir.getParentFile().exists() || downloadDir.getParentFile().mkdirs())
                    {
                        String logFileName = new SimpleDateFormat("'chromedriver_'HHmmss'.log'").format(new Date());
                        final String logPath = new File(downloadDir.getParentFile(), logFileName).getAbsolutePath();
                        log("Saving chromedriver log to: " + logPath);
                        System.setProperty(CHROME_DRIVER_VERBOSE_LOG_PROPERTY, "true");
                        System.setProperty(CHROME_DRIVER_LOG_PROPERTY, logPath);
                    }
                    else
                    {
                        log("Failed to create directory for chromedriver log: " + downloadDir.getParentFile().getAbsolutePath());
                    }
                    ChromeOptions options = new ChromeOptions();
                    Dictionary<String, Object> prefs = new Hashtable<>();

                    prefs.put("download.prompt_for_download", "false");
                    prefs.put("download.default_directory", downloadDir.getAbsolutePath());
                    prefs.put("profile.content_settings.pattern_pairs.*.multiple-automatic-downloads", 1); // Turns off multiple download warning
                    prefs.put("security.warn_submit_insecure", "false");
                    prefs.put("pdfjs.disabled", true); // Download PDFs
                    prefs.put("credentials_enable_service", false);
                    prefs.put("profile.password_manager_enabled", false);
                    options.setExperimentalOption("prefs", prefs);
                    options.setExperimentalOption("detach", true); // Leaves browser window open after stopping the driver service
                    options.setExperimentalOption("excludeSwitches",
                            Collections.singletonList("enable-automation"));  // Removes the "Chrome is being controlled by automated test software". banner.
                    options.addArguments("test-type"); // Suppress '--ignore-certificate-errors' warning
                    options.addArguments("disable-xss-auditor");
                    options.addArguments("ignore-certificate-errors");
                    options.addArguments("disable-extensions"); // Should avoid the "Disable Developer Extensions" dialog.

                    DesiredCapabilities capabilities = DesiredCapabilities.chrome();
                    capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                    newDriverService = ChromeDriverService.createDefaultService();
                    newWebDriver = new ChromeDriver((ChromeDriverService) newDriverService, capabilities);
                }
                break;
            }
            case FIREFOX:
            {
                if (oldWebDriver != null && !(oldWebDriver instanceof FirefoxDriver))
                {
                    oldWebDriver.quit();
                    oldWebDriver = null;
                    if (oldDriverService != null && oldDriverService.isRunning())
                        oldDriverService.stop();
                }
                if (oldWebDriver == null)
                {
                    final FirefoxProfile profile = new FirefoxProfile();
                    profile.setPreference("app.update.auto", false);
                    profile.setPreference("extensions.update.autoUpdate", false);
                    profile.setPreference("extensions.update.enabled", false);
                    profile.setPreference("dom.max_script_run_time", 0);
                    profile.setPreference("dom.max_chrome_script_run_time", 0);

                    profile.setPreference("browser.download.folderList", 2);
                    profile.setPreference("browser.download.downloadDir", downloadDir.getAbsolutePath());
                    profile.setPreference("browser.download.dir", downloadDir.getAbsolutePath());
                    profile.setPreference("browser.download.manager.showAlertOnComplete", false);
                    profile.setPreference("browser.download.manager.showWhenStarting", false);
                    profile.setPreference("browser.helperApps.alwaysAsk.force", false);
                    profile.setPreference("browser.helperApps.neverAsk.saveToDisk",
                            "application/vnd.ms-excel," + // .xls
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet," + // .xlsx
                                    "application/octet-stream," +
                                    "application/pdf," +
                                    "application/zip," +
                                    "application/x-gzip," +
                                    "application/x-zip-compressed," +
                                    "application/xml," +
                                    "image/png," +
                                    "image/tiff," +
                                    "text/html," +
                                    "text/plain," +
                                    "text/xml," +
                                    "text/x-script.perl," +
                                    "text/tab-separated-values," +
                                    "text/csv");
                    profile.setPreference("pdfjs.disabled", true); // disable Firefox's built-in PDF viewer

                    profile.setPreference("browser.ssl_override_behavior", 0);

                    profile.setAcceptUntrustedCertificates(true);
                    profile.setAssumeUntrustedCertificateIssuer(false);
                    profile.setEnableNativeEvents(useNativeEvents());


                    DesiredCapabilities capabilities = DesiredCapabilities.firefox();
                    capabilities.setCapability(FirefoxDriver.PROFILE, profile);

                    String browserPath = System.getProperty("selenium.firefox.binary", "");
                    if (browserPath.length() > 0)
                    {
                        FirefoxBinary binary = new FirefoxBinary(new File(browserPath));
                        capabilities.setCapability(FirefoxDriver.BINARY, binary);
                    }
                    try
                    {
                        newWebDriver = new FirefoxDriver(capabilities);
                    }
                    catch (WebDriverException rethrow)
                    {
                        throw new WebDriverException("ERROR: Failed to initialize FirefoxDriver. Ensure that you are not using a version newer than 45.", rethrow);
                    }
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Browser not yet implemented: " + browserType);
        }

        if (newWebDriver != null)
        {
            Capabilities caps = ((HasCapabilities) newWebDriver).getCapabilities();
            String browserName = caps.getBrowserName();
            String browserVersion = caps.getVersion();
            log("Browser: " + browserName + " " + browserVersion);
            return new ImmutablePair<>(newWebDriver, newDriverService);
        }
        else
        {
            return oldDriverAndService;
        }
    }

    protected boolean useNativeEvents()
    {
        return false;
    }

    public Object executeScript(String script, Object... arguments)
    {
        return ((JavascriptExecutor) getDriver()).executeScript(script, arguments);
    }

    public Object executeAsyncScript(String script, Object... arguments)
    {
        script = "var callback = arguments[arguments.length - 1];\n" + script; // See WebDriver documentation
        return ((JavascriptExecutor) getDriver()).executeAsyncScript(script, arguments);
    }

    @LogMethod(quiet = true)
    public void resumeJsErrorChecker()
    {
        // Turn on server side logging of client errors.
        if (isScriptCheckEnabled())
        {
            Connection cn = createDefaultConnection(false);
            ExperimentalFeaturesHelper.setExperimentalFeature(cn, "javascriptErrorServerLogging", true);
        }
    }

    @LogMethod(quiet = true)
    public void pauseJsErrorChecker()
    {
        // Turn off server side logging of client errors.
        if (isScriptCheckEnabled())
        {
            Connection cn = createDefaultConnection(false);
            ExperimentalFeaturesHelper.setExperimentalFeature(cn, "javascriptErrorServerLogging", false);
        }
    }

    public enum BrowserType
    {
        REMOTE,
        FIREFOX,
        IE,
        CHROME,
        HTML
    }

    public static void sleep(long ms)
    {
        try
        {
            Thread.sleep(ms);
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    public void copyFile(String original, String copy)
    {
        copyFile(new File(original), new File(copy));
    }

    public void copyFile(File original, File copy)
    {
        try
        {
            Files.createDirectories(Paths.get(copy.toURI()).getParent());
            Files.copy(Paths.get(original.toURI()), Paths.get(copy.toURI()),
                    StandardCopyOption.COPY_ATTRIBUTES,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void log(String str)
    {
        str = str.replace(Locator.NOT_HIDDEN, "NOT_HIDDEN"); // This xpath fragment really clutters up the log
        TestLogger.log(str);
    }

    private static final Pattern LABKEY_ERROR_TITLE_PATTERN = Pattern.compile("(\\d\\d\\d)\\D.*Error.*", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOMCAT_ERROR_PATTERN = Pattern.compile("HTTP Status\\s*(\\d\\d\\d)\\D");

    public int getResponseCode()
    {
        //We can't seem to get response codes via javascript, so we rely on default titles for error pages
        String title = getDriver().getTitle();

        Matcher m = LABKEY_ERROR_TITLE_PATTERN.matcher(title);
        if (m.find())
            return Integer.parseInt(m.group(1));

        //Now check the Tomcat page. This is going to be unreliable over time (known to work for Tomcat 7.0 - 9.0)
        m = TOMCAT_ERROR_PATTERN.matcher(getDriver().getPageSource());
        if (m.find())
            return Integer.parseInt(m.group(1));

        if ((!title.toLowerCase().contains("error")) && (!title.toLowerCase().contains("not found")))
            log("WARNING: Page appears to be an error page but title doesn't match any know pattern: \"" + title + "\"");

        return 200;
    }

    public String getResponseText()
    {
        return getHtmlSource();
    }

    public URL getURL()
    {
        try
        {
            return new URL(getDriver().getCurrentUrl());
        }
        catch (MalformedURLException x)
        {
            throw new RuntimeException("Bad location from selenium tester: " + getDriver().getCurrentUrl(), x);
        }
    }

    public String[] getLinkAddresses()
    {
        String js =
                "getLinkAddresses = function () {\n" +
                        "        var links = window.document.links;\n" +
                        "        var addresses = new Array();\n" +
                        "        for (var i = 0; i < links.length; i++)\n" +
                        "          addresses[i] = links[i].getAttribute('href');\n" +
                        "        return addresses;\n" +
                        "};" +
                        "return getLinkAddresses();";
        @SuppressWarnings("unchecked")
        List<String> linkArray = (ArrayList<String>) executeScript(js);
        ArrayList<String> links = new ArrayList<>();
        for (String link : linkArray)
        {
            if (link.contains("#"))
            {
                link = link.substring(0, link.indexOf("#"));
            }
            if (link.trim().length() > 0)
            {
                links.add(link);
            }
        }

        return links.toArray(new String[links.size()]);
    }

    public String getCurrentRelativeURL()
    {
        URL url = getURL();
        String urlString = getDriver().getCurrentUrl();
        if (80 == WebTestHelper.getWebPort() && url.getAuthority().endsWith(":-1"))
        {
            int portIdx = urlString.indexOf(":-1");
            urlString = urlString.substring(0, portIdx) + urlString.substring(portIdx + (":-1".length()));
        }

        String baseURL = WebTestHelper.getBaseURL();
        assertTrue("Expected URL to begin with " + baseURL + ", but found " + urlString, urlString.indexOf(baseURL) == 0);
        return urlString.substring(baseURL.length());
    }

    public void pushLocation()
    {
        _locationStack.push(getCurrentRelativeURL());
    }

    public void popLocation()
    {
        popLocation(defaultWaitForPage);
    }

    public void popLocation(int millis)
    {
        String location = _locationStack.pop();
        assertNotNull("Cannot pop without a push.", location);
        TestLogger.log("WARNING: pushing/popping locations can be hard to debug, please use beginAt(WebTestHelper.buildURL()) if possible.");
        beginAt(location, millis);
    }

    public void saveLocation()
    {
        _savedLocation = getCurrentRelativeURL();
    }

    public String getSavedLocation()
    {
        return _savedLocation;
    }

    public void recallLocation()
    {
        recallLocation(defaultWaitForPage);
    }

    public void recallLocation(int wait)
    {
        assertNotNull("Cannot recall without saving first.", _savedLocation);
        beginAt(_savedLocation, wait);
    }

    public void refresh()
    {
        refresh(defaultWaitForPage);
    }

    public void refresh(int millis)
    {
        doAndWaitForPageToLoad(() -> getDriver().navigate().refresh(), millis);
    }

    public void goBack(int millis)
    {
        doAndWaitForPageToLoad(() -> getDriver().navigate().back(), millis);
    }

    public void goBack()
    {
        goBack(defaultWaitForPage);
    }

    public void clickAdminMenuItem(String... items)
    {
        new SiteNavBar(getDriver()).clickAdminMenuItem(true, items);
    }

    public void clickUserMenuItem(String... items)
    {
        clickUserMenuItem(true, items);
    }

    public void clickUserMenuItem(boolean wait, String... items)
    {
        new SiteNavBar(getDriver()).clickUserMenuItem(wait, items);
    }

    @LogMethod(quiet = true)
    public WebElement clickMenuButton(boolean wait, WebElement menu, boolean onlyOpen, @LoggedParam String ... subMenuLabels)
    {
        menu.click();
        try
        {
            waitForElement(BootstrapMenu.Locators.menuItem(subMenuLabels[0]).notHidden(), 1000);
        }
        catch (NoSuchElementException retry)
        {
            menu.click(); // Sometimes ext4 menus don't open on the first try
            waitForElement(BootstrapMenu.Locators.menuItem(subMenuLabels[0]).notHidden(), 1000);
        }
        if (onlyOpen && subMenuLabels.length == 0)
            return null;

        for (int i = 0; i < subMenuLabels.length - 1; i++)
        {
            WebElement subMenuItem = waitForElement(BootstrapMenu.Locators.menuItem(subMenuLabels[i]).notHidden(), 2000);
            clickAndWait(subMenuItem, 0);
        }
        WebElement item = waitForElement(BootstrapMenu.Locators.menuItem(subMenuLabels[subMenuLabels.length - 1]).notHidden());
        if (onlyOpen)
        {
            mouseOver(item);
            return item;
        }

        if (wait)
            clickAndWait(item);
        else
            clickAndWait(item, 0);
        return null;
    }

    // Click on a module listed on the admin menu;
    public void goToModule(String moduleName)
    {
        new SiteNavBar(getDriver()).goToModule(moduleName);
    }

    public void goToSchemaBrowser()
    {
        goToModule("Query");
        shortWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.lk-sb-instructions")));
        waitForElement(Locators.pageSignal("queryTreeRendered"));
    }

    public void goToSchemaBrowser(int waitMilSec)
    {
        int waitSeconds = waitMilSec / 1000;

        goToModule("Query");
        new WebDriverWait(getDriver(), waitSeconds).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.lk-sb-instructions")));
        waitForElement(Locators.pageSignal("queryTreeRendered"), waitMilSec);
    }

    public FolderManagementPage goToFolderManagement()
    {
        clickAdminMenuItem("Folder", "Management");
        return new FolderManagementPage(getDriver());
    }

    public void goToFolderPermissions()
    {
        clickAdminMenuItem("Folder", "Permissions");
    }

    public ProjectSettingsPage goToProjectSettings()
    {
        clickAdminMenuItem("Folder", "Project Settings");
        return new ProjectSettingsPage(getDriver());
    }

    public DataRegionTable goToSiteUsers()
    {
        if (!isElementPresent(Locator.id("labkey-nav-trail-current-page").withText("Site Users")))
            clickAdminMenuItem("Site", "Site Users");
        return new DataRegionTable("Users", this);
    }

    public void goToSiteGroups()
    {
        if (!isElementPresent(Locator.tag("a").withClass("x4-tab-active").withText("Site Groups")))
            clickAdminMenuItem("Site", "Site Groups");
    }

    public void goToSiteDevelopers()
    {
        if (!isElementPresent(Locator.id("labkey-nav-trail-current-page").withText("Developers Group")))
        {
            clickAdminMenuItem("Site", "Site Developers");
            waitForElement(Locator.name("names"));
        }
    }

    public void goToSiteAdmins()
    {
        if (!isElementPresent(Locator.id("labkey-nav-trail-current-page").withText("Administrators Group")))
        {
            clickAdminMenuItem("Site", "Site Admins");
        }
    }

    public ManageViewsPage goToManageViews()
    {
        clickAdminMenuItem("Manage Views");
        waitForElement(Locator.xpath("//*[starts-with(@id, 'dataviews-panel')]"));
        _extHelper.waitForLoadingMaskToDisappear(WAIT_FOR_JAVASCRIPT);
        return new ManageViewsPage(getDriver());
    }

    public void goToManageStudy()
    {
        clickAdminMenuItem("Manage Study");
    }

    public void goToManageAssays()
    {
        clickAdminMenuItem("Manage Assays");
    }

    public BeginPage goToManageLists()
    {
        clickAdminMenuItem("Manage Lists");
        return new BeginPage(getDriver());
    }

    public void goToCreateProject()
    {
        clickAdminMenuItem("Site", "Create Project");
    }


    /**
     * Switch to the initial test window
     */
    public void switchToMainWindow()
    {
        switchToWindow(0);
    }

    public void switchToWindow(int index)
    {
        waitFor(() -> getDriver().getWindowHandles().size() > index, WAIT_FOR_JAVASCRIPT);
        List<String> windows = new ArrayList<>(getDriver().getWindowHandles());
        getDriver().switchTo().window(windows.get(index));
    }

    protected void closeExtraWindows()
    {
        List<String> windows = new ArrayList<>(getDriver().getWindowHandles());
        for (int i = 1; i < windows.size(); i++)
        {
            getDriver().switchTo().window(windows.get(i));
            executeScript("window.onbeforeunload = null;");
            getDriver().close();
        }
        if (windows.size() > 1)
        {
            getDriver().switchTo().window(windows.get(0));
        }
    }

    public boolean isPageEmpty()
    {
        //IE and Firefox have different notions of empty.
        //IE returns html for all pages even empty text...
        String text = getDriver().getPageSource();
        if (null == text || text.trim().length() == 0)
            return true;

        text = getBodyText();
        return null == text || text.trim().length() == 0;
    }

    /**
     * Get rendered, visible page text.
     * @return All visible text from the 'body' of the page
     */
    public String getBodyText()
    {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CallableGetText getText = new CallableGetText();
        Future<String> future = executor.submit(getText);

        try
        {
            return future.get(60, TimeUnit.SECONDS);
        }
        catch (java.util.concurrent.TimeoutException | InterruptedException | ExecutionException e)
        {
            throw new TestTimeoutException("Timed out getting page text. Page is probably too complex. Refactor test to look for specific element(s) instead.", e);
        }
        finally
        {
            executor.shutdownNow();
        }
    }

    /**
     * Get page text using a separate thread to avoid test timeouts when complex pages choke WebDriver
     */
    private class CallableGetText implements Callable<String>
    {
        @Override
        public String call()
        {
            try
            {
                return shortWait().until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("body"))).getText();
            }
            catch (TimeoutException |NoSuchElementException tex)
            {
                return getDriver().getPageSource(); // probably viewing a tsv or text file
            }
        }
    }

    @Contract(pure = true)
    public WebDriverWait shortWait()
    {
        return new WebDriverWait(getDriver(), 10);
    }

    @Contract(pure = true)
    public WebDriverWait longWait()
    {
        return new WebDriverWait(getDriver(), 30);
    }

    /**
     * @param reuseSession true to have the Java API connection "hijack" the session from the Selenium browser window
     */
    public Connection createDefaultConnection(boolean reuseSession)
    {
        Connection connection = new Connection(WebTestHelper.getBaseURL(), PasswordUtil.getUsername(), PasswordUtil.getPassword());
        if (reuseSession)
        {
            Cookie cookie = getDriver().manage().getCookieNamed("JSESSIONID");
            if (cookie == null)
            {
                throw new IllegalStateException("No session cookie available to reuse.");
            }

            connection.addCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(), cookie.getExpiry(), cookie.isSecure());
        }

        return connection;
    }

    public long beginAt(String relativeURL)
    {
        return beginAt(relativeURL, defaultWaitForPage);
    }

    public long beginAt(String relativeURL, int millis)
    {
        relativeURL = stripContextPath(relativeURL);
        String logMessage = "";

        try
        {
            if (relativeURL.length() == 0)
                logMessage = "Navigating to root";
            else
            {
                logMessage = "Navigating to " + relativeURL;
                if (relativeURL.charAt(0) != '/')
                {
                    relativeURL = "/" + relativeURL;
                }
            }

            final String fullURL = WebTestHelper.getBaseURL() + relativeURL;

            long elapsedTime = doAndWaitForPageToLoad(() -> getDriver().navigate().to(fullURL), millis);
            logMessage += " [" + elapsedTime + " ms]";


            return elapsedTime;
        }
        finally
        {
            log(logMessage); // log after navigation to
        }
    }

    public long goToURL(final URL url, int milliseconds)
    {
        String logMessage = "Navigating to " + url.toString();
        try
        {

            long elapsedTime = doAndWaitForPageToLoad(() -> getDriver().navigate().to(url), milliseconds);
            logMessage += " [" + elapsedTime + " ms]";

            return elapsedTime;
        }
        finally
        {
            log(logMessage);
        }
    }

    public void navigateToQuery(String schemaName, String queryName)
    {
        navigateToQuery(schemaName, queryName, null);
    }

    public void navigateToQuery(String schemaName, String queryName, Integer msTimeout)
    {
        RelativeUrl queryURL = new RelativeUrl("query", "executequery");
        queryURL.setContainerPath(getCurrentContainerPath());
        queryURL.addParameter("schemaName", schemaName);
        queryURL.addParameter("query.queryName", queryName);
        queryURL.setTimeout(msTimeout);

        queryURL.navigate(this);
    }

    // Get the container id of the current page
    public String getContainerId()
    {
        return (String)executeScript("return LABKEY.container.id;");
    }

    public String getContainerId(String url)
    {
        pushLocation();
        beginAt(url);
        String containerId = getContainerId();
        popLocation();
        return containerId;
    }

    public String getCurrentProject()
    {
        String[] splitPath = getCurrentContainerPath().split("/");
        for (String pathElement : splitPath)
        {
            if (!pathElement.isEmpty())
                return pathElement;
        }
        return "/"; //root
    }

    public String getCurrentContainerPath()
    {
        return (String)executeScript("return LABKEY.container.path;");
    }

    public String getCurrentUser()
    {
        return (String)executeScript("return LABKEY.user.email;");
    }

    public String getCurrentUserName()
    {
        return getDisplayName();
    }

    // Return display name for the current logged in user (or impersonated user)
    public String getDisplayName()
    {
        return (String)executeScript("return LABKEY.user.displayName");
    }

    public boolean onLabKeyPage()
    {
        return (Boolean)executeScript("return window.LABKEY != undefined;");
    }

    public boolean isSignedIn()
    {
        return (Boolean)executeScript("return LABKEY.user.isSignedIn;");
    }

    public boolean isUserSystemAdmin()
    {
        return (Boolean)executeScript("return LABKEY.user.isSystemAdmin;");
    }

    public boolean isUserAdmin()
    {
        return (Boolean)executeScript("return LABKEY.user.isAdmin;");
    }

    public boolean isSignedInAsPrimaryTestUser()
    {
        return PasswordUtil.getUsername().equals(getCurrentUser());
    }

    public boolean isImpersonating()
    {
        return (Boolean)executeScript("return LABKEY.impersonatingUser != undefined;");
    }

    public void assertSignedInNotImpersonating()
    {
        assertTrue("Not signed in", isSignedIn());
        assertFalse("Impersonating", isImpersonating());
        assertElementPresent(SiteNavBar.Locators.userMenu);
    }

    public boolean isOnServerErrorPage()
    {
        return isElementPresent(Locator.css("body > table.server-error"));
    }

    public void assertAlert(String msg)
    {
        Alert alert = waitForAlert();
        assertEquals("Wrong alert text", msg, alert.getText());
        alert.accept();
    }

    public void assertAlertContains(String partialMessage)
    {
        Alert alert = waitForAlert();
        String alertText = alert.getText();
        assertTrue("Wrong alert text: " + alertText + ", expected: " + partialMessage, alertText.contains(partialMessage));
        alert.accept();
    }

    public int dismissAllAlerts()
    {
        int alertCount = 0;
        while (alertCount < 10)
        {
            try
            {
                Alert alert = getDriver().switchTo().alert();
                log("Found unexpected alert: " + getAlertText(alert));
                alert.dismiss();
                alertCount++;
            }
            catch (NoAlertPresentException done)
            {
                break;
            }
        }
        getDriver().switchTo().defaultContent();
        if (alertCount == 10)
        {
            log("Too many alerts. Alert loop in JavaScript?");
        }
        return alertCount;
    }

    private String getAlertText(Alert alert)
    {
        try
        {
            return alert.getText();
        }
        catch (RuntimeException e)
        {
            return "Failed to get alert text: " + e.getMessage();
        }
    }

    public Alert getAlertIfPresent()
    {
        try
        {
            return getDriver().switchTo().alert();
        }
        catch (NoAlertPresentException ex)
        {
            return null;
        }
    }

    public String acceptAlert()
    {
        Alert alert = waitForAlert();
        String text = alert.getText();
        alert.accept();
        return text;
    }

    public String cancelAlert()
    {
        Alert alert = waitForAlert();
        String text = alert.getText();
        alert.dismiss();
        return text;
    }

    protected Alert waitForAlert()
    {
        waitFor(() -> null != getAlertIfPresent(), WAIT_FOR_JAVASCRIPT);
        return getDriver().switchTo().alert();
    }

    public void waitForAlert(String alertText, int wait)
    {
        waitFor(() -> null != getAlertIfPresent(), "No alert appeared.", wait);
        assertAlert(alertText);
    }

    public void assertExtMsgBox(String title, String text, String buttonToClick)
    {
        String actual = _extHelper.getExtMsgBoxText(title);
        assertTrue("Expected Ext.Msg box text '" + text + "', actual '" + actual + "'", actual.contains(text));
        clickButton(buttonToClick, 0);
    }

    public void assertExt4MsgBox(String bodyText, String buttonToClick)
    {
        waitForElement(Locator.css(".x4-window div").withText(bodyText));
        clickButton(buttonToClick, 0);
    }

    public String acceptModalAlert()
    {
        Alert alert = new LabKeyAlert(getDriver());
        String bodyText = alert.getText();
        alert.accept();
        return bodyText;
    }

    public enum SeleniumEvent
    {blur,change,mousedown,mouseup,click,reset,select,submit,abort,error,load,mouseout,mouseover,unload,keyup,focus}

    /**
     * Create and fire a JavaScript UIEvent
     * @param l event target
     * @param event event
     */
    public void fireEvent(Locator l, SeleniumEvent event)
    {
        fireEvent(l.findElement(getDriver()), event);
    }

    /**
     * Create and fire a JavaScript UIEvent
     * @param el event target
     * @param event event
     */
    public void fireEvent(WebElement el, SeleniumEvent event)
    {
        executeScript("" +
                "var element = arguments[0];" +
                "var eventType = arguments[1];" +
                "var myEvent = document.createEvent('UIEvent');" +
                "myEvent.initEvent(" +
                "   eventType, /* event type */" +
                "   true,      /* can bubble? */" +
                "   true       /* cancelable? */" +
                ");" +
                "element.dispatchEvent(myEvent);", el, event.toString());
    }

    public void assertTitleEquals(String match)
    {
        assertEquals("Wrong page title", match, getDriver().getTitle());
    }

    public void assertTitleContains(String match)
    {
        String title = getDriver().getTitle();
        assertTrue("Page title: '" + title + "' doesn't contain '" + match + "'", title.contains(match));
    }

    public void assertNoLabKeyErrors()
    {
        List<WebElement> errors = Locators.labkeyError.findElements(getDriver());

        for (WebElement error : errors)
        {
            String errorText = error.getText();
            if (!errorText.isEmpty())
                fail("Unexpected error found: " + errorText);
        }
    }

    public void assertLabKeyErrorPresent()
    {
        List<WebElement> errors = Locators.labkeyError.findElements(getDriver());

        for (WebElement error : errors)
        {
            if (!error.getText().isEmpty())
                return;
        }
        fail("No errors found");
    }

    public static String encodeText(String unencodedText)
    {
        return unencodedText
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public boolean isTextPresent(String... texts)
    {
        final MutableBoolean present = new MutableBoolean(true);

        TextSearcher.TextHandler handler = new TextSearcher.TextHandler()
        {
            @Override
            public boolean handle(String htmlSource, String text)
            {
                // Not found... stop enumerating and return false
                if (!htmlSource.contains(text))
                    present.setFalse();

                return present.getValue();
            }
        };
        TextSearcher searcher = new TextSearcher(this);
        searcher.setSearchTransformer(TextSearcher.TextTransformers.IDENTITY);
        searcher.setSourceTransformer(TextSearcher.TextTransformers.IDENTITY);
        searcher.searchForTexts(handler, texts);

        return present.getValue();
    }

    public List<String> getTextOrder(TextSearcher searcher, String... texts)
    {
        final List<Pair<String, Integer>> foundTexts = new ArrayList<>();

        TextSearcher.TextHandler handler = (textSource, text) -> {
            if (textSource.contains(text))
                foundTexts.add(new ImmutablePair<>(text, textSource.indexOf(text)));
            return true;
        };

        searcher.searchForTexts(handler, texts);

        List<String> orderedTexts = new ArrayList<>();
        foundTexts.stream()
                .sorted(Comparator.comparing(Pair::getValue))
                .forEachOrdered((pair) -> orderedTexts.add(pair.getKey()));

        return orderedTexts;
    }

    public List<String> getMissingTexts(TextSearcher searcher, String... texts)
    {
        final List<String> missingTexts = new ArrayList<>();

        TextSearcher.TextHandler handler = (textSource, text) -> {
            if (!textSource.contains(text))
                missingTexts.add(text);
            return true;
        };

        searcher.searchForTexts(handler, texts);

        return missingTexts;
    }

    public String getText(Locator elementLocator)
    {
        WebElement el = elementLocator.findElement(getDriver());
        return el.getText();
    }

    public List<String> getTexts(List<WebElement> elements)
    {
        List<String> texts = new ArrayList<>();

        for (WebElement el : elements)
        {
            texts.add(el.getText());
        }

        return texts;
    }

    /**
     * Verifies that all the strings are present in the page html source
     */
    public void assertTextPresent(String... texts)
    {
        assertTextPresent(new TextSearcher(this), texts);
    }

    public void assertTextPresent(TextSearcher searcher, String... texts)
    {
        List<String> missingTexts = getMissingTexts(searcher, texts);

        if (!missingTexts.isEmpty())
        {
            String failMsg = (missingTexts.size() == 1 ? "Text '" : "Texts ['") + String.join("', '", missingTexts) + "'";
            failMsg += missingTexts.size() == 1 ? " was not present" : "] were not present";
            fail(failMsg);
        }
    }

    /**
     * Verifies that all the strings are present in the page html source, disregards casing discrepancies
     */
    public void assertTextPresentCaseInsensitive(String... texts)
    {
        TextSearcher searcher = new TextSearcher(this);

        searcher.setSearchTransformer((text) -> encodeText(text).toLowerCase());

        searcher.setSourceTransformer(String::toLowerCase);

        assertTextPresent(searcher, texts);
    }

    /**
     * Verifies that at least one of the strings is present in the page html source
     */
    public void assertOneOfTheseTextsPresent(String... texts)
    {
        final MutableBoolean found = new MutableBoolean(false);

        TextSearcher.TextHandler handler = new TextSearcher.TextHandler(){
            @Override
            public boolean handle(String htmlSource, String text)
            {
                if (htmlSource.contains(text))
                    found.setTrue();

                return !found.getValue();
            }
        };
        TextSearcher searcher = new TextSearcher(this);
        searcher.searchForTexts(handler, texts);

        if (!found.getValue())
            fail("Did not find any of the following values on current page " + Arrays.toString(texts));
    }

    /**
     * Verifies that all the strings are present in the page html source
     */
    public void assertTextPresent(List<String> texts)
    {
        String[] textsArray = {};
        textsArray = texts.toArray(textsArray);
        assertTextPresent(textsArray);
    }

    public void assertTextPresentCaseInsensitive(List<String> texts)
    {
        String[] textsArray = {};
        textsArray = texts.toArray(textsArray);
        assertTextPresentCaseInsensitive(textsArray);
    }

    //takes the arguments used to set a filter and transforms them into the description in the grid view
    //then verifies that this description is present
    public void assertFilterTextPresent(String column, String type, String value)
    {
        String desc = type + value;
        String closeParenthesis = ")";
        if(type.contains("Equals One Of"))
        {
            desc = column + " IS ONE OF (" + value.replace(";", ", ") + closeParenthesis;
        }
        else if(type.contains("Contains One Of"))
        {
            desc = column + " CONTAINS ONE OF (" + value.replace(";", ", ") + closeParenthesis;
        }
        else if(type.contains("Does Not Equal Any Of"))
        {
            desc = column + " IS NOT ANY OF (" + value.replace(";", ", ") + closeParenthesis;
        }
        else if(type.contains("Does Not Contain Any Of"))
        {
            desc = column + " DOES NOT CONTAIN ANY OF (" + value.replace(";", ", ") + closeParenthesis;
        }
        else if(type.equals("Equals"))
        {
            desc = column +  " = " + value;
        }
        else if(type.contains("Start") || type.contains("Contain"))    //Starts With, Does Not Start With, Contains, Does not Contain
        {
            desc = column + " " + type.toUpperCase() + " " + value;
        }
        else if(type.equals("Does Not Equal"))
        {
            desc = column + " <> " + value;
        }
        else if(type.contains("Greater"))
        {
            desc = column + " >";
            if(type.contains("Equal To"))
                desc+="=";
            desc += " " + value;
        }
        else if(type.contains("Less"))
        {
            desc = column + " <";
            if(type.contains("Equal To"))
                desc+="=";
            desc += " " + value;
        }
        else if(type.contains("Is Blank"))
        {
            desc = column + " is blank";
        }
        else if(type.contains("Is Not Blank"))
        {
            desc = column + " is not blank";
        }

        assertTextPresent(desc);

    }

    public void assertTextPresent(String text, int amount)
    {
        assertEquals("Text '" + text + "' was not present the correct number of times", amount, countText(text));
    }

    public void assertTextPresent(TextSearcher searcher, String text, int amount)
    {
        assertEquals("Text '" + text + "' was not present the correct number of times", amount, countText(searcher, text));
    }

    public int countText(String text)
    {
        text = text.replace("&nbsp;", " ");
        String html = getHtmlSource();
        // Strip all JavaScript tags; in particular, the selenium-injected javascript tag, which can foul up the expected occurrences
        String source = html.replaceAll("(?msi)<script type=\"text/javascript\">.*?</script>", "");
        int current_index = 0;
        int count = 0;

        while ((current_index = source.indexOf(text, current_index + 1)) != -1)
            count++;
        return count;
    }

    public static int countText(TextSearcher searcher, String text)
    {
        final List<Integer> foundIndices = new ArrayList<>();

        TextSearcher.TextHandler handler = new TextSearcher.TextHandler()
        {
            @Override
            public boolean handle (String source, String text)
            {
                int current_index = 0;
                while ((source.indexOf(text, current_index + 1)) != -1)
                {
                    current_index = source.indexOf(text, current_index +1);
                    foundIndices.add(current_index);
                }
                return true;
            }
        };

        searcher.searchForTexts(handler, new String[]{text});
        return foundIndices.size();
    }

    public static void assertTextNotPresent(TextSearcher searcher, String... texts)
    {
        // Number of characters on either side of found text to include in error message
        final int RANGE = 20;
        List<String> errors = new ArrayList<>();

        TextSearcher.TextHandler handler = new TextSearcher.TextHandler()
        {
            @Override
            public boolean handle(String htmlSource, String text)
            {
                int position = htmlSource.indexOf(text);

                if (position > -1)
                {
                    int prefixStart = Math.max(0, position - RANGE);
                    int suffixEnd = Math.min(htmlSource.length() - 1, position + text.length() + RANGE);
                    String prefix = htmlSource.substring(prefixStart, position);
                    String suffix = htmlSource.substring(position + text.length(), suffixEnd);

                    errors.add("Text '" + text + "' was present: " + prefix + "[" + text + "]" + suffix);
                }

                return true;
            }
        };

        searcher.searchForTexts(handler, texts);
        Assert.assertTrue(String.join("\n", errors), errors.isEmpty());
    }

    public void assertTextNotPresent(String... texts)
    {
        TextSearcher searcher = new TextSearcher(this);
        searcher.setSearchTransformer((text) -> encodeText(text).replace("&nbsp;", " "));

        assertTextNotPresent(searcher, texts);
    }

    public String getTextInTable(String dataRegion, int row, int column)
    {
        String id = Locator.xq(dataRegion);
        return getDriver().findElement(By.xpath("//table[@id=" + id + "]/tbody/tr[" + row + "]/td[" + column + "]")).getText();
    }

    public void assertTextAtPlaceInTable(String textToCheck, String dataRegion, int row, int column)
    {
        assertEquals(textToCheck + " is not at that place in the table", textToCheck, getTextInTable(dataRegion, row, column));
    }

    public String getTextInNonDataRegionTable(String title, int row, int column)
    {
        return getTableCellText(Locator.xpath("//div/h3/a/span[text()='" + title + "']/../../../../div/table"), row, column);
    }

    public void assertTableRowInNonDataRegionTable(String title, String textToCheck, int row, int column)
    {
        assertEquals(textToCheck + " is not at that place in the table", textToCheck, getTextInNonDataRegionTable(title, row, column));
    }

    /**
     * Searches only the displayed text in the body of the page, not the HTML source.
     */
    public boolean isTextBefore(String text1, String text2)
    {
        String source = getBodyText();
        return (source.indexOf(text1) < source.indexOf(text2));
    }

    /**
     * @return null = yes, present in this order
     * otherwise returns out of order string and explanation of error
     */
    public String isPresentInThisOrder(String... text)
    {
        String source = getBodyText();
        int previousIndex = -1;
        String previousString = null;

        for (String s : text)
        {
            int index = source.indexOf(s);

            if(index == -1)
                return s + " not found";
            if(index <= previousIndex)
                return s + " occured out of order; came before " + previousString;
            previousIndex = index;
            previousString = s;
        }
        return null;
    }

    // Searches only the displayed text in the body of the page, not the HTML source.
    public void assertTextPresentInThisOrder(String... texts)
    {
        TextSearcher searcher = new TextSearcher(this::getBodyText);
        assertTextPresentInThisOrder(searcher, texts);
    }

    public void assertTextPresentInThisOrder(TextSearcher searcher, String... texts)
    {
        List<String> foundTextInOrder = getTextOrder(searcher, texts);
        assertEquals("Text found out of order.", Arrays.asList(texts), foundTextInOrder);
    }

    public void assertTextBefore(String text1, String text2)
    {
        assertTextPresentInThisOrder(text1, text2);
    }

    private void waitForPageToLoad(WebElement toBeStale, int millis)
    {
        _testTimeout = true;
        new WebDriverWait(getDriver(), millis / 1000).until(new Function<WebDriver, Boolean>()
        {
            private final ExpectedCondition<Boolean> staleCheck = ExpectedConditions.stalenessOf(toBeStale);

            @Override
            public Boolean apply(WebDriver input)
            {
                return staleCheck.apply(input);
            }

            @Override
            public String toString()
            {
                return "waiting for browser to navigate";
            }
        });
        waitForOnReady("jQuery");
        waitForOnReady("Ext");
        waitForOnReady("Ext4");
        waitForOnReady("LABKEY.Utils");
        mouseOut();
        _testTimeout = false;
    }

    /**
     * Wait for JavaScript API's onReady
     * @param apiName Will return when {apiName}.onReady calls the callback
     */
    private void waitForOnReady(String apiName)
    {
        try
        {
            executeAsyncScript("" +
                    "try " +
                    "{" +
                    apiName + ".onReady(callback);" +
                    "}" +
                    "catch(e)" +
                    "{" +
                    "  callback();" +
                    "}");
        }
        catch (TimeoutException e)
        {
            throw new RuntimeException("Timed out waiting for " + apiName + ".onReady(). Check server log for JavaScript errors.", e);
        }
    }

    public long doAndWaitForPageToLoad(Runnable func)
    {
        return doAndWaitForPageToLoad(func, defaultWaitForPage);
    }

    public long doAndWaitForPageToLoad(Runnable func, final int msWait)
    {
        long startTime = System.currentTimeMillis();
        WebElement toBeStale = null;

        if (msWait > 0)
        {
            _pageLoadListeners.getOrDefault(getDriver(), Collections.emptySet()).forEach((listener) -> {
                if (null != listener)
                    listener.beforePageLoad();
            });
            getDriver().manage().timeouts().pageLoadTimeout(msWait, TimeUnit.MILLISECONDS);
            toBeStale = Locator.css(":root").findElement(getDriver()); // Document should become stale
        }

        func.run();

        if (msWait > 0)
        {
            waitForPageToLoad(toBeStale, msWait);
            getDriver().manage().timeouts().pageLoadTimeout(defaultWaitForPage, TimeUnit.MILLISECONDS);
            _pageLoadListeners.getOrDefault(getDriver(), Collections.emptySet()).forEach((listener) -> {
                if (null != listener)
                    listener.afterPageLoad();
            });
        }

        return System.currentTimeMillis() - startTime;
    }

    private static final WeakHashMap<WebDriver, Set<PageLoadListener>> _pageLoadListeners = new WeakHashMap<>();

    public void addPageLoadListener(PageLoadListener listener)
    {
        _pageLoadListeners.putIfAbsent(getDriver(), Collections.newSetFromMap(new WeakHashMap<>()));
        _pageLoadListeners.get(getDriver()).add(listener);
    }

    /**
     * Interface for classes that want to be notified of page loads (e.g. to clear a cache)
     * Not guarianteed to work flawlessly. It depends on tests using {@linkplain #doAndWaitForPageToLoad(Runnable)}
     */
    public interface PageLoadListener
    {
        default void beforePageLoad(){}
        default void afterPageLoad(){}
    }

    /**
     * Wait for signaling element created by LABKEY.Utils.signalWebDriverTest
     * If signal element is already present, this will wait for it to become stale and be recreated
     * @param func Function that will trigger the desired signal to appear on the page
     * @param signalName Should match the signal name defined in LABKEY.Utils.signalWebDriverTest
     * @return The 'value' of the signal, if any
     */
    public String doAndWaitForPageSignal(Runnable func, String signalName)
    {
        return doAndWaitForPageSignal(func, signalName, shortWait());
    }

    /**
     * Wait for signaling element created by LABKEY.Utils.signalWebDriverTest
     * If signal element is already present, this will wait for it to become stale and be recreated
     * @param func Function that will trigger the desired signal to appear on the page
     * @param signalName Should match the signal name defined in LABKEY.Utils.signalWebDriverTest
     * @return The 'value' of the signal, if any
     */
    public String doAndWaitForPageSignal(Runnable func, String signalName, WebDriverWait wait)
    {
        String value;
        try
        {
            value = doAndWaitForElementToRefresh(func, Locators.pageSignal(signalName), wait).getAttribute("value");
        }
        catch (StaleElementReferenceException multiSignaled)
        {
            value = Locators.pageSignal(signalName).findElement(getDriver()).getAttribute("value");
        }
        return value;
    }

    /**
     * Do something that should make an element disappear and reappear
     */
    public WebElement doAndWaitForElementToRefresh(Runnable func, Locator loc, SearchContext context, WebDriverWait wait)
    {
        WebElement previousElement = loc.findElementOrNull(context);

        func.run();

        if (previousElement != null)
            wait.until(ExpectedConditions.stalenessOf(previousElement));

        return wait.until(LabKeyExpectedConditions.elementPresent(loc, context));
    }

    public WebElement doAndWaitForElementToRefresh(Runnable func, Locator loc, WebDriverWait wait)
    {
        return doAndWaitForElementToRefresh(func, loc, getDriver(), wait);
    }

    /**
     * Wait for Supplier to return true
     * @param wait milliseconds
     * @return false if Supplier.get() doesn't return true within 'wait' ms
     */
    @Contract(pure = true)
    public static boolean waitFor(Supplier<Boolean> checker, int wait)
    {
        long startTime = System.currentTimeMillis();
        do
        {
            if( checker.get() )
                return true;
            sleep(100);
        } while ((System.currentTimeMillis() - startTime) < wait);

        return false;
    }

    public void waitForEquals(String message, Supplier expected, Supplier actual, int wait)
    {
        waitFor(() -> Objects.equals(expected.get(), actual.get()), wait);

        assertEquals(message, expected.get(), actual.get());
    }

    public void waitForNotEquals(String message, Supplier expected, Supplier actual, int wait)
    {
        waitFor(() -> !Objects.equals(expected.get(), actual.get()), wait);

        assertNotEquals(message, expected.get(), actual.get());
    }

    public static void waitFor(Supplier<Boolean> checker, String failMessage, int wait)
    {
        if (!waitFor(checker, wait))
        {
            throw new TimeoutException(failMessage + " [" + wait + "ms]");
        }
    }

    public File clickAndWaitForDownload(Locator elementToClick)
    {
        return clickAndWaitForDownload(elementToClick.findElement(getDriver()));
    }

    public File[] clickAndWaitForDownload(final Locator elementToClick, final int expectedFileCount)
    {
        return clickAndWaitForDownload(elementToClick.findElement(getDriver()), expectedFileCount);
    }

    public File clickAndWaitForDownload(final WebElement elementToClick)
    {
        return clickAndWaitForDownload(elementToClick, 1)[0];
    }

    public File[] clickAndWaitForDownload(final WebElement elementToClick, final int expectedFileCount)
    {
        return doAndWaitForDownload(elementToClick::click, expectedFileCount);
    }

    public File doAndWaitForDownload(Runnable func)
    {
        return doAndWaitForDownload(func, 1)[0];
    }

    // Pattern matcher for UUID
    // https://stackoverflow.com/questions/136505/searching-for-uuids-in-text-with-regex
    private static final Pattern TEMP_FILE_PATTERN = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.tmp");

    public File[] doAndWaitForDownload(Runnable func, final int expectedFileCount)
    {
        final File downloadDir = BaseWebDriverTest.getDownloadDir();
        File[] existingFilesArray = downloadDir.listFiles();
        final List<File> existingFiles;

        if (existingFilesArray != null)
            existingFiles = Arrays.asList(existingFilesArray);
        else
            existingFiles = new ArrayList<>();

        func.run();

        final FileFilter tempFilesFilter = file -> file.getName().contains(".part") ||
                file.getName().contains(".com.google.Chrome") ||
                file.getName().contains(".crdownload") || TEMP_FILE_PATTERN.matcher(file.getName()).matches();

        final FileFilter newFileFilter = file -> !existingFiles.contains(file) &&
                !tempFilesFilter.accept(file) &&
                file.length() > 0;

        waitFor(() ->{
                    final File[] files = downloadDir.listFiles(newFileFilter);
                    return files != null && files.length >= expectedFileCount;
                },
                "File(s) did not appear in download dir: " + downloadDir.toString(), WAIT_FOR_PAGE);

        waitFor(() -> {
                    final File[] files = downloadDir.listFiles(tempFilesFilter);
                    return files != null && files.length == 0;
                },
                "Temp files remain after download: " + downloadDir.toString(), WAIT_FOR_JAVASCRIPT);

        File[] newFiles = downloadDir.listFiles(newFileFilter);

        log("File(s) downloaded to " + downloadDir);
        for (File newFile : newFiles)
        {
            log("  File downloaded: " + newFile.getName());
        }
        assertEquals("Wrong number of files downloaded to " + downloadDir.toString(), expectedFileCount, newFiles.length);

        if (getDriver() instanceof FirefoxDriver)
            Locator.css("body").findElement(getDriver()).sendKeys(Keys.ESCAPE); // Dismiss download dialog

        return newFiles;
    }

    public WebElement waitForElement(final Locator locator)
    {
        return waitForElement(locator, WAIT_FOR_JAVASCRIPT);
    }

    public WebElement waitForElement(final Locator locator, int wait)
    {
        return locator.waitForElement(getDriver(), wait);
    }

    /**
     *
     * @param locator Element to wait for
     * @param elementTimeout amount of time to wait for
     * @param failIfNotFound should fail if element is not found?  If not, will return false
     * @return did element appear?
     */
    public boolean waitForElement(final Locator locator, int elementTimeout, boolean failIfNotFound)
    {
        if (failIfNotFound)
        {
            locator.waitForElement(getDriver(), elementTimeout);
        }
        else
        {
            try
            {
                locator.waitForElement(getDriver(), elementTimeout);
            }
            catch(NoSuchElementException e)
            {
                return false;
            }
        }
        return true;
    }

    public WebElement waitForElementToBeVisible(final Locator locator)
    {
        // waitForElement first, to differentiate between element never present vs not visible
        WebElement element = waitForElement(locator);
        waitFor(element::isDisplayed,
                "Timeout waiting for element to become visible", BaseWebDriverTest.WAIT_FOR_JAVASCRIPT);
        return element;
    }

    public WebElement waitForAnyElement(String error, final Locator... locators)
    {
        if (locators.length == 0)
            throw new IllegalArgumentException("Specify at least one Locator");

        try
        {
            return shortWait().until(new ExpectedCondition<WebElement>()
            {
                @Override
                public WebElement apply(@Nullable WebDriver webDriver)
                {
                    for (Locator loc : locators)
                    {
                        try
                        {
                            return loc.findElement(webDriver);
                        }
                        catch (NoSuchElementException ignore) {}
                    }
                    return null;
                }
            });
        }
        catch (TimeoutException notFound)
        {
            throw new NoSuchElementException(error);
        }
    }

    public WebElement waitForAnyElement(final Locator... locators)
    {
        return waitForAnyElement("None of the specified elements appeared", locators);
    }

    public List<WebElement> waitForElements(final Locator... locators)
    {
        if (locators.length > 0)
        {
            return shortWait().until(new Function<SearchContext, List<WebElement>>()
            {
                @Override
                public List<WebElement> apply(@Nullable SearchContext context)
                {
                    List<WebElement> allElements = new ArrayList<>();
                    for (Locator loc : locators)
                    {
                        List<WebElement> elements = loc.findElements(context);
                        if (elements.isEmpty())
                            return null;
                        allElements.addAll(elements);
                    }
                    return allElements;
                }

                @Override
                public String toString()
                {
                    return "elements to appear";
                }
            });
        }
        return null;
    }

    public WebElement waitForElementWithRefresh(Locator loc, int wait)
    {
        long startTime = System.currentTimeMillis();

        do
        {
            try
            {
                return waitForElement(loc, 1000);
            }
            catch (NoSuchElementException retry)
            {
                refresh();
            }
        }while(System.currentTimeMillis() - startTime < wait);

        return waitForElement(loc, 1000);
    }

    public void waitForElementText(final Locator locator, final String text)
    {
        waitForElementText(locator, text, WAIT_FOR_JAVASCRIPT);
    }

    public void waitForElementText(final Locator locator, final String text, int wait)
    {
        waitFor(() -> getText(locator).equals(text),
                "Expected '" + text + "' in element '" + locator + "'", wait);
    }

    public void waitForElementToDisappear(final Locator locator)
    {
        waitForElementToDisappear(locator, WAIT_FOR_JAVASCRIPT);
    }

    public void waitForElementToDisappear(final Locator locator, int wait)
    {
        locator.waitForElementToDisappear(getDriver(), wait);
    }

    public void waitForElementToDisappearWithRefresh(Locator loc, int wait)
    {
        long startTime = System.currentTimeMillis();

        do
        {
            if(!isElementPresent(loc))
                return;
            refresh();
        }while(System.currentTimeMillis() - startTime < wait);

        waitForElementToDisappear(loc, 1000);
    }

    public void waitForTextToDisappear(final String text)
    {
        waitForTextToDisappear(text, WAIT_FOR_JAVASCRIPT);
    }

    public void waitForTextToDisappear(final String text, int wait)
    {
        String failMessage = "Text: " + text + " was still present after [" + wait + "ms]";
        waitFor(() -> !isTextPresent(text), failMessage, wait);
    }

    public void waitForTextWithRefresh(int wait, String... text)
    {
        long startTime = System.currentTimeMillis();

        do
        {
            if(isTextPresent(text))
                return;
            else
                sleep(1000);
            refresh();
        }while(System.currentTimeMillis() - startTime < wait);
        assertTextPresent(text);
    }

    public void waitForText(final String... text)
    {
        waitForText(WAIT_FOR_JAVASCRIPT, text);
    }

    public void waitForText(int wait, final String... text)
    {
        waitFor(() -> isTextPresent(text), wait);
        assertTextPresent(text);
    }

    public void waitForText(final String text, final int count, int wait)
    {
        final String failMessage = "'" + text + "' was not present " + count + " times.";
        waitFor(() -> countText(text) == count, failMessage, wait);
    }

    public boolean isElementPresent(Locator loc)
    {
        return loc.findElements(getDriver()).size() > 0;
    }

    public boolean isElementVisible(Locator loc)
    {
        return loc.findElement(getDriver()).isDisplayed();
    }

    public void assertElementPresent(Locator loc)
    {
        assertTrue("Element is not present: " + loc.getLoggableDescription(), isElementPresent(loc));
    }

    public void assertElementPresent(Locator loc, int amount)
    {
        assertElementPresent("Element '" + loc + "' is not present " + amount + " times", loc, amount);
    }

    public void assertElementPresent(String message, Locator loc, int amount)
    {
        assertEquals(message, amount, getElementCount(loc));
    }

    public void assertElementContains(Locator loc, String text)
    {
        String elemText = loc.findElement(getDriver()).getText();
        if(elemText == null)
            fail("The element at location " + loc.toString() + " contains no text! Expected '" + text + "'.");
        if(!elemText.contains(text))
            fail("The element at location '" + loc.toString() + "' contains '" + elemText + "'; expected '" + text + "'.");
    }

    public boolean elementContains(Locator loc, String text)
    {
        String elemText = loc.findElement(getDriver()).getText();
        return (elemText != null && elemText.contains(text));
    }

    public void waitForFormElementToEqual(final WebElement el, final String value)
    {
        String failMessage = "Field with name " + el.getAttribute("name") + " did not have expected value";
        waitForEquals(failMessage, () -> value, () -> getFormElement(el), WAIT_FOR_JAVASCRIPT);
    }

    public void waitForFormElementToEqual(final Locator locator, final String value)
    {
        waitForFormElementToEqual(new EphemeralWebElement(locator, getDriver()), value);
    }

    public void waitForFormElementToNotEqual(final WebElement el, final String value)
    {
        String failMessage = "Field with name " + el.getAttribute("name") + " did change value";
        waitForNotEquals(failMessage, () -> value, () -> getFormElement(el), WAIT_FOR_JAVASCRIPT);
    }

    public void waitForFormElementToNotEqual(final Locator locator, final String value)
    {
        waitForFormElementToNotEqual(new EphemeralWebElement(locator, getDriver()), value);
    }

    public String getFormElement(Locator loc)
    {
        return getFormElement(loc.findElement(getDriver()));
    }

    public String getFormElement(WebElement el)
    {
        return (String) executeScript("return arguments[0].value;", el);
    }

    /**
     * @deprecated Use {@link org.junit.Assert#assertEquals(String, Object, Object) and {@link #getFormElement(Locator)}}
     */
    @Deprecated
    public void assertFormElementEquals(Locator loc, String value)
    {
        assertEquals(value, getFormElement(loc));
    }

    /**
     * @deprecated Use {@link org.junit.Assert#assertEquals(String, Object, Object) and {@link #getSelectedOptionText(Locator)}
     */
    @Deprecated
    public void assertOptionEquals(Locator loc, String value)
    {
        assertEquals(value, getSelectedOptionText(loc));
    }

    public String getSelectedOptionText(Locator loc)
    {
        return getSelectedOptionText(loc.findElement(getDriver()));
    }

    public String getSelectedOptionText(WebElement el)
    {
        Select select = new Select(el);
        return select.getFirstSelectedOption().getText();
    }

    public String getSelectedOptionValue(Locator loc)
    {
        return getSelectedOptionValue(loc.findElement(getDriver()));
    }

    public String getSelectedOptionValue(WebElement el)
    {
        Select select = new Select(el);
        return select.getFirstSelectedOption().getAttribute("value");
    }

    public List<String> getSelectedOptionValues(Locator loc)
    {
        Select select = new Select(loc.findElement(getDriver()));
        List<WebElement> selectedOptions =  select.getAllSelectedOptions();
        List<String> values = new ArrayList<>();
        for (WebElement selectedOption : selectedOptions)
            values.add(selectedOption.getAttribute("value"));
        return values;
    }

    public List<String> getSelectOptions(Locator loc)
    {
        Select select = new Select(loc.findElement(getDriver()));
        List<WebElement> selectOptions = select.getOptions();
        return getTexts(selectOptions);
    }

    public void assertElementNotPresent(String errorMsg, Locator loc)
    {
        assertFalse(errorMsg, isElementPresent(loc));
    }

    public void assertElementNotPresent(Locator loc)
    {
        assertElementNotPresent("Element was present in page: " + loc, loc);
    }

    public void assertElementNotVisible(Locator loc)
    {
        assertFalse("Element was visible in page: " + loc, loc.findElement(getDriver()).isDisplayed());
    }

    public void assertElementVisible(Locator loc)
    {
        assertTrue("Element was not visible: " + loc, loc.findElement(getDriver()).isDisplayed());
    }

    public WebElement scrollIntoView(Locator loc)
    {
        return scrollIntoView(loc.findElement(getDriver()));
    }

    public WebElement scrollIntoView(Locator loc, Boolean alignToTop)
    {
        return scrollIntoView(loc.findElement(getDriver()), alignToTop);
    }

    public WebElement scrollIntoView(WebElement el)
    {
        return scrollIntoView(el, false);
    }

    public WebElement scrollIntoView(WebElement el, Boolean alignToTop)
    {
        executeScript("arguments[0].scrollIntoView(arguments[1]);", el, alignToTop);
        return el;
    }

    public void scrollBy(Integer x, Integer y)
    {
        executeScript("window.scrollBy(" + x.toString() +", " + y.toString() + ");");
    }

    /**
     * @deprecated Use {@link WebElement#click()}
     * find and waitFor methods in Locator now return decorated WebElements that provide special click handling
     */
    @Deprecated
    public void click(WebElement el)
    {
        el.click();
    }

    public void click(Locator l)
    {
        clickAndWait(l, 0);
    }

    public void clickAt(Locator l, int xCoord, int yCoord)
    {
        clickAt(l, xCoord, yCoord, WAIT_FOR_PAGE);
    }

    public void clickAt(Locator l, int xCoord, int yCoord, int pageTimeout)
    {
        WebElement el = l.waitForElement(getDriver(), WAIT_FOR_JAVASCRIPT);
        clickAt(el, xCoord, yCoord, pageTimeout);
    }

    public void clickAt(final WebElement el, final int xCoord, final int yCoord, int pageTimeout)
    {
        doAndWaitForPageToLoad(() ->
        {
            Actions builder = new Actions(getDriver());
            builder.moveToElement(el, xCoord, yCoord)
                    .click()
                    .build()
                    .perform();
        }, pageTimeout);
    }

    public void clickAndWait(Locator l)
    {
        clickAndWait(l, defaultWaitForPage);
    }

    public static final int WAIT_FOR_EXT_MASK_TO_DISSAPEAR = -1;
    public static final int WAIT_FOR_EXT_MASK_TO_APPEAR = -2;

    public void clickAndWait(Locator l, int pageTimeoutMs)
    {
        WebElement el;
        try
        {
            el = l.findElement(getDriver());
            clickAndWait(el, pageTimeoutMs);
        }
        catch (StaleElementReferenceException e)
        {
            // Locator.findElement likely didn't return the element we wanted due to timing problem. Wait and try again.
            // Ideally we'd decorate WebElement to know its locator and encapsulate the retry.
            sleep(500);
            el = l.findElement(getDriver());
            clickAndWait(el, pageTimeoutMs);
            log("WARNING: Need to improve timing, clicked element immediately became stale.: " + el.toString());
        }
    }

    public void clickAndWait(WebElement el)
    {
        clickAndWait(el, getDefaultWaitForPage());
    }

    public void clickAndWait(final WebElement el, int pageTimeoutMs)
    {
        doAndWaitForPageToLoad(() ->
        {
            el.click();
        }, pageTimeoutMs);

        if(pageTimeoutMs==WAIT_FOR_EXT_MASK_TO_APPEAR)
            _extHelper.waitForExt3Mask(WAIT_FOR_JAVASCRIPT);
        else if(pageTimeoutMs==WAIT_FOR_EXT_MASK_TO_DISSAPEAR)
            _extHelper.waitForExt3MaskToDisappear(WAIT_FOR_JAVASCRIPT);
    }

    public void doubleClick(Locator l)
    {
        doubleClickAndWait(l, 0);
    }

    public void doubleClickAndWait(final Locator l, int millis)
    {
        doubleClickAndWait(l.findElement(getDriver()), millis);
    }

    public void doubleClick(WebElement l)
    {
        doubleClickAndWait(l, 0);
    }

    public void doubleClickAndWait(final WebElement l, int millis)
    {
        doAndWaitForPageToLoad(() ->
        {
            Actions action = new Actions(getDriver());
            action.doubleClick(l).perform();
        }, millis);
    }

    public void selectFolderTreeItem(String folderName)
    {
        click(Locator.permissionsTreeNode(folderName));
    }

    public void mouseOut()
    {
        new Actions(getDriver()).moveByOffset(-9999,-9999).build().perform();
    }

    public void mouseOver(Locator l)
    {
        WebElement el = l.findElement(getDriver());
        mouseOver(el);
    }

    public void mouseOver(WebElement el)
    {
        Actions builder = new Actions(getDriver());
        builder.moveToElement(el).build().perform();
    }

    public int getElementIndex(Locator.XPathLocator l)
    {
        return l.child("preceding-sibling::*").findElements(getDriver()).size();
    }

    public int getElementIndex(WebElement el)
    {
        return Locator.xpath("preceding-sibling::*").findElements(el).size();
    }

    public void dragAndDrop(Locator from, Locator to)
    {
        dragAndDrop(from, to, Position.top);
    }
    public void dragAndDrop(WebElement from, WebElement to)
    {
        dragAndDrop(from, to, Position.top);
    }

    public enum Position
    {top, bottom, middle}

    public void dragAndDrop(Locator from, Locator to, Position pos)
    {
        WebElement fromEl = from.findElement(getDriver());
        WebElement toEl = to.findElement(getDriver());
        dragAndDrop(fromEl, toEl, pos);
    }

    public void dragAndDrop(WebElement fromEl, WebElement toEl, Position pos)
    {
        int y;
        switch (pos)
        {
            case top:
                y = 1;
                break;
            case bottom:
                y = toEl.getSize().getHeight() - 1;
                break;
            case middle:
                y = toEl.getSize().getHeight() / 2;
                break;
            default:
                throw new IllegalArgumentException("Unexpected position: " + pos.toString());
        }

        Actions builder = new Actions(getDriver());
        builder.clickAndHold(fromEl).moveToElement(toEl, toEl.getSize().getWidth() / 2, y).release().build().perform();
    }

    public void dragAndDrop(Locator el, int xOffset, int yOffset)
    {
        WebElement fromEl = el.findElement(getDriver());
        dragAndDrop(fromEl, xOffset, yOffset);
    }

    public void dragAndDrop(WebElement fromEl, int xOffset, int yOffset)
    {
        Actions builder = new Actions(getDriver());
        builder.clickAndHold(fromEl).moveByOffset(xOffset + 1, yOffset + 1).release().build().perform();
    }

    // This is useful when making a draggin selection in a plot, and there may be many elements ontop of the one you want.
    public void dragAndDrop(int xOffset, int yOffset)
    {
        Actions builder = new Actions(getDriver());
        builder.clickAndHold().moveByOffset(xOffset + 1, yOffset + 1).release().build().perform();
    }

    /**
     * @deprecated Use {@link #getTableCellText(org.labkey.test.Locator.XPathLocator, int, int)}
     */
    @Deprecated
    private String getTableCellText(String tableId, int row, int column)
    {
        return getTableCellText(Locator.xpath("//table[@id=" + Locator.xq(tableId) + "]"), row, column);
    }

    public String getTableCellText(Locator.XPathLocator table, int row, int column)
    {
        return getText(getSimpleTableCell(table, row, column));
    }

    public Locator getSimpleTableCell(Locator.XPathLocator table, int row, int column)
    {
        return table.append("/tbody/tr[" + (row + 1) + "]/*[self::td or self::th][" + (column + 1) + "]");
    }

    /**
     * @deprecated Use {@link #getTableCellText(org.labkey.test.Locator.XPathLocator, int, int)} and {@link org.junit.Assert#assertEquals(Object, Object)}
     */
    @Deprecated public void assertTableCellTextEquals(String tableName, int row, int column, String value)
    {
        assertEquals(tableName + "." + String.valueOf(row) + "." + String.valueOf(column) + " != \"" + value + "\"", value, getTableCellText(tableName, row, column));
    }

    /**
     * @deprecated Use {@link #getTableCellText(org.labkey.test.Locator.XPathLocator, int, int)} and {@link #assertElementContains(Locator, String)}
     */
    @Deprecated public void assertTableCellContains(String tableName, int row, int column, String... strs)
    {
        String cellText = getTableCellText(tableName, row, column);

        for (String str : strs)
        {
            assertTrue(tableName + "." + row + "." + column + " should contain \'" + str + "\' (actual value is " + cellText + ")", cellText.contains(str));
        }
    }

    /**
     * @deprecated Use {@link #getTableCellText(org.labkey.test.Locator.XPathLocator, int, int)}
     */
    @Deprecated public void assertTableCellNotContains(String tableName, int row, int column, String... strs)
    {
        String cellText = getTableCellText(tableName, row, column);

        for (String str : strs)
        {
            assertFalse(tableName + "." + row + "." + column + " should not contain \'" + str + "\'", cellText.contains(str));
        }
    }

    // Specifies cell values in a TSV string -- values are separated by tabs, rows are separated by \n
    public void assertTableRowsEqual(String tableId, int startRow, String cellValuesTsv)
    {
        String[] lines = cellValuesTsv.split("\n");
        String[][] cellValues = new String[lines.length][];

        for (int row = 0; row < cellValues.length; row++)
            cellValues[row] = lines[row].split("\t");

        assertTableRowsEqual(tableId, startRow, cellValues);
    }

    public void assertTableRowsEqual(String tableId, int startRow, String[][] cellValues)
    {
        for (int row = 0; row < cellValues.length; row++)
            for (int col = 0; col < cellValues[row].length; col++)
                assertTableCellTextEquals(tableId, row + startRow, col, cellValues[row][col]);
    }

    /**
     * @deprecated Use {@link org.labkey.test.util.DataRegionTable#getColumnDataAsText(int)}
     */
    @Deprecated
    // Returns the value of all cells in the specified column
    public List<String> getTableColumnValues(String tableName, int column)
    {
        int rowCount = getTableRowCount(tableName);

        List<String> values = new ArrayList<>(rowCount);

        for (int i = 0; i < rowCount; i++)
        {
            try
            {
                values.add(getTableCellText(Locator.id(tableName), i, column));
            }
            catch(NoSuchElementException ignore) {}
        }

        return values;
    }

    // Returns the number of rows (both <tr> and <th>) in the specified table
    public int getTableRowCount(String tableName)
    {
        return Locator.xpath("//table[@id=" + Locator.xq(tableName) + "]/thead/tr").findElements(getDriver()).size() +
                Locator.xpath("//table[@id=" + Locator.xq(tableName) + "]/tbody/tr").findElements(getDriver()).size();
    }

    /**
     * @deprecated Slow to return false. Use a specific Locator
     */
    @Deprecated
    public boolean isButtonPresent(String text)
    {
        try
        {
            findButton(text);
            return true;
        }
        catch (NoSuchElementException notPresent)
        {
            return false;
        }
    }

    public void clickButtonByIndex(String text, int index)
    {
        clickButtonByIndex(text, index, defaultWaitForPage);
    }

    public void clickButtonByIndex(String text, int index, int wait)
    {
        clickAndWait(findButton(text, index), wait);
    }

    public WebElement findButton(String text, int index)
    {
        Locator.XPathLocator locators = Locator.XPathLocator.union(
                // check for normal labkey button:
                Locator.lkButton(text).index(index),
                // check for Ext 4 button:
                Ext4Helper.Locators.ext4Button(text).index(index),
                // check for Ext button:
                Locator.extButton(text).index(index),
                // check for normal html button:
                Locator.button(text).index(index),
                // check for bootstrap button
                Locator.bootstrapButton(text).index(index)
        );

        try
        {
            return waitForElement(locators);
        }
        catch (NoSuchElementException notFound)
        {
            throw new NoSuchElementException("No button found with text \"" + text + "\" at index " + index, notFound);
        }
    }

    public WebElement findButton(String text)
    {
        Locator.XPathLocator locators = Locator.XPathLocator.union(
                // normal labkey nav button:
                Locator.lkButton(text),
                // Ext 4 button:
                Ext4Helper.Locators.ext4Button(text),
                // Ext 3 button:
                Locator.extButton(text),
                // normal HTML button:
                Locator.button(text),
                // GWT button:
                Locator.gwtButton(text),
                // bootstrap button
                Locator.bootstrapButton(text)
        );

        try
        {
            return waitForElement(locators);
        }
        catch (NoSuchElementException tryCaps)
        {
            if (!text.equals(text.toUpperCase()))
            {
                log("WARNING: Update test. Possible wrong casing for button: " + text);
                try
                {
                    return findButton(text.toUpperCase());
                }
                catch (NoSuchElementException capsFailed) {}
            }
            throw new NoSuchElementException("No button found with text " + text, tryCaps);
        }
    }

    protected WebElement findButtonContainingText(String text)
    {
        Locator.XPathLocator locators = Locator.XPathLocator.union(
                // normal labkey button:
                Locator.lkButton().containing(text),
                // Ext 4 button:
                Ext4Helper.Locators.ext4ButtonContainingText(text),
                // Ext 3 button:
                Locator.extButtonContainingText(text),
                // normal HTML button:
                Locator.buttonContainingText(text),
                // check for bootstrap button
                Locator.bootstrapButton().containing(text)
        );

        try
        {
            return waitForElement(locators);
        }
        catch (NoSuchElementException notFound)
        {
            throw new NoSuchElementException("No button found containing text " + text, notFound);
        }
    }

    /**
     * Wait for a button to appear, click it, then waits for the page to load.
     * Use clickButton(text, 0) to click a button and continue immediately.
     */
    public void clickButton(String text)
    {
        clickButton(text, defaultWaitForPage);
    }

    /**
     * Wait for a button to appear, click it, then waits for the text to appear.
     */
    public void clickButton(String text, String waitForText)
    {
        clickButton(text, 0);
        waitForText(waitForText);
    }

    /**
     * Wait for a button to appear, click it, then wait for <code>waitMillis</code> for the page to load.
     */
    public void clickButton(final String text, int waitMillis)
    {
        clickButton(text, waitMillis, 0);
    }

    /**
     * Wait for a button to appear, click it, then wait for <code>waitMillis</code> for the page to load.
     *     -- which is which button of this name (first, second, etc.)
     */
    public void clickButton(final String text, int waitMillis, int which)
    {
        clickAndWait(findButton(text, which), waitMillis);
    }

    /**
     * @deprecated Avoid me please. I am bad and hacky
     * Mash button until it goes stale and page loads
     */
    @Deprecated
    public void mashButton(final String text)
    {
        WebElement button = findButton(text);
        scrollIntoView(button);
        doAndWaitForPageToLoad(() -> shortWait().until(LabKeyExpectedConditions.clickUntilStale(button)));
    }

    public void clickButtonContainingText(String text)
    {
        clickButtonContainingText(text, defaultWaitForPage);
    }

    public void clickButtonContainingText(String text, int waitMills)
    {
        clickAndWait(findButtonContainingText(text), waitMills);
    }

    public void clickButtonContainingText(String buttonText, String textShouldAppearAfterLoading)
    {
        clickButtonContainingText(buttonText, 0);
        waitForText(defaultWaitForPage, textShouldAppearAfterLoading);
    }

    /**
     *  wait for element, click it, return immediately
     */
    public void waitAndClick(Locator l)
    {
        waitAndClick(WAIT_FOR_JAVASCRIPT, l, 0);
    }

    /**
     *  wait for element, click it, wait for page to load
     */
    public void waitAndClickAndWait(Locator l)
    {
        waitAndClick(WAIT_FOR_JAVASCRIPT, l, WAIT_FOR_PAGE);
    }

    /**
     *  wait for element, click it, wait for page to load
     */
    public void waitAndClick(int waitFor, Locator l, int waitForPageToLoad)
    {
        WebElement el = l.waitForElement(getDriver(), waitFor);
        try
        {
            clickAndWait(el, waitForPageToLoad);
        }
        catch (StaleElementReferenceException e)
        {
            // Locator.findElement likely didn't return the element we wanted due to timing problem. Wait and try again.
            // Ideally we'd decorate WebElement to know its locator and encapsulate the retry.
            sleep(500);
            el = l.findElement(getDriver());
            clickAndWait(el, waitForPageToLoad);
        }
    }

    /** @return target of link */
    public String getLinkHref(String linkText, String controller, String folderPath)
    {
        Locator link = Locator.linkWithText(linkText);
        String localAddress = getButtonHref(link, true);
        // IE puts the entire link in href, not just the local address
        if (localAddress.contains("/"))
        {
            int location = localAddress.lastIndexOf("/");
            if (location < localAddress.length() - 1)
                localAddress = localAddress.substring(location + 1);
        }
        return (WebTestHelper.getContextPath() + "/" + controller + folderPath + "/" + localAddress);
    }


    public String getButtonHref(Locator buttonLoc, boolean truncate)
    {
        String address = getAttribute(buttonLoc, "href");
        // IE puts the entire link in href, not just the local address
        if (truncate && address.contains("/"))
        {
            int location = address.lastIndexOf("/");
            if (location < address.length() - 1)
                address = address.substring(location + 1);
        }
        return address;
    }

    public void setFormElement(Locator l, String text)
    {
        WebElement el = l.waitForElement(new WebDriverWait(getDriver(), 5));
        setFormElement(el, text);
    }

    public void setFormElement(WebElement el, String text)
    {
        String inputType = el.getAttribute("type");

        if ("file".equals(inputType))
        {
            log("WARNING: Please use File object to set file input");
            setFormElement(el, new File(text));
            return;
        }

        if (isHtml5InputTypeSupported(inputType))
        {
            setHtml5Input(el, inputType, text);
        }
        else
        {
            setInput(el, text);
        }
    }

    private void setInput(WebElement input, String text)
    {
        if (StringUtils.isEmpty(text))
        {
            input.clear();
        }
        else if (!input.getTagName().equals("select") && text.length() < 1000 && !text.contains("\n") && !text.contains("\t"))
        {
            input.clear();
            input.sendKeys(text);
        }
        else
        {
            setFormElementJS(input, text);
        }

        String elementClass = input.getAttribute("class");
        if (elementClass.contains("gwt-TextBox") || elementClass.contains("gwt-TextArea") || elementClass.contains("x-form-text"))
            fireEvent(input, SeleniumEvent.blur); // Make GWT and ExtJS form elements behave better
    }

    private static final List<String> html5InputTypes = Arrays.asList("color", "date", "datetime-local", "email", "month", "number", "range", "search", "tel", "time", "url", "week");
    private final Map<String, Boolean> html5InputSupport = new HashMap<>(); // Don't make static. Different tests may run on different browsers
    protected final boolean isHtml5InputTypeSupported(String inputType)
    {
        if (!html5InputTypes.contains(inputType))
        {
            return false;
        }
        if (!html5InputSupport.containsKey(inputType))
        {
            html5InputSupport.put(inputType,
                    (Boolean) executeScript(
                            "var i = document.createElement('input');" +
                                    "i.setAttribute('type', arguments[0]);" +
                                    "return i.type === arguments[0];"
                            , inputType));
        }
        return html5InputSupport.get(inputType);
    }

    private void setHtml5Input(WebElement input, String inputType, String value)
    {
        switch (inputType)
        {
            case "date":
                setHtml5DateInput(input, value);
                break;
            case "password":
            case "search":
                setInput(input, value); // These don't require special handling, don't output warning
                break;
            default:
                log(String.format("WARNING: No special handling defined for HTML5 input type = '%s'.", inputType));
                setInput(input, value);
        }
    }

    private void setHtml5DateInput(WebElement el, String text)
    {
        String inputFormat = "yyyy-MM-dd";
        SimpleDateFormat inputFormatter = new SimpleDateFormat(inputFormat);

        try
        {
            setHtml5DateInput(el, inputFormatter.parse(text));
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Unable to parse date " + text + ". Format should be " + inputFormat);
        }
    }

    private void setHtml5DateInput(WebElement el, Date date)
    {
        String formFormat = "MMddyyyy";
        SimpleDateFormat formFormatter = new SimpleDateFormat(formFormat);
        String formDate = formFormatter.format(date);

        fireEvent(el, SeleniumEvent.focus);
        executeScript("arguments[0].value = ''", el);
        el.sendKeys(formDate);
    }

    /**
     * Set form element directly via JavaScript rather than WebElement.sendKeys
     * @param l Locator for form element
     * @param text text to set
     */
    public void setFormElementJS(Locator l, String text)
    {
        WebElement el = l.findElement(getDriver());
        setFormElementJS(el, text);
    }

    public void setFormElementJS(WebElement el, String text)
    {
        if ("select".equals(el.getTagName()))
        {
            try
            {
                selectOptionByText(el,text);
                log("WARNING: Use selectOptionByText(..) instead of setFormElement(..) for select inputs");
            }
            catch (NoSuchElementException x)
            {
                selectOptionByValue(el,text);
                log("WARNING: Use selectOptionByValue(..) instead of setFormElement(..) for select inputs");
            }
        }
        else
        {
            executeScript("arguments[0].value = arguments[1]", el, text);
        }
        fireEvent(el, SeleniumEvent.change);
    }

    public void setFormElement(Locator loc, File file)
    {
        WebElement el = loc.waitForElement(new WebDriverWait(getDriver(), 5));
        setFormElement(el, file);
    }

    public void setFormElement(WebElement el, File file)
    {
        assertTrue("File not found: " + file.toString(), file.exists());
        String cssString = "";
        String styleString = "";
        if (!el.isDisplayed())
        {
            cssString = el.getAttribute("class");
            styleString = el.getAttribute("style");
            log("Remove class so that WebDriver can interact with concealed form element");
            executeScript("arguments[0].setAttribute('class', '');arguments[0].setAttribute('style', '');", el);
        }

        el.sendKeys(FileUtil.getAbsoluteCaseSensitiveFile(file).getAbsolutePath());

        if (!cssString.isEmpty() || !styleString.isEmpty())
        {
            try
            {
                executeScript("arguments[0].setAttribute('class', arguments[1]);arguments[0].setAttribute('style', arguments[2])", el, cssString, styleString);
            }
            catch (StaleElementReferenceException ignore) {}
        }
    }

    public void setFormElements(String tagName, String formElementName, String[] values)
    {
        for (int i = 0; i < values.length; i++)
        {
            setFormElement(Locator.tagWithName(tagName, formElementName).index(i), values[i]);
        }
    }

    /**
     * @deprecated Use {@link Locator#findElements(SearchContext)}.size()
     */
    @Deprecated
    public int getElementCount(Locator locator)
    {
        return locator.findElements(getDriver()).size();
    }

    /**
     * @deprecated Use {@link #assertElementPresent(Locator)}
     */
    @Deprecated
    public void assertButtonPresent(String buttonText)
    {
        assertTrue("Button '" + buttonText + "' was not present", isButtonPresent(buttonText));
    }

    /**
     * @deprecated Use {@link #assertElementNotPresent(Locator)}. This is slow.
     */
    @Deprecated
    public void assertButtonNotPresent(String buttonText)
    {
        assertFalse("Button '" + buttonText + "' was present", isButtonPresent(buttonText));
    }

    public void checkRadioButton(Locator radioButtonLocator)
    {
        RadioButton(radioButtonLocator).find(getDriver()).check();
    }

    public void checkCheckbox(Locator checkBoxLocator)
    {
        WebElement checkbox = checkBoxLocator.findElement(getDriver());
        checkCheckbox(checkbox);
    }

    public void checkCheckbox(WebElement el)
    {
        setCheckbox(el, true);
    }

    public void uncheckCheckbox(WebElement el)
    {
        setCheckbox(el, false);
    }

    public void setCheckbox(WebElement el, boolean check)
    {
        String type = el.getAttribute("type");
        if ("radio".equals(type))
        {
            log("WARNING: Use checkRadioButton for radio buttons");
            if (!check)
                throw new IllegalArgumentException("Unable to deselect a radio button directly");
        }
        else if (!"checkbox".equals(type))
        {
            throw new IllegalArgumentException("Element not a checkbox: " + el.toString() + "\nTry Ext4Helper or ExtHelper.");
        }

        boolean selected = el.isSelected();
        if (check != selected)
            el.click();
    }

    public void setCheckbox(Locator checkBoxLocator, boolean check)
    {
        WebElement checkbox = checkBoxLocator.findElement(getDriver());
        setCheckbox(checkbox, check);
    }

    public void assertRadioButtonSelected(Locator radioButtonLocator)
    {
        assertTrue("Radio Button is not selected at " + radioButtonLocator.toString(), isChecked(radioButtonLocator));
    }

    public void uncheckCheckbox(Locator checkBoxLocator)
    {
        WebElement checkbox = checkBoxLocator.findElement(getDriver());
        uncheckCheckbox(checkbox);
    }

    public void assertChecked(Locator checkBoxLocator)
    {
        assertTrue("Checkbox not checked at " + checkBoxLocator.toString(), isChecked(checkBoxLocator));
    }

    public void assertNotChecked(Locator checkBoxLocator)
    {
        assertFalse("Checkbox checked at " + checkBoxLocator.toString(), isChecked(checkBoxLocator));
    }

    public boolean isChecked(Locator checkBoxLocator)
    {
        return checkBoxLocator.findElement(getDriver()).isSelected();
    }

    public void selectOptionByValue(Locator locator, String value)
    {
        WebElement selectElement = locator.findElement(getDriver());
        selectOptionByValue(selectElement, value);
    }

    public void selectOptionByValue(WebElement selectElement, String value)
    {
        Select select = new Select(selectElement);
        select.selectByValue(value);
    }

    public void selectOptionByText(Locator locator, String text)
    {
        WebElement selectElement = locator.findElement(getDriver());
        selectOptionByText(selectElement, text);
    }

    public void selectOptionByText(WebElement selectElement, String value)
    {
        Select select = new Select(selectElement);
        select.selectByVisibleText(value);
    }

    public void selectOptionByTextContaining(WebElement selectElement, String value)
    {
        Select select = new Select(selectElement);
        List<WebElement> options = select.getOptions();
        List<String> matches = new ArrayList<>();

        for (WebElement option : options)
        {
            String optionText = option.getText();
            if (optionText.contains(value))
                matches.add(optionText);
        }

        if (matches.size() == 1)
            select.selectByVisibleText(matches.get(0));
        else if (matches.isEmpty())
            select.selectByVisibleText(value);
        else
            fail("Too many matches for '" + value + "': ['" + StringUtils.join(matches, "', '") + "']");

    }
    public void addUrlParameter(String parameter)
    {
        addUrlParameter(parameter, defaultWaitForPage);
    }

    public void addUrlParameter(String parameter, int mils)
    {
        String currentURL = getCurrentRelativeURL();
        // Strip off any '#' on the end of the URL
        String suffix = "";
        if (currentURL.contains("#"))
        {
            String[] parts = currentURL.split("#");
            currentURL = parts[0];
            // There might not be anything after the '#'
            suffix = "#" + (parts.length > 1 ? parts[1] : "");
        }
        if (!currentURL.contains(parameter))
        {
            if (currentURL.contains("?"))
            {
                if (currentURL.indexOf("?") == currentURL.length() - 1)
                    beginAt(currentURL.concat(parameter + suffix), mils);
                else
                    beginAt(currentURL.concat("&" + parameter + suffix), mils);
            }
            else
                beginAt(currentURL.concat("?" + parameter + suffix), mils);
        }
    }

    public String getUrlParam(String paramName)
    {
        return getUrlParam(paramName, false);
    }

    public String getUrlParam(String paramName, boolean decode)
    {
        Map<String, String> params = getUrlParameters();
        String paramValue = params.get(paramName);

        if (paramValue != null && decode)
        {
            paramValue = paramValue.replace("+", "%20");
            try
            {
                paramValue = URLDecoder.decode(paramValue, "UTF-8");
            } catch(UnsupportedEncodingException ignore) {}
        }

        return paramValue;
    }

    public Map<String, String> getUrlParameters()
    {
        Map<String, String> params = new HashMap<>();
        String urlQuery = getURL().getQuery();
        if (urlQuery != null)
        {
            String[] urlParams = urlQuery.split("&");
            for (String param : urlParams)
            {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2)
                    params.put(keyValue[0].trim(), keyValue[1].trim());
                else if (keyValue.length == 1)
                    params.put(keyValue[0], "");
                else
                    log("Unable to parse url parameter: " + param);
            }
        }
        return params;
    }

    public void assertAttributeEquals(Locator locator, String attributeName, String value)
    {
        String actual = getAttribute(locator, attributeName);
        assertEquals(value, actual);
    }

    public void assertAttributeEquals(WebElement element, String attributeName, String value)
    {
        String actual = element.getAttribute(attributeName);
        assertEquals(value, actual);
    }

    public void assertAttributeContains(Locator locator, String attributeName, String value)
    {
        String actual = getAttribute(locator, attributeName);
        assertTrue("Expected attribute '" + locator + "@" + attributeName + "' value to contain '" + value + "', but was '" + actual + "' instead.", actual != null && actual.contains(value));
    }

    public void assertAttributeContains(WebElement element, String attributeName, String value)
    {
        String actual = element.getAttribute(attributeName);
        assertTrue("Expected attribute '" + element + "@" + attributeName + "' value to contain '" + value + "', but was '" + actual + "' instead.", actual != null && actual.contains(value));
    }

    public void assertAttributeNotContains(Locator locator, String attributeName, String value)
    {
        String actual = getAttribute(locator, attributeName);
        assertTrue("Expected attribute '" + locator + "@" + attributeName + "' value to not contain '" + value + "', but was '" + actual + "' instead.", actual != null && !actual.contains(value));
    }

    public void assertAttributeNotContains(WebElement element, String attributeName, String value)
    {
        String actual = element.getAttribute(attributeName);
        assertTrue("Expected attribute '" + element + "@" + attributeName + "' value to not contain '" + value + "', but was '" + actual + "' instead.", actual != null && !actual.contains(value));
    }

    public String getAttribute(Locator locator, String attributeName)
    {
        return locator.findElement(getDriver()).getAttribute(attributeName);
    }

    public int getDefaultWaitForPage()
    {
        return defaultWaitForPage;
    }

    public void setDefaultWaitForPage(int defaultWaitForPage)
    {
        this.defaultWaitForPage = defaultWaitForPage;
    }

    public String getHtmlSource()
    {
        return getDriver().getPageSource();
    }

    public boolean isExtTreeNodeSelected(String nodeCaption)
    {
        Locator loc = Locator.xpath("//div[contains(./@class,'x-tree-selected')]/a/span[text()='" + nodeCaption + "']");
        return isElementPresent(loc);
    }

    public boolean isExtTreeNodeExpanded(String nodeCaption)
    {
        Locator loc = Locator.xpath("//div[contains(./@class,'x-tree-node-expanded')]/a/span[text()='" + nodeCaption + "']");
        return isElementPresent(loc);
    }

    public void pressTab(Locator l)
    {
        WebElement el = l.findElement(getDriver());
        el.sendKeys(Keys.TAB);
    }

    public void pressEnter(Locator l)
    {
        WebElement el = l.findElement(getDriver());
        el.sendKeys(Keys.ENTER);
    }

    public void pressDownArrow(Locator l)
    {
        WebElement el = l.findElement(getDriver());
        el.sendKeys(Keys.DOWN);
    }

    public void pressUpArrow(Locator l)
    {
        WebElement el = l.findElement(getDriver());
        el.sendKeys(Keys.UP);
    }

    public void setCodeEditorValue(String id, String value)
    {
        _extHelper.setCodeMirrorValue(id, value);
    }

    public void waitForElements(final Locator loc, final int count)
    {
        waitForElements(loc, count, WAIT_FOR_JAVASCRIPT);
    }

    public void waitForElements(final Locator loc, final int count, int wait)
    {
        waitFor(() -> count == loc.findElements(getDriver()).size(), wait);
        assertEquals("Element not present expected number of times", count, loc.findElements(getDriver()).size());
    }
}
