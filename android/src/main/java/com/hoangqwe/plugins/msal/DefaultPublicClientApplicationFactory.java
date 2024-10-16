package com.hoangqwe.plugins.msal;

import android.content.Context;
import androidx.annotation.NonNull;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;
import java.io.File;

public class DefaultPublicClientApplicationFactory implements PublicClientApplicationFactory {

    @Override
    public IMultipleAccountPublicClientApplication createMultipleAccountPublicClientApplication(
        @NonNull Context context,
        @NonNull File configFile
    ) throws InterruptedException, MsalException {
        return PublicClientApplication.createMultipleAccountPublicClientApplication(context, configFile);
    }
}
