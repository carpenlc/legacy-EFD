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
package com.solers.delivery.web.combine;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ExtCssProcessorTestCase {
    
    @Test
    public void testProcess() {
        Processor p = new ExtCssProcessor();
        
        String input = ".x-mask-loading div{padding:5px 10px 5px 25px;background:#fbfbfb url( '../images/default/grid/loading.gif' ) no-repeat 5px 5px;line-height:16px;}";
        String expected = ".x-mask-loading div{padding:5px 10px 5px 25px;background:#fbfbfb url( '/ext/resources/images/default/grid/loading.gif' ) no-repeat 5px 5px;line-height:16px;}";
        
        Assert.assertEquals(expected, p.process(input));
    }
    
}
