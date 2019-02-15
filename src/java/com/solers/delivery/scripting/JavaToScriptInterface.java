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
package com.solers.delivery.scripting;

import com.solers.delivery.scripting.event.DeliveryEventConsumer;

/**
 * This interface expose the java event producer to scripting 
 */
public interface JavaToScriptInterface {
    public void addConsumer(DeliveryEventConsumer deliveryEventConsumer);
    public void startListening(long timeout) throws ScriptException;
    public void stopListening(DeliveryEventConsumer deliveryEventConsumer) throws ScriptException;
    public String[] getArguments();
}
