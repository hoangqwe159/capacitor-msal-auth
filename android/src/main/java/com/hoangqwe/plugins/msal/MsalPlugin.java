package com.hoangqwe.plugins.msal;

import android.Manifest;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.microsoft.identity.client.exception.MsalException;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;

@CapacitorPlugin(
    name = "MsalPlugin",
    permissions = { @Permission(alias = "network", strings = { Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET }) }
)
public class MsalPlugin extends Plugin {

    private MsalPluginManager implementation;

    @Override
    public void load() {
        implementation = new MsalPluginManager(this.getActivity());
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
        List<String> scopes = call.getArray("scopes").toList();

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

        try {
            implementation.initializePcaInstance(
                clientId,
                domainHint,
                tenant,
                authorityType,
                authorityUrl,
                keyHash,
                brokerRedirectUriRegistered,
                scopes
            );
        } catch (Exception e) {
            call.resolve();
        }


        call.resolve();
    }

    @PluginMethod
    public void login(final PluginCall call) throws MsalException, InterruptedException {
        try {
            String identifier = call.getString("identifier");

            implementation.login(identifier, call);
        } catch (InterruptedException | MsalException exception) {
            call.reject("Error when logging in");
        }
    }

    @PluginMethod
    public void logout(final PluginCall call) {
        implementation.logout(call);
    }

    @PluginMethod
    public void getAccounts(final PluginCall call) {
        implementation.getAccounts(call);
    }
}
