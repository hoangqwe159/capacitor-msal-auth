package com.hoangqwe.plugins.msal;

import android.content.Context;
import androidx.annotation.NonNull;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;
import java.io.File;

public interface PublicClientApplicationFactory {
    IMultipleAccountPublicClientApplication createMultipleAccountPublicClientApplication(
        @NonNull final Context context,
        @NonNull final File configFile
    ) throws InterruptedException, MsalException;
}
