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
package com.solers.delivery.install;

import java.util.Vector;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class ConfigureMetricsTask extends Task {
    
    @Override
    public void execute() {
        boolean enabled = false;
        String host = "localhost";
        String port = "0";
        
        enabled = bool("Do you want to enable remote metrics collection?");
        if (enabled) {
            host = string("Remote host");
            port = integer("Remote port");
        }
        
        getProject().setProperty("metrics.remote.enabled", String.valueOf(enabled));
        getProject().setProperty("metrics.remote.server.host", host);
        getProject().setProperty("metrics.remote.server.port", port);
    }
    
    private boolean bool(String prompt) {
        Vector<String> choices = new Vector<String>();
        choices.add("y");
        choices.add("n");
        InputRequest request = new MultipleChoiceInputRequest(prompt, choices);
        getProject().getInputHandler().handleInput(request);
        String value = request.getInput();
        return "y".equals(value);
    }
    
    private String string(String prompt) {
        StringRequest request = new StringRequest(prompt);
        getProject().getInputHandler().handleInput(request);
        return request.getInput();
    }
    
    private String integer(String prompt) {
        InputRequest request = new IntRequest(prompt, null);
        getProject().getInputHandler().handleInput(request);
        return request.getInput();
    }
}
