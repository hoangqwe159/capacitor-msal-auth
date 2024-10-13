package com.hoangqwe.plugins.msal;

import android.util.Log;

public class MsalPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
