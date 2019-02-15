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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.util.KeepAliveInputStream;

import com.solers.util.IOConsole;

public class InputHandler extends DefaultInputHandler {
    
    public InputHandler() {
    }
    
    /**
     * Prompts and requests input.  May loop until a valid input has
     * been entered.
     * @param request the request to handle
     * @throws org.apache.tools.ant.BuildException if not possible to read from console
     */
    public void handleInput(InputRequest request) {
        
        if (request instanceof PasswordRequest) {
            handlePasswordInput(request);
            return;
        }
        
        String prompt = getPrompt(request);
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new KeepAliveInputStream(getInputStream())));
            do {
                System.err.println(prompt);
                System.err.flush();
                try {
                    String input = in.readLine();
                    request.setInput(input);
                    if ((input == null || input.trim().length() == 0) && request.getDefaultValue() != null) {
                       request.setInput(request.getDefaultValue());
                    }
                } catch (IOException e) {
                    throw new BuildException("Failed to read input from" + " Console.", e);
                }
            } while (!request.isInputValid());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new BuildException("Failed to close input.", e);
                }
            }
        }
    }
    
    public void handlePasswordInput(InputRequest request) {
        String prompt = getPrompt(request);
        char[] password = PasswordProviderConsole.readPassword(IOConsole.DEFAULT, prompt, true);
        request.setInput(new String(password));
    }
}
