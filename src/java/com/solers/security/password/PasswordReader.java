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
package com.solers.security.password;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.log4j.Logger;

/**
 * <p>Helper class to read a password from an {@link InputStream} or {@link System.in}</p>
 * 
 * @author <a href="mailto:dthemistokleous@solers.com">Dean Themistokleous</a>
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class PasswordReader {
    
    private static final Logger log = Logger.getLogger(PasswordReader.class);
    private static final int NEWLINE = 10;
    private static final int CARRIAGE_RETURN = 13;
    private static final int BUFSIZE = 128;

    public char[] readPassword() {
        try {
            log.info("Reading password from stdin...");
            return readPassword(System.in);
        } finally {
            log.info("Read password from stdin");
        }
    }
    
    public char[] readPassword(InputStream input) {
        int size = BUFSIZE;
        int position = 0;
        char [] buf = new char[size];
        try {
            while (true) {
                char c = (char) input.read();
                if (position == size) {
                    size = position + BUFSIZE;
                    buf = Arrays.copyOf(buf, size);
                }
                if (notNewline(c)) {
                    buf[position++] = c;
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            log.error("Error reading from input stream", ex);
        }
        
        try {
            return Arrays.copyOf(buf, position);
        } finally {
            Arrays.fill(buf, ' ');
        }
    }
    
    protected boolean notNewline(char c) {
        return c != NEWLINE && c != CARRIAGE_RETURN;
    }
}
