package com.hoangqwe.plugins.msal;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.microsoft.identity.client.exception.MsalException;
import java.io.IOException;
import org.json.JSONException;
import android.Manifest;

@CapacitorPlugin(
    name = "MsalPlugin",
    permissions = { @Permission(alias = "network", strings = { Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET }) }
)
public class MsalPlugin extends Plugin {
    private MsalPluginManager implementation;

    @Override
    public void load() {
        implementation = new MsalPluginManager(this.getActivity().getApplicationContext());
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void initializePcaInstance(final PluginCall call) throws MsalException, InterruptedException, IOException, JSONException {
        String clientId = call.getString("clientId");
        String domainHint = call.getString("domainHint");
        String tenant = call.getString("tenant");
        String keyHash = call.getString("keyHash");
        String authorityTypeString = call.getString("authorityType", AuthorityType.AAD.name());
        String authorityUrl = call.getString("authorityUrl");
        Boolean brokerRedirectUriRegistered = call.getBoolean("brokerRedirectUriRegistered", false);

        if (keyHash == null || keyHash.isEmpty()) {
            call.reject("Invalid key hash specified.");
            return;
        }

        AuthorityType authorityType;
        if (AuthorityType.AAD.name().equals(authorityTypeString)) {
            authorityType = AuthorityType.AAD;
        } else if (AuthorityType.B2C.name().equals(authorityTypeString)) {
            authorityType = AuthorityType.B2C;
        } else {
            call.reject("Invalid authorityType specified. Only AAD and B2C are supported.");
            return;
        }

        implementation.initializePcaInstance(
            clientId,
            domainHint,
            tenant,
            authorityType,
            authorityUrl,
            keyHash,
            brokerRedirectUriRegistered
        );

        call.resolve();
    }
}
