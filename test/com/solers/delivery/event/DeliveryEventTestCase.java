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
package com.solers.delivery.event;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.GeneralSecurityException;

import javax.crypto.IllegalBlockSizeException;

import junit.framework.TestCase;

import com.solers.delivery.content.status.ConsumerProgress;
import com.solers.delivery.event.ContentEvent.ContentEventAction;
import com.solers.delivery.event.ContentEvent.ContentEventResult;

public class DeliveryEventTestCase extends TestCase {

    public void testEventsAreSerializable() throws IllegalBlockSizeException, IOException, GeneralSecurityException {
        this.serializationTester(new ContentEvent(-1L));
        this.serializationTester(new GBSUpdateCompleteEvent(-1L, null));
        this.serializationTester(new PendingGBSUpdateEvent(-1L, "", null, null, -1, ContentEventAction.ADD, ContentEventResult.DELETE_FAILED));
        this.serializationTester(this.getSyncronizationEvent());
    }

    public void testTransientFields() throws IOException, ClassNotFoundException {
        SynchronizationEvent synchronizationEvent = this.getSyncronizationEvent();
        assertNotNull(synchronizationEvent.getProgress());
        
        byte[] bytes = this.toByteArray(synchronizationEvent);
        
        synchronizationEvent = this.toObject(bytes);
        assertNull(synchronizationEvent.getProgress());
        
        ContentEvent originalContentEvent = new ContentEvent(-1L);
        originalContentEvent.setMessage("foo");
        
        assertNotNull(originalContentEvent.getMessage());
        
        bytes = this.toByteArray(originalContentEvent);
        
        ContentEvent newContentEvent = this.toObject(bytes);
        assertNull(newContentEvent.getMessage());
    }
    
    private void serializationTester(Serializable obj) throws IOException {
        assertTrue(this.toByteArray(obj).length > 0);
    }
    
    private byte[] toByteArray(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        byte[] array = bos.toByteArray();
        return array;
    }

    @SuppressWarnings("unchecked")
    private <T> T toObject(byte[] array) throws IOException, ClassNotFoundException {
        Object obj =  new ObjectInputStream(new ByteArrayInputStream(array)).readObject(); 
        return (T)obj;
    }
    
    private SynchronizationEvent getSyncronizationEvent() {
        SynchronizationEvent event = new SynchronizationEvent(-1L);
        event.setProgress(new ConsumerProgress());
        return event;
    }
}
