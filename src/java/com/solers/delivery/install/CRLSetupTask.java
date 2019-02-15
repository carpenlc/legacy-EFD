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

import java.io.File;
import java.util.Vector;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;

import com.solers.delivery.install.PathRequest.PathPermission;
import com.solers.delivery.install.PathRequest.PathType;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class CRLSetupTask extends Task {
    
    @Override
    public void execute() {
        String path = "";
        boolean crlDisabled = false;
        boolean dynamicDisabled = false;
        
        crlDisabled = ask("Do you want to disable certificate revocation?");
        if (!crlDisabled) {
            dynamicDisabled = ask("Do you want to disable dynamic certificate revocation?");
            if (dynamicDisabled) {
                File dir = getPath("Enter CRL directory>");
                path = dir.getAbsolutePath();
            }
        }
        
        getProject().setProperty("crl.enable", String.valueOf((crlDisabled == false)));
        getProject().setProperty("crl.dynamic", String.valueOf((dynamicDisabled == false)));
        getProject().setProperty("crl.stores", path);
    }
    
    private boolean ask(String prompt) {
        Vector<String> choices = new Vector<String>();
        choices.add("y");
        choices.add("n");
        InputRequest request = new MultipleChoiceInputRequest(prompt, choices);
        getProject().getInputHandler().handleInput(request);
        String value = request.getInput();
        return "y".equals(value);
    }
    
    private File getPath(String prompt) {
        InputRequest request = new PathRequest(prompt, false, true, true, PathType.DIRECTORY, PathPermission.ANY);
        getProject().getInputHandler().handleInput(request);
        return new File(request.getInput()).getAbsoluteFile();
    }
}
