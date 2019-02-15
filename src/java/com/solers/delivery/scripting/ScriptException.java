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

public class ScriptException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * Empty exception.
     */
    public ScriptException() {}

    /**
     * Specify a message.
     * 
     * @param msg
     *            The message
     */
    public ScriptException(String msg) {
        super(msg);
    }

    /**
     * Specify an exception.
     * 
     * @param t
     *            The exception
     */
    public ScriptException(Throwable t) {
        super(t);
    }

    /**
     * Specify a message and an exception.
     * 
     * @param msg
     *            The message
     * @param t
     *            The exception
     */
    public ScriptException(String msg, Throwable t) {
        super(msg, t);
    }
}
