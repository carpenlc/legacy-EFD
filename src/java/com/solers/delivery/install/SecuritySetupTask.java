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
import java.io.IOException;
import java.security.Security;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.apache.tools.ant.types.LogLevel;

import com.rsa.jsafe.crypto.CryptoJ;
import com.rsa.jsafe.crypto.JSAFE_InvalidUseException;
import com.rsa.jsafe.provider.JsafeJCE;
import com.solers.delivery.install.PathRequest.PathPermission;
import com.solers.delivery.install.PathRequest.PathType;
import com.solers.delivery.security.PasswordType;
import com.solers.security.EncryptionUtils;
import com.solers.security.KeyStoreGenerator;
import com.solers.security.password.PasswordProvider;
import com.solers.util.IOConsole;

public class SecuritySetupTask extends Task {
    private String passwordfile;
    private String keyStore;
    private String trustStore;

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public void setPasswordfile(String passFile) {
        this.passwordfile = passFile;
    }

    public void execute() {
        try {
            CryptoJ.setMode(CryptoJ.FIPS140_MODE);
            Security.insertProviderAt(new JsafeJCE(), 1);
        } catch (JSAFE_InvalidUseException ex) {
        	// TestPros, July 2010, Version 2.1.1 - added some detail here
        	getProject().log("Unable to set security to FIPS-140", ex, Project.MSG_ERR);
            throw new BuildException("Unable to set security to FIPS-140, see install.log for details");
        }
        
        PasswordProviderConsole passProvider = getPasswordConsole();
        PasswordProvider provider = null;
        try {
            provider = passProvider.createPasswordFile(passwordfile, IOConsole.DEFAULT);
        } catch (IOException ie) {
           throw new BuildException("Fail to read password", ie);
        } catch (Exception ex) {
            log("Error occurred: " + ex.getMessage(), ex, LogLevel.ERR.getLevel());
            throw new BuildException("Error occurred: " + ex.getMessage(), ex);
        }
        
        if (provider != null) {
            provider.setPassword(PasswordType.ENCRYPTION.key(), EncryptionUtils.toHex(EncryptionUtils.generateKey().getEncoded()));
            Vector<String> choices = new Vector<String>();
            choices.add("y");
            choices.add("n");
            InputRequest request = new MultipleChoiceInputRequest("Create a new keystore and truststore?", choices);
            getProject().getInputHandler().handleInput(request);
            String value = request.getInput();
            if (value.equals("y")) {
                buildStores(provider);
            }
            
            getProject().setProperty("security.setup", "true");
        }
    }
    
    private void buildStores(PasswordProvider provider) {        
        File keyPath = getPath("Enter path to private key (PEM format)>");
        File chainPath = getPath("Enter path to cert chain (PEM format)>");
        KeyStoreGenerator generator = new KeyStoreGenerator(keyPath, chainPath);
        
        try {
            generator.saveKeyStore(new File(keyStore), provider.getPassword(PasswordType.PKI_KEYSTORE.key()).toCharArray(), provider.getPassword(PasswordType.PKI_KEY.key()).toCharArray());
            getProject().log("Created "+keyStore);
            generator.saveTrustStore(new File(trustStore), provider.getPassword(PasswordType.PKI_TRUSTSTORE.key()).toCharArray());
            getProject().log("Created "+trustStore);
        } catch (Exception ex) {
            getProject().log("An error occurred: "+ex.getMessage(), ex, Project.MSG_ERR);
            throw new BuildException(ex);
        }
    }
    
    private File getPath(String prompt) {
        InputRequest request = new PathRequest(prompt, false, true, true, PathType.FILE, PathPermission.READ);
        getProject().getInputHandler().handleInput(request);
        return new File(request.getInput()).getAbsoluteFile();
    }
    
    protected PasswordProviderConsole getPasswordConsole() {
        return new PasswordProviderConsole();
    }
}
