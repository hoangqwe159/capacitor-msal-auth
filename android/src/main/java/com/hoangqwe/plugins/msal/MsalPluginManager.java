package com.hoangqwe.plugins.msal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PluginCall;
import com.microsoft.identity.client.AcquireTokenParameters;
import com.microsoft.identity.client.AcquireTokenSilentParameters;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.ICurrentAccountResult;
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.Prompt;
import com.microsoft.identity.client.exception.MsalClientException;
import com.microsoft.identity.client.exception.MsalException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MsalPluginManager {

    private IMultipleAccountPublicClientApplication instance;
    private final PublicClientApplicationFactory publicClientApplicationFactory;
    private final Context context;
    private final AppCompatActivity activity;
    private String clientId;
    private String domainHint;
    private String tenant;
    private String redirectUri;
    private AuthorityType authorityType;
    private String customAuthorityUrl;
    private String keyHash;
    private Boolean brokerRedirectUriRegistered;
    private List<String> scopes;

    public MsalPluginManager(AppCompatActivity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
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
        Boolean brokerRedirectUriRegistered,
        List<String> scopes
    ) throws MsalException, InterruptedException, IOException, JSONException {
        String tenantId = (tenant != null ? tenant : "common");
        String authorityUrl = customAuthorityUrl != null ? customAuthorityUrl : "https://login.microsoftonline.com/" + tenantId;
        String urlEncodedKeyHash = URLEncoder.encode(keyHash, "UTF-8");
        String redirectUri = "msauth://" + this.context.getPackageName() + "/" + urlEncodedKeyHash;

        this.redirectUri = redirectUri;
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
        this.clientId = clientId;
        this.domainHint = domainHint;
        this.tenant = tenant;
        this.authorityType = authorityType;
        this.customAuthorityUrl = customAuthorityUrl;
        this.keyHash = keyHash;
        this.brokerRedirectUriRegistered = brokerRedirectUriRegistered;
        this.scopes = scopes;

        if (!config.delete()) {
            Logger.warn("Warning! Unable to delete config file.");
        }

        Logger.debug("Pca instance is initialized");
    }

    // Verifies broker redirect URI against the app's signature, to make sure that this is legit.
    private void verifyRedirectUriWithAppSignature() throws MsalClientException {
        final String packageName = this.context.getPackageName();
        try {
            final PackageInfo info = this.context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
            for (final Signature signature : info.signatures) {
                final MessageDigest messageDigest = MessageDigest.getInstance("SHA");
                messageDigest.update(signature.toByteArray());
                final String signatureHash = Base64.encodeToString(messageDigest.digest(), Base64.NO_WRAP);

                final Uri.Builder builder = new Uri.Builder();
                final Uri uri = builder.scheme("msauth")
                        .authority(packageName)
                        .appendPath(signatureHash)
                        .build();

                if (this.redirectUri.equalsIgnoreCase(uri.toString())) {
                    // Life is good.
                    return;
                }
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            Log.e(this.context.getPackageName(), "Unexpected error in verifyRedirectUriWithAppSignature()", e);
        }

        throw new MsalClientException(
                MsalClientException.REDIRECT_URI_VALIDATION_ERROR,
                "The redirect URI in the configuration file doesn't match with the one " +
                        "generated with package name and signature hash. Please verify the uri in the config file and your app registration in Azure portal.");
    }

    public void login(String identifier, PluginCall call) throws MsalException, InterruptedException {
        acquireToken(identifier, result -> {
                JSObject accountInfo = new JSObject();

                accountInfo.put("accessToken", result.getAccessToken());
                accountInfo.put("authorizationHeader", result.getAuthorizationHeader());
                accountInfo.put("authenticationScheme", result.getAuthenticationScheme());
                accountInfo.put("tenantId", result.getTenantId());
                accountInfo.put("expiresOn", result.getExpiresOn().toString());
                accountInfo.put("scopes", new JSONArray(Arrays.asList(result.getScope())));

                IAccount resultAccount = result.getAccount();

                JSObject account = getJSObjectAccount(resultAccount);
                accountInfo.put("account", account);
                accountInfo.put("idToken", resultAccount.getIdToken());
                accountInfo.put("authority", resultAccount.getAuthority());


                call.resolve(accountInfo);

        });
    }

    public void getAccounts(PluginCall call) {
        try {
            this.instance.getAccounts(
                    new IPublicClientApplication.LoadAccountsCallback() {
                        @Override
                        public void onTaskCompleted(List<IAccount> result) {
                            JSArray accountsArray = new JSArray();
                            for (IAccount account : result) {
                                accountsArray.put(getJSObjectAccount(account));
                            }

                            JSObject response = new JSObject();
                            response.put("accounts", accountsArray);

                            call.resolve(response);
                        }

                        @Override
                        public void onError(MsalException exception) {
                            Logger.error("Error occurred during getAccounts", exception);
                            call.reject("Error occurred during getAccounts");
                        }
                    }
            );
        } catch (Exception e) {
            Logger.error("Error occurred during getAccounts", e);
            call.reject("Error occurred during getAccounts");
        }
    }

    public void logout(PluginCall call) {
        this.instance.getAccounts(
                new IPublicClientApplication.LoadAccountsCallback() {
                    @Override
                    public void onTaskCompleted(List<IAccount> result) {
                        for (IAccount account : result) {
                            try {
                                instance.removeAccount(account);
                            } catch (MsalException | InterruptedException e) {
                                call.reject("Error when logging out");
                            }
                        }
                        call.resolve(null);
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Logger.error("Error occurred during logOut", exception);
                        call.reject("Error occurred during logOut");
                    }
                }
            );
    }

    private JSObject getJSObjectAccount(IAccount account) {
        JSObject result = new JSObject();

        result.put("authority", account.getAuthority());
        result.put("homeAccountId", account.getId());
        result.put("idTokenClaims", new JSONObject(account.getClaims()));
        result.put("tenantId", account.getTenantId());
        result.put("username", account.getUsername());
        result.put("idToken", account.getIdToken());

        return result;
    }

    private File writeJSONObjectConfig(JSONObject data) throws IOException {
        File config = new File(this.context.getFilesDir() + "auth_config.json");

        try (FileWriter writer = new FileWriter(config, false)) {
            writer.write(data.toString());
            writer.flush();
        }

        return config;
    }

    private void acquireToken(String identifier, final TokenResultCallback callback) throws MsalException, InterruptedException {
        if (identifier != null) {
            try {
                acquireTokenSilently(identifier, callback);
            } catch (MsalException | InterruptedException e) {
                acquireTokenInteractively(callback);
            }
        } else {
            acquireTokenInteractively(callback);
        }
    }

    private void acquireTokenSilently(String identifier, final TokenResultCallback callback) throws MsalException, InterruptedException {
        AcquireTokenSilentParameters.Builder builder = new AcquireTokenSilentParameters.Builder()
            .withScopes(this.scopes)
            .fromAuthority(this.instance.getConfiguration().getDefaultAuthority().getAuthorityURL().toString());

        builder = builder.forAccount(this.instance.getAccount(identifier));

        AcquireTokenSilentParameters parameters = builder.build();
        IAuthenticationResult silentAuthResult = this.instance.acquireTokenSilent(parameters);

        callback.tokenReceived(silentAuthResult);
    }

    private void acquireTokenInteractively(final TokenResultCallback callback) {
        AcquireTokenParameters.Builder params = new AcquireTokenParameters.Builder()
            .startAuthorizationFromActivity(this.activity)
            .withScopes(scopes)
            .withPrompt(Prompt.SELECT_ACCOUNT)
            .withCallback(
                new AuthenticationCallback() {
                    @Override
                    public void onCancel() {
                        Logger.info("Login cancelled");
                        callback.tokenReceived(null);
                    }

                    public void onSuccess(IAuthenticationResult authenticationResult) {
                        Logger.info(authenticationResult.getAccessToken());
                        callback.tokenReceived(authenticationResult);
                    }

                    @Override
                    public void onError(MsalException ex) {
                        Logger.error("Unable to acquire token interactively", ex);
                        callback.tokenReceived(null);
                    }
                }
            );

        this.instance.acquireToken(params.build());
    }
}
