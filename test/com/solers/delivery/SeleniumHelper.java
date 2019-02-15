/****************************************************************
 *
 * Solers, Inc. as the author of Enterprise File Delivery 2.1 (EFD 2.1)
 * source code submitted herewith to the Government under contract
 * retains those intellectual property rights as set forth by the Federal 
 * Acquisition Regulations agreement (FAR). The Government has 
 * unlimited rights to redistribute copies of the EFD 2.1 in 
 * executable or source format to support operational installation 
 * and software maintenance. Additionally, the executable or 
 * source may be used or modified for by third parties as 
 * directed by the government.
 *
 * (c) 2009 Solers, Inc.
 ***********************************************************/
package com.solers.delivery;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;

import com.solers.delivery.Waiter;
import com.solers.delivery.content.AllowedHostService;
import com.solers.delivery.domain.AllowedHost;
import com.solers.delivery.domain.User;
import com.solers.delivery.user.PasswordService;
import com.solers.delivery.user.UserService;
import com.thoughtworks.selenium.Selenium;

public class SeleniumHelper {

    private static final Logger log = Logger.getLogger(SeleniumHelper.class);

    protected UserService userService;
    protected PasswordService passwordService;
    protected AllowedHostService allowedHostService;

    private final String username;
    private final String password;
    private final String adminUsername;
    private final String adminUserPassword;

    protected User user;
    protected User adminUser;

    protected File supplierContent;
    protected File consumerContent;
    protected final AllowedHost allowedHost;
    private AccessibleSeleneseTestBase stb;


    public SeleniumHelper() {
        username = "webUser";
        password = "!@#$%^&*()QWErt";
        adminUsername = "adminUser";
        adminUserPassword = "!@#$%^&*()QWErt";
        allowedHost = new AllowedHost("alias", "cs-test");
    }

    public void initializeServices(UserService userService, PasswordService passwordService, AllowedHostService allowedHostService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.allowedHostService = allowedHostService;
        addDefaultAllowedHost();
    }

    public void init() throws Exception {
        createWebappWorkdir();
        createTestSuppliersAndConsumers();
        stb = new AccessibleSeleneseTestBase();
    }

    public void start() throws Exception {
        if (log.isTraceEnabled())
            log.trace(String.format("Initializing Selenium test on server https://localhost:%s/", System.getProperty("web.port")));
        stb.setUp(String.format("https://localhost:%s/", System.getProperty("web.port")), "*chrome");
    }

    public void stop() throws IOException {
        getSelenium().stop();
        if (supplierContent != null && supplierContent.exists()) {
            FileUtils.deleteDirectory(supplierContent);
        }
        if (consumerContent != null && consumerContent.exists()) {
            FileUtils.deleteDirectory(consumerContent);
        }
    }

    /**
     * Convenience method that simply use the test user/password to login
     * @throws InterruptedException
     */
    public void login() throws InterruptedException {
        this.login(this.getTestUser(), this.getTestUserPassword());
    }
    
    /**
     * Convenience method that simply use the test admin user/password to login
     * @throws InterruptedException
     */
    public void loginAdmin() throws InterruptedException {
        this.login(this.getTestAdminUser(), this.getTestUserPassword());
    }
    
    /**
     * Logs the given EFD user in <br/>
     * 
     * Opens the base url for the EFD webapp <br />
     * Waits up to 60 seconds for the login form to be displayed<br />
     * Enter the given username into the username textfield <br />
     * Enter the given password into the password textfield <br />
     * Clicks the login button<br />
     * Waits up to 60 seconds for the DoD banner to be displayed <br />
     * Clicks the "OK" button on the DoD banner <br />
     * then returns
     * 
     * @throws InterruptedException
     */
    public void login(User user, String password) throws InterruptedException {
        getSelenium().open("/");

        new Waiter(60) { 
            public boolean condition() {
                return getSelenium().isVisible("username");
            }
        }.doWait();

        getSelenium().type("username", user.getUsername());
        getSelenium().type("password", password);
        getSelenium().click("//table[@id='login-button']");

        new Waiter(60) {
            public boolean condition() {
                return getSelenium().isVisible("//table[@id='dod-banner-ok']");
            }
        }.doWait();

        getSelenium().click("//table[@id='dod-banner-ok']");

        String statusBarText = getSelenium().getText("//div[@id='user-statusbar']//div[contains(@class,'x-status-text')]");

        Assert.assertTrue("After login the navigator should be displayed", getSelenium().isVisible("navigation-navigator"));
        Assert.assertTrue("After login, the status bar text should start with 'Welcome firstname lastname.'", isStatusBarTextCorrectAfterLogin(user, statusBarText));
    }

    /**
     * Logs the current EFD user out <br/>
     * 
     * Clicks the logout button <br />
     * Waits up to 60 seconds for the "Click OK to log out" dialog to display <br />
     * Clicks the "OK" button on that dialog <br />
     * Waits up to 60 seconds until the login dialog to display <br />
     * then returns
     * 
     * @throws InterruptedException
     */
    public void logout() throws InterruptedException {

        getSelenium().click("//div[@id='user-statusbar']//*[contains(@class,'logout-button')]");

        getSelenium().waitForCondition(declareGlobals("Ext") + "Ext.Msg.getDialog().isVisible()", "1000");
        getSelenium().click("dom=" + executeInAnonymousFunction(declareGlobals("Ext") + dismissExtMsgDialog()));

        new Waiter(60) {
            public boolean condition() {
                return getSelenium().isVisible("username");
            }
        }.doWait();

        Assert.assertTrue("After logout, the login form should be displayed", getSelenium().isVisible("username"));
    }

    private boolean isStatusBarTextCorrectAfterLogin(User user, String text) {
        String f = user.getFirstName(), l = user.getLastName();
        return text != null && text.startsWith(String.format("Welcome %s %s.", f == null ? "" : f, l == null ? "" : l));
    }

    /**
     * Creates javascript to make it easier to reference global objects like <br/>
     * currentWindow and, when "Ext" is passed as a parameter, the Ext global object <br/>
     * 
     * @param globals
     * @return
     */
    public CharSequence declareGlobals(String... globals) {
        StringBuilder js = new StringBuilder();
        js.append(declareCurrentWindow());
        if (globals != null) {
            for (String global : globals) {
                js.append("if (!" + global + ") {var " + global + "=CurrentWindow." + global + ";}");
            }
        }
        return js;
    }

    public String declareCurrentWindow() {
        return "var CurrentWindow, c = selenium.browserbot.getCurrentWindow(); c = c.wrappedJSObject ? c.wrappedJSObject : c; CurrentWindow = c;";
    }

    /**
     * Dismisses the global Ext message dialog <br/>
     * 
     * Note: Ext.Msg.getDialog() returns a dialog that Ext implements as a singleton, so <br/>
     * a unique id should not and cannot be assigned to the OK button. The Ext.query call returns<br/>
     * the button to click
     */
    public String dismissExtMsgDialog() {
        return "var dialog = Ext.Msg.getDialog(); return Ext.query(\"button[class*='x-btn-text']:nodeValue(OK)\", dialog.getEl().dom)[0];";
    }

    /**
     * Creates javascript that will wrap the given javascript block in an anonymous function and execute it
     * 
     * @param body
     * @return
     */
    public CharSequence executeInAnonymousFunction(String body) {
        StringBuilder js = new StringBuilder("(function(){ ");
        js.append(body);
        js.append(" })();");
        return js;
    }

    public Selenium getSelenium() {
        return stb.getSelenium();
    }

    public String getAdminUserName() {
        return this.adminUsername;
    }
    
    public String getTestUsername() {
        return username;
    }

    public String getAdminUserPassword() {
        return this.adminUserPassword;
    }
    
    public String getTestUserPassword() {
        return password;
    }

    public User getTestUser() {
        if (user == null) {
            user = createWebUserAndSetPassword(this.username, this.password, false);
        }
        return user;
    }
    
    public User getTestAdminUser() {
        if (this.adminUser == null) {
            this.adminUser = this.createWebUserAndSetPassword(this.adminUsername, this.adminUserPassword, true);
        }
        return this.adminUser;
    }

    public File getSupplierContent() throws IOException {
        return supplierContent;
    }

    public File getConsumerContent() throws IOException {
        return consumerContent;
    }

    protected AllowedHost getAllowedHost() {
        return allowedHost;
    }

    public void addDefaultAllowedHost() {
        if (allowedHostService.get(allowedHost.getAlias()) == null) {
            allowedHostService.save(allowedHost);
        }
    }

    public User createWebUserAndSetPassword(String username, String password, boolean isAdmin) {
        User user = userService.get(username);
        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setFirstName("firstName");
            user.setLastName("lastName");
            user.setLastLogin(Calendar.getInstance().getTime());
            user.setInitialPassword(false);
            user.setAdminRole(isAdmin);
            user.setFailedLogins(0);
            user.setEnabled(true);
            userService.save(user);
            passwordService.updatePassword(username, password);
        }
        return user;
    }

    private void createWebappWorkdir() {
        // "intSite/workdir/webapp" will have been created in ant, but make sure that it is there
        // so that this test case can be run from eclipse
        File webappDir = new File(System.getProperty("efd.home"), "intSite/workdir/webapp");
        webappDir.mkdirs();
    }

    private void createTestSuppliersAndConsumers() throws IOException {
        supplierContent = new File(System.getProperty("java.io.tmpdir"), "intData/supplier");
        supplierContent.mkdirs();

        for (int i = 0; i < 100; i++) {
            File f = new File(supplierContent, "content-" + i);
            Writer fileWriter = new FileWriter(f);
            IOUtils.write("hello world " + i, fileWriter);
            fileWriter.close();
        }

        consumerContent = new File(System.getProperty("java.io.tmpdir"), "intData/consumer");
        consumerContent.mkdirs();
    }

}
