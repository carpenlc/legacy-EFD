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
import org.springframework.security.GrantedAuthority;

public class MockAuthentication implements Authentication {

    private static final long serialVersionUID = 1l;

    private String name;
    private Object credentials;
    private boolean authenticated;
    private GrantedAuthority [] authorities;
    
    public MockAuthentication() {
        
    }

    public MockAuthentication(String name, Object credentials, boolean authenticated) {
        this.name = name;
        this.credentials = credentials;
        this.authenticated = authenticated;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public GrantedAuthority[] getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public void setAuthenticated(boolean arg0) throws IllegalArgumentException {

    }

    public void setAuthorities(GrantedAuthority[] authorities) {
        this.authorities = authorities;
    }
}