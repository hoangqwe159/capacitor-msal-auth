package com.hoangqwe.plugins.msal;

import com.microsoft.identity.client.IAuthenticationResult;

public interface TokenResultCallback {
    void tokenReceived(IAuthenticationResult tokenResult);
}
