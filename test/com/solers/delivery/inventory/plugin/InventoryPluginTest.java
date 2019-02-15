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
package com.solers.delivery.inventory.plugin;


import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.solers.delivery.inventory.Inventory;
import com.solers.delivery.inventory.MockInventory;
import com.solers.delivery.inventory.MockNode;
import com.solers.delivery.inventory.MockUtil;
import com.solers.delivery.inventory.fs.FileSystemInventoryProvider;
import com.solers.delivery.inventory.plugin.provider.InventoryProvider;
import com.solers.delivery.inventory.plugin.provider.Parameter;
import com.solers.delivery.inventory.plugin.provider.ProviderInfo;
import com.solers.delivery.inventory.plugin.provider.StandardProviderInfo;

public class InventoryPluginTest extends TestCase {
    private final ClassLoader context = Thread.currentThread().getContextClassLoader();
    private final String mockImpl = MockInventoryProvider.class.getName();
    
    public void setUp() {
        InventoryPlugin.clearPlugins();
    }
    
    private static File find(String name) {        
        try {
            URL u = InventoryPluginTest.class.getResource("/plugin/" + name);
            File f = new File(u.toURI());
            return f;
        } catch (Exception e) {
            e.printStackTrace();
            fail();
            return null;
        }
    }
    
    public void testClearPlugins() {
        Assert.assertTrue(InventoryPlugin.registeredPlugins.isEmpty());
        InventoryPlugin.registeredPlugins.put("test", new MockInventoryProvider());
        Assert.assertEquals(1, InventoryPlugin.registeredPlugins.size());
        InventoryPlugin.clearPlugins();
        Assert.assertTrue(InventoryPlugin.registeredPlugins.isEmpty());
    }
    
    public void testGetImplementationMainPath() {
        try {
            Class<? extends InventoryProvider> impl = InventoryPlugin.getImplementation(
                MockUtil.createResource(mockImpl),
                context);
            Assert.assertNotNull(impl);
            Assert.assertEquals(mockImpl, impl.getName());
        } catch (PluginException pe) {
            Assert.fail();
        }
        
    }
    
    public void testGetImplementationBadURL() {
        try {
            InventoryPlugin.getImplementation(
                new File("thisMostCertainlyDoesNotExistAsnocmQWVogTE23491").toURI().toURL(),
                context);
            fail("File not found should fail.");
        } catch (MalformedURLException mue) {
            fail("Should not fail setting up test.");
        } catch (PluginException pe) {
            Assert.assertTrue(pe.getCause() instanceof IOException);
        }
    }
    
    public void testGetImplementationEmptyFile() {
        try {
            InventoryPlugin.getImplementation(
                MockUtil.createResource(""),
                context);
            fail("Should not accept an empty file.");
        } catch (PluginException pe) {
            Assert.assertTrue(pe instanceof PluginLoadException);
        }
    }
    
    public void testGetImplementationBadClass() {
        try {
            InventoryPlugin.getImplementation(
                MockUtil.createResource("this.class.does.not.Exist"),
                context);
            fail("Should not load nonexistent classes without error");
        } catch (PluginException pe) {
            Assert.assertTrue(pe.getCause() instanceof ClassNotFoundException);
        }
    }
    
    public void testGetImplementationWrongClass() {
        try {
            InventoryPlugin.getImplementation(
                MockUtil.createResource("java.lang.String"), context);
            fail("Should ensure class is correct implemntation interface.");
        } catch (PluginException pe) {
            
        }
    }
    
    public void testGetInstanceNoProvidersAvailable() {
        try {
            Assert.assertEquals(0, InventoryPlugin.getProviderInfo().size());
            InventoryPlugin.newInstance(null, new HashMap<String, Object>(0));
            fail();
        } catch (PluginException pe) {
            Assert.assertTrue(pe instanceof PluginProviderNotFoundException);
        }
    }
    
    public void testGetInstanceAcceptThrowsRuntimeException() {
        try {
            Assert.assertFalse(AcceptException.called);
            Assert.assertEquals(0, InventoryPlugin.getProviderInfo().size());
            InventoryPlugin.register(AcceptException.class);
            InventoryPlugin.newInstance(null, null);
        } catch (PluginException ppnfe) {
            assertTrue(ppnfe instanceof PluginProviderNotFoundException);
        } catch (RuntimeException e) {
            fail("Do not allow runtime exceptions to bubble out of 3rd party inventory providers.");
        }
        Assert.assertTrue(AcceptException.called == true);
        AcceptException.called = false;
    }
    
    public void testGetInstanceMainPath() {
        try {
            InventoryPlugin.register(MockInventoryProvider.class);
            //mock inventory accepts everything
            Inventory i = InventoryPlugin.newInstance(null, new HashMap<String, Object>(0));
            Assert.assertNotNull(i);
            Assert.assertTrue(i instanceof MockInventory);
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testGetInstanceExecutionException() {
        try {
            InventoryPlugin.register(GetInstanceException.class);
            InventoryPlugin.newInstance(null, new HashMap<String, Object>(0));
            fail();
        } catch (PluginException pe) {
            Assert.assertTrue(pe instanceof PluginExecutionException);
            Assert.assertTrue(pe.getCause() instanceof NullPointerException);
        }
    }
    
    public void testGetProviderInfo() {
        assertEquals(0, InventoryPlugin.getProviderInfo().size());
        try {
            InventoryPlugin.register(MockInventoryProvider.class);
            assertEquals(1, InventoryPlugin.getProviderInfo().size());
            for (ProviderInfo pi : InventoryPlugin.getProviderInfo()) {
                assertEquals(pi, MockInventoryProvider.info);
            }
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testNewJarFileClassLoaderCorruptPlugin() {
        File corrupt = find("corrupt.jar");
        try {
            InventoryPlugin.newJarFileClassLoader(corrupt);
            fail();
        } catch (PluginException pe) {
        }
    }
    
    public void testNewJarFileClassLoaderNullFile() {
        try {
            InventoryPlugin.newJarFileClassLoader(null);
            fail();
        } catch (PluginException pe) {}
    }
    
    public void testNewJarFileClassLoaderAbsentFile() {
        try {
            InventoryPlugin.newJarFileClassLoader(new File("aaoToenuh4352987aolgqvwmbxbgfONITC"));
            fail();
        } catch (PluginException pe) {}
    }
    
    public void testNewJarFileClassLoaderNotFile() {
        File t = new File("./", "testNewJarFileClassLoaderNotFile");
        Assert.assertTrue(t.mkdir());
        Assert.assertTrue(t.isDirectory());
        t.deleteOnExit();
        try {
            InventoryPlugin.newJarFileClassLoader(t);
            fail();
        } catch (PluginException pe) {
            
        } finally {
            t.delete();
        }
    }
    
    public void testNewJarFileClassLoaderValidJar() {
        try {
            File f = find("not_plugin.jar");
            URLClassLoader cl = InventoryPlugin.newJarFileClassLoader(f);
            Assert.assertNotNull(cl);
            URL u = cl.findResource("test");
            LineNumberReader lnr = new LineNumberReader(new InputStreamReader(u.openStream()));
            String line = lnr.readLine();
            Assert.assertNotNull(line);
            Assert.assertEquals("success", line);
        } catch (PluginException pe) {
            fail();
        } catch (IOException ioe) {
            fail();
        }
    }
    
    public void testNewJarFileClassLoaderWithParent() {
        final AtomicBoolean called = new AtomicBoolean(false);
        File f = find("not_plugin.jar");
        try {
            URLClassLoader cl = InventoryPlugin.newJarFileClassLoader(f,
                new ClassLoader() {
                    @Override
                    protected Class<?> findClass(String name) throws ClassNotFoundException {
                        called.set(true);
                        return super.findClass(name);
                    }
                    @Override
                    public Class<?> loadClass(String name) throws ClassNotFoundException {
                        called.set(true);
                        return super.loadClass(name);
                    }
                }
            );
            try {
                Assert.assertFalse(called.get());
                cl.loadClass("whatever");
            } catch (ClassNotFoundException cnfe) {}
            Assert.assertTrue(called.get());
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testNewProviderInstanceMainPath() {
        try {
            InventoryProvider ip = InventoryPlugin.newProviderInstance(MockInventoryProvider.class);
            Assert.assertNotNull(ip);
            Assert.assertEquals(MockInventoryProvider.info, ip.getProviderInfo());
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testNewProviderInstanceIllegalAccess() {
        try {
            InventoryPlugin.newProviderInstance(PrivateProvider.class);
            fail();
        } catch (PluginException pe) {
            Assert.assertTrue(pe.getCause() instanceof IllegalAccessException);
        }
    }
    
    public void testNewProviderInstanceNoDefaultConstructor() {
        try {
            InventoryPlugin.newProviderInstance(CannotInstantiateProvider.class);
            fail();
        } catch (PluginException pe) {
            Assert.assertTrue(pe.getCause() instanceof InstantiationException);
        }
    }
    
    public void testNewProviderInstanceExceptionDuringConstruction() {
        try {
            InventoryPlugin.newProviderInstance(ErrorDuringConstruction.class);
            fail();
        } catch (PluginException pe) {
            Assert.assertTrue(pe instanceof PluginLoadException);
            Assert.assertTrue(pe.getCause() instanceof IOException);
        }
    }
    
    public void testRegister() {
        Assert.assertTrue(InventoryPlugin.registeredPlugins.isEmpty());
        try {
            ProviderInfo pi = InventoryPlugin.register(MockInventoryProvider.class);
            Assert.assertNotNull(pi);
            Assert.assertEquals("mock", pi.getName());
        } catch (PluginException pe) {
            fail();
        }
    }

    public void testRegisterPluginNotAPlugin() {
        File f = find("not_plugin.jar");
        try {
            InventoryPlugin.register(f);
            fail();
        } catch (PluginException pe) {
            
        }
    }
    
    public void testRegisterPluginMainPath() {
        File f = find("mock_plugin.jar");
        try {
            Assert.assertEquals(0, InventoryPlugin.getProviderInfo().size());
            InventoryPlugin.register(f);
            Assert.assertEquals(1, InventoryPlugin.getProviderInfo().size());
            for (ProviderInfo pi : InventoryPlugin.getProviderInfo()) {
                Assert.assertEquals(MockInventoryProvider.info, pi);
            }
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testRegisterPluginDuplicatePlugin() {
        File f = find("mock_plugin.jar");
        try {
            Assert.assertEquals(0, InventoryPlugin.getProviderInfo().size());
            InventoryPlugin.register(f);
            Assert.assertEquals(1, InventoryPlugin.getProviderInfo().size());
            InventoryProvider p = InventoryPlugin.registeredPlugins.values().iterator().next();
            InventoryPlugin.register(f);
            Assert.assertEquals(1, InventoryPlugin.getProviderInfo().size());
            Assert.assertSame(p, InventoryPlugin.registeredPlugins.values().iterator().next());
        } catch (PluginException pe) {
            fail();
        }
    }
    
    public void testRegisterBadPluginImplementation() {
        try {
            InventoryPlugin.register(find("invalid_plugin_bad_impl.jar"));
            fail();
        } catch (PluginException pe) {
        }
    }
    
    public void testRegisterCorruptJarFile() {
        try {
            InventoryPlugin.register(find("corrupt.jar"));
            fail();
        } catch (PluginException pe) {
        }
    }
    
    public void testDetectFilesystemPlugin() {
        ProviderInfo expected = new FileSystemInventoryProvider().getProviderInfo();
        assertFalse(InventoryPlugin.getProviderInfo().contains(expected));
        try {
            InventoryPlugin.register(FileSystemInventoryProvider.class);
        } catch (Exception e) {
            fail();
        }
        assertTrue(InventoryPlugin.getProviderInfo().contains(expected));
    }
        
    public static class MockInventoryProvider implements InventoryProvider {
        protected static final ProviderInfo info = 
            new StandardProviderInfo("Solers, Inc.", "mock", "1.0");
        
        @Override
        public boolean accept(URI resource) {
            return true;
        }

        @Override
        public ProviderInfo getProviderInfo() {
            return info;
        }

        @Override
        public Inventory newInstance(URI resource, Map<String, Object> parameters) {
            return new MockInventory(new MockNode("root", true));
        }
        
        @Override
        public Collection<Parameter> getParameterInfo() {
            return Collections.emptySet();
        }
        
    }
    
    private static class PrivateProvider extends MockInventoryProvider {}
    public static class CannotInstantiateProvider extends MockInventoryProvider {
        public CannotInstantiateProvider(String param) {}
    }
    public static class ErrorDuringConstruction extends MockInventoryProvider {
        public ErrorDuringConstruction() throws IOException {
            throw new IOException();
        }
    }
    public static class GetInstanceException extends MockInventoryProvider {
        @Override
        public Inventory newInstance(URI resource, Map<String, Object> parameters) {
            throw new NullPointerException();
        }
    }
    public static class AcceptException extends MockInventoryProvider {
        public static boolean called = false;
        @Override
        public boolean accept(URI resource) {
            called = true;
            throw new RuntimeException("Catch me!");            
        }
    }
}
