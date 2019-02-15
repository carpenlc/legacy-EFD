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
package com.solers.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class StreamCopierTestCase {
    
    @Test
    public void testCopy() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream("test".getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        StreamCopier copier = new StreamCopier(input, output);
        
        long bytes = copier.copy();
        
        Assert.assertEquals(4, bytes);
        Assert.assertEquals("test", new String(output.toByteArray()));
    }
    
    public void testCopyWithCallback() throws IOException {
        ByteArrayInputStream input = new ByteArrayInputStream("test".getBytes());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        StreamCopier copier = new StreamCopier(input, output);
        
        final AtomicInteger counter = new AtomicInteger(0);
        Callback<Integer> cb = new Callback<Integer>() {
            public void call(Integer e) {
                counter.addAndGet(e);
            }
        };
        
        long bytes = copier.copy(cb);
        
        Assert.assertEquals(4, bytes);
        Assert.assertEquals(4, counter.get());
        Assert.assertEquals("test", new String(output.toByteArray()));
    }
    
    @Test
    public void testStop() throws Exception {
        InputStream input = new NeverEndingInputStream();
        OutputStream output = new DoNothingOutputStream();
        
        final StreamCopier copier = new StreamCopier(input, output);
        
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            public void run() {
                try {
                    copier.copy();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });
        
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);
        e.schedule(new Runnable() {
            public void run() {
                copier.stop();
            }
        }, 500, TimeUnit.MILLISECONDS);
        
        es.shutdown();
        es.awaitTermination(5, TimeUnit.MINUTES);
    }
    
    @Test
    public void testInterrupt() throws Exception {
        
        final ExecutorService es = Executors.newSingleThreadExecutor();
        es.submit(new Runnable() {
            public void run() {
                InputStream input = new NeverEndingInputStream();
                OutputStream output = new DoNothingOutputStream();
                StreamCopier copier = new StreamCopier(input, output);
                try {
                    copier.copy();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });
        
        ScheduledExecutorService e = Executors.newScheduledThreadPool(1);
        e.schedule(new Runnable() {
            public void run() {
                es.shutdownNow();
            }
        }, 500, TimeUnit.MILLISECONDS);
        
        es.awaitTermination(5, TimeUnit.MINUTES);
    }
    
    private static class NeverEndingInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            return 0;
        }
        
    }
    
    private static class DoNothingOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            
        }
        
    }
}
