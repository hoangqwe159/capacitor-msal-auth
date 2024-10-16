package com.hoangqwe.plugins.msal;

import android.content.Context;
import android.util.Log;
import com.getcapacitor.Logger;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.exception.MsalException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MsalPluginManager {

    private IMultipleAccountPublicClientApplication instance;
    private final PublicClientApplicationFactory publicClientApplicationFactory;
    private final Context context;

    public MsalPluginManager(Context context) {
        this.context = context;
        this.publicClientApplicationFactory = new DefaultPublicClientApplicationFactory();
    }

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public void initializePcaInstance(
        String clientId,
        String domainHint,
        String tenant,
        AuthorityType authorityType,
        String customAuthorityUrl,
        String keyHash,
        Boolean brokerRedirectUriRegistered
    ) throws MsalException, InterruptedException, IOException, JSONException {
        String tenantId = (tenant != null ? tenant : "common");
        String authorityUrl = customAuthorityUrl != null ? customAuthorityUrl : "https://login.microsoftonline.com/" + tenantId;
        String urlEncodedKeyHash = URLEncoder.encode(keyHash, StandardCharsets.UTF_8);
        String redirectUri = "msauth://" + this.context.getPackageName() + "/" + urlEncodedKeyHash;

        JSONObject configFile = new JSONObject();
        JSONObject authorityConfig = new JSONObject();

        switch (authorityType) {
            case AAD:
                authorityConfig.put("type", AuthorityType.AAD.name());
                authorityConfig.put("authority_url", authorityUrl);
                authorityConfig.put("audience", (new JSONObject()).put("type", "AzureADMultipleOrgs").put("tenant_id", tenantId));
                configFile.put("broker_redirect_uri_registered", brokerRedirectUriRegistered);
                break;
            case B2C:
                authorityConfig.put("type", AuthorityType.B2C.name());
                authorityConfig.put("authority_url", authorityUrl);
                authorityConfig.put("default", "true");
                break;
        }

        configFile.put("client_id", clientId);
        configFile.put("domain_hint", domainHint);
        configFile.put("authorization_user_agent", "DEFAULT");
        configFile.put("redirect_uri", redirectUri);
        configFile.put("account_mode", "MULTIPLE");
        configFile.put("authorities", (new JSONArray()).put(authorityConfig));

        File config = writeJSONObjectConfig(configFile);
        this.instance = publicClientApplicationFactory.createMultipleAccountPublicClientApplication(this.context, config);

        if (!config.delete()) {
            Logger.warn("Warning! Unable to delete config file.");
        }

        Logger.debug("Pca instance is initialized");
    }

    private File writeJSONObjectConfig(JSONObject data) throws IOException {
        File config = new File(this.context.getFilesDir() + "auth_config.json");

        try (FileWriter writer = new FileWriter(config, false)) {
            writer.write(data.toString());
            writer.flush();
        }

        return config;
    }
}
