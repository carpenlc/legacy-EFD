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
package com.solers.delivery.rest.auth;

import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/**
 * Acegi implementations of RestletAuthentication.
 * 
 * Uses the Acegi SecurityContextHolder to retrieve the current users authentication information
 * 
 * @author <a href="mailto:kconaway@solers.com">Kevin Conaway</a>
 */
public class AcegiRestAuthentication implements RestAuthentication {

    @Override
    public ChallengeResponse getDetails() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        if (ctx != null) {
            Authentication auth = ctx.getAuthentication();
            if (auth != null && auth.getCredentials() != null) {
                char[] credentials = auth.getCredentials().toString().toCharArray();
                return new ChallengeResponse(ChallengeScheme.HTTP_BASIC, auth.getName(), credentials);
            }
        }
        return null;
    }

}
