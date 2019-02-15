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
package com.solers.delivery.acegi;

import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.providers.AuthenticationProvider;

/**
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class MockAuthenticationProvider implements AuthenticationProvider {

    public static final String USER = "user";
    public static final String PASS = "password1";

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        String name = auth.getName();
        Object credentials = auth.getCredentials();
        boolean authenticated = USER.equals(name) && PASS.equals(credentials);

        if (name.equals("throw")) {
            throw new BadCredentialsException("");
        }

        return new MockAuthentication(name, credentials, authenticated);
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class arg0) {
        return true;
    }
}
