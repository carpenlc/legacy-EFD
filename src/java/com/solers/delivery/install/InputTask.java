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
import java.util.Arrays;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.apache.tools.ant.util.StringUtils;

import com.solers.delivery.util.FileSystemUtil;
import com.solers.delivery.util.password.UserPasswordEncoder;

public class InputTask extends Task {
    private String inputtype;

    private String validargs = null;
    private String message = "";
    private String addproperty = null;
    private String defaultvalue = null;
    private boolean newpath = false;
    
    /**
     * Set whether a path request requires a path that does not exist
     * 
     * @param arg If true, the path request must be for a pat that does not exist
     */
    public void setNewPath(boolean arg) {
        this.newpath = arg;
    }

    /**
     * Defines valid input parameters as comma separated strings. If set, input task will reject any input not defined as accepted and requires the user to
     * reenter it. Validargs are case sensitive. If you want 'a' and 'A' to be accepted you need to define both values as accepted arguments.
     * 
     * @param validargs
     *            A comma separated String defining valid input args.
     */
    public void setValidargs(String validargs) {
        this.validargs = validargs;
    }

    /**
     * Defines the name of a property to be created from input. Behaviour is according to property task which means that existing properties cannot be
     * overridden.
     * 
     * @param addproperty
     *            Name for the property to be created from input
     */
    public void setAddproperty(String addproperty) {
        this.addproperty = addproperty;
    }

    /**
     * Sets the Message which gets displayed to the user during the build run.
     * 
     * @param message
     *            The message to be displayed.
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Defines the default value of the property to be created from input. Property value will be set to default if not input is received.
     * 
     * @param defaultvalue
     *            Default value for the property if no input is received
     */
    public void setDefaultvalue(String defaultvalue) {
        this.defaultvalue = defaultvalue;
    }

    public void setInputtype(String inputType) {
        this.inputtype = inputType;
    }

    /**
     * Set a multiline message.
     * 
     * @param msg
     *            The message to be displayed.
     */
    public void addText(String msg) {
        message += getProject().replaceProperties(msg);
    }

    /**
     * Actual method executed by ant.
     * 
     * @throws BuildException
     *             on error
     */
    public void execute() {
        if (addproperty != null && getProject().getProperty(addproperty) != null) {
            log("skipping " + getTaskName() + " as property " + addproperty + " has already been set.");
            return;
        }

        InputRequest request = getInputRequest();

        getProject().getInputHandler().handleInput(request);

        String value = request.getInput();
        
        if (addproperty != null && value != null) {
            getProject().setNewProperty(addproperty, value);
        }
    }

    protected InputRequest getInputRequest() {
        InputRequest request;

        if (validargs != null) {
            Vector<?> accept = StringUtils.split(validargs, ',');
            request = new MultipleChoiceInputRequest(message, accept);
        } else if (inputtype != null && Arrays.asList(RequestType.values()).contains(getRequestType())) {
            request = getCustomInputRequest();
        } else {
            request = new BaseRequest(message, defaultvalue);
        }
        return request;
    }

    private InputRequest getCustomInputRequest() {
        RequestType type = getRequestType();

        switch (type) {
            case PATH:
                return new PathRequest(message, defaultvalue, newpath);
            case INT:
                return new IntRequest(message, defaultvalue);
            case PASSWORD:
                return new PasswordRequest(message, defaultvalue);
            default:
                return null;
        }
    }

    private RequestType getRequestType() {
        return Enum.valueOf(RequestType.class, inputtype.toUpperCase());
    }
}

enum RequestType {
    PATH("path"), INT("int"), PASSWORD("password");
    // IP("ip"), MULTIIP("multiip"), HTTPURL("httpurl"),NONEMPTY("notempty"),
    // DBURL("dburl"),LDAPURL("ldapurl"),FILEPATH("path");

    private String name;

    RequestType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

class BaseRequest extends InputRequest {
    BaseRequest(String message, String defaultValue) {
        super(message);
        setDefaultValue(defaultValue);
    }
}

class PathRequest extends BaseRequest {
    
    public enum PathType { FILE, DIRECTORY, FILE_OR_DIRECTORY }
    public enum PathPermission { READ, ANY }
    
    private final boolean newpath;
    private final boolean existingPath;
    private final boolean canContainSpaces;
    private final PathType pathType;
    private final PathPermission permission;
    
    PathRequest(String message, String defaultValue, boolean newpath) {
        this(message, defaultValue, newpath, false);
    }
    
    PathRequest(String message, String defaultValue, boolean newpath, boolean existingPath) {
        this(message, defaultValue, newpath, existingPath, false, PathType.FILE_OR_DIRECTORY, PathPermission.ANY);
    }
    
    PathRequest(String message, boolean newpath, boolean existingPath, boolean canContainSpaces, PathType pathType, PathPermission permission) {
        this(message, null, newpath, existingPath, canContainSpaces, pathType, permission);
    }
    
    PathRequest(String message, String defaultValue, boolean newpath, boolean existingPath, boolean canContainSpaces, PathType pathType, PathPermission permission) {        
        super(message, FilenameUtils.normalize(defaultValue));              
        this.newpath = newpath;
        this.existingPath = existingPath;
        this.canContainSpaces = canContainSpaces;
        this.pathType = pathType;
        this.permission = permission;
    }

    public boolean isInputValid() {
        String path = getInput();
        
        if (path == null || path.trim().length() == 0) {
            System.err.println("The path specified needs to be a valid absolute path and not empty");
            return false;
        } else {
            File file = new File(path);
            if (newpath && file.exists()) {
                System.err.println("The specified path cannot already exist");
                return false;
            }
            if (existingPath && !file.exists()) {
                System.err.println("The specified path must exist");
                return false;
            }
            if (canContainSpaces == false && path.indexOf(' ') > 0) {
                System.err.println("The specified path cannot contain spaces");
                return false;
            }
           
            switch (pathType) {
                case FILE:
                    if (!file.isFile()) {
                        System.err.println("The specified path must be a file");
                        return false;
                    }
                    break;
                case DIRECTORY:
                    if (!file.isDirectory()) {
                        System.err.println("The specified path must be a directory");
                        return false;
                    }
            }
            
            switch (permission) {
                case READ:
                    if (!FileSystemUtil.isReadable(file)) {
                        System.err.println("The specified path must be readable");
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    public String getInput() {
        String input = super.getInput();
        String normalizedInput = FilenameUtils.normalize(input);
        if (normalizedInput != null) {
            File file = new File(normalizedInput);
            if (file.isAbsolute()) {
                return file.getAbsolutePath();
            }
        }
        return null;
    }
}

class IntRequest extends BaseRequest {
    public IntRequest(String message, String defaultValue) {
        super(message, defaultValue);
    }

    public boolean isInputValid() {
        String value = getInput();

        boolean valid = true;

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            valid = false;
            System.err.println(value + " is not valid. It must be a number");
        }
        
        return valid;
    }
}

class PasswordRequest extends BaseRequest {

    private final UserPasswordEncoder encoder;
    
    /**
     * @param message
     * @param defaultValue
     */
    public PasswordRequest(String message, String defaultValue) {
        super(message, defaultValue);
        encoder = new UserPasswordEncoder();
    }
    
    public boolean isInputValid() {
        return true;
    }
    
    public String getInput() {
        String pw = super.getInput();
        
        return encoder.encodePassword(pw);
    }
}

class StringRequest extends BaseRequest {
    
    private boolean required;
    
    public StringRequest(String message, String defaultValue) {
        super(message, defaultValue);
    }
    
    public StringRequest(String message) {
        this(message, null);
        setRequired(true);
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean isInputValid() {
        String value = getInput();
        if (required) {
            return value != null && value.length() > 0;
        }
        return super.isInputValid();
    }
    
}

