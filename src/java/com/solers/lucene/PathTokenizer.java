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
package com.solers.lucene;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/**
 * @author <a href="mailto:kevin.conaway@solers.com">Kevin Conaway</a>
 */
public class PathTokenizer extends Tokenizer {
    
    private boolean done;
    
    public PathTokenizer(Reader reader) {
        super(reader);
    }
    
    @Override
    public Token next(Token result) throws IOException {
        if (done) {
            return null;
        }
        
        char[] next = token();
        int len = next.length;
        if (len == 0) {
            done = true;
            return null;
        }
        
        char [] buffer = initBuffer(len, result);
        for (int i=0; i < len; i++) {
            buffer[i] = next[i];
        }
        
        return result;
    }
    
    @Override
    public void reset(Reader input) throws IOException {
        super.reset(input);
        this.done = false;
    }

    private char [] initBuffer(int len, Token token) {
        token.clear();
        
        char [] buffer = token.termBuffer();
        if (token.termLength() < len) {
            buffer = token.resizeTermBuffer(len);
        }
        token.setTermLength(len);
        return buffer;
    }
    
    private char[] token() throws IOException {
        CharArrayWriter buffer = new CharArrayWriter();
        while (true) {
            int c = input.read();
            if (c == -1 || c == '/' || c == ' ') {
                break;
            }
            
            if (Character.isUpperCase(c)) {
                c = Character.toLowerCase(c);
            }
            
            buffer.append((char)c);
        }
        return buffer.toCharArray();
    }

}
