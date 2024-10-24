# capacitor-msal-auth

This Capacitor plugin provides seamless integration with the Microsoft Authentication Library (MSAL), enabling secure multi-account login support for both web and mobile platforms. Easily manage authentication flows with Microsoft Azure AD and support multiple accounts within your app.

## Installation
* `npm i capacitor-msal-auth`
* `npx cap sync`
* Create an app registration: https://learn.microsoft.com/en-us/entra/identity-platform/scenario-spa-app-registration
* In the app registration, go to Authentication, and then Add platform, and then iOS/macOS
* You will be asked for a bundle identifier, which you can find in Xcode (under the General tab of your project)
* Do the same for Android. When asked for the package name, use the name defined in `AndroidManifest.xml`.
* In the Signature section, generate a hash for your key. You will need this key hash later.
* (Android) In the `AndroidManifest.xml` file, append the following code within the `<application>` section:
```xml
<activity
    android:name="com.microsoft.identity.client.BrowserTabActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="msauth"
              android:host="<package name>"
              android:path="/<key hash, with prepending slash>" />
    </intent-filter>
</activity>
```

Note that there are two placeholders, one for you package name and one for the key hash.

* (Android) Add the following snippet to the `build.gradle` file in the `android/` folder
```gradle
allprojects {
    repositories {
        maven {
            url 'https://pkgs.dev.azure.com/MicrosoftDeviceSDK/DuoSDK-Public/_packaging/Duo-SDK-Feed/maven/v1'
        }
    }
}
```

* (Android) Register the plugin in the `MainActivity.java`
```java
import com.getcapacitor.BridgeActivity;
import android.os.Bundle;
import com.hoangqwe.plugins.msal.MsalPlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MsalPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
```

* (iOS) Add a new keychain group to your project's Signing & Capabilities. The keychain group should be `com.microsoft.adalcache`
* (iOS) Configure URL-schemes by adding the following to your `Info.plist` file:
```
<key>CFBundleURLTypes</key>
<array>
    <dict>
        <key>CFBundleURLSchemes</key>
        <array>
            <string>msauth.$(PRODUCT_BUNDLE_IDENTIFIER)</string>
        </array>
    </dict>
</array>
<key>LSApplicationQueriesSchemes</key>
<array>
    <string>msauthv2</string>
    <string>msauthv3</string>
</array>
```
* (iOS) Add `import MSAL` to the top of the AppDelegate file to ensure that the library is linked
* (iOS) if your app's AppDelegate already implements a `application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool` function, you should add the following code inside this method:
```swift
func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey: Any] = [:]) -> Bool {
    if  MSALPublicClientApplication.handleMSALResponse(
        url, sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String
    ) == true {
        return true
    }
    return ApplicationDelegateProxy.shared.application(app, open: url, options: options)
}
```

## Usage
Usage of the plugin is fairly simple, as it has methods: `login`, `logout`, and `getAccounts`.

### Login
```typescript
import {Plugins} from '@capacitor/core';
import { MsalPlugin } from "capacitor-msal-auth";

await MsalPlugin.initializePcaInstance({
    clientId: '<client id>',
    tenant: '<tenant, defaults to common>',
    domainHint: '<domainHint>',
    scopes: ['<scopes, defaults to no scopes>'],
    keyHash: '<Android only, the key hash as obtained above>',
});

const result = await MsalPlugin.login();

const accessToken = result.accessToken;
const idToken = result.account.idToken;
```

### Get accounts and login silently
```typescript
const { accounts } = await MsalPlugin.getAccounts();

// choose account by username
// identifier can be username, oid or homeAccountId
const username = accounts[0].username;
const result = await MsalPlugin.login({ identifier: username });

const accessToken = result.accessToken;
const idToken = result.account.idToken;
```

### Logout
```typescript
await MsAuthPlugin.logout();
```

## API

<docgen-index>

* [`initializePcaInstance(...)`](#initializepcainstance)
* [`login(...)`](#login)
* [`logout()`](#logout)
* [`getAccounts()`](#getaccounts)
* [Interfaces](#interfaces)
* [Type Aliases](#type-aliases)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initializePcaInstance(...)

```typescript
initializePcaInstance(options: BaseOptions) => Promise<void>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#baseoptions">BaseOptions</a></code> |

--------------------


### login(...)

```typescript
login(account?: { identifier?: string | undefined; } | undefined) => Promise<AuthenticationResult>
```

| Param         | Type                                  |
| ------------- | ------------------------------------- |
| **`account`** | <code>{ identifier?: string; }</code> |

**Returns:** <code>Promise&lt;<a href="#authenticationresult">AuthenticationResult</a>&gt;</code>

--------------------


### logout()

```typescript
logout() => Promise<void>
```

--------------------


### getAccounts()

```typescript
getAccounts() => Promise<{ accounts: AccountInfo[]; }>
```

**Returns:** <code>Promise&lt;{ accounts: AccountInfo[]; }&gt;</code>

--------------------


### Interfaces


#### BaseOptions

| Prop                              | Type                        |
| --------------------------------- | --------------------------- |
| **`clientId`**                    | <code>string</code>         |
| **`tenant`**                      | <code>string</code>         |
| **`domainHint`**                  | <code>string</code>         |
| **`authorityType`**               | <code>'AAD' \| 'B2C'</code> |
| **`authorityUrl`**                | <code>string</code>         |
| **`knownAuthorities`**            | <code>string[]</code>       |
| **`keyHash`**                     | <code>string</code>         |
| **`brokerRedirectUriRegistered`** | <code>boolean</code>        |
| **`scopes`**                      | <code>string[]</code>       |
| **`redirectUri`**                 | <code>string</code>         |

### Type Aliases


#### AuthenticationResult

Result returned from the authority's token endpoint.
- uniqueId               - `oid` or `sub` claim from ID token
- tenantId               - `tid` claim from ID token
- scopes                 - Scopes that are validated for the respective token
- account                - An account object representation of the currently signed-in user
- idToken                - Id token received as part of the response
- idTokenClaims          - MSAL-relevant ID token claims
- accessToken            - Access token or SSH certificate received as part of the response
- fromCache              - Boolean denoting whether token came from cache
- expiresOn              - Javascript <a href="#date">Date</a> object representing relative expiration of access token
- extExpiresOn           - Javascript <a href="#date">Date</a> object representing extended relative expiration of access token in case of server outage
- refreshOn              - Javascript <a href="#date">Date</a> object representing relative time until an access token must be refreshed
- state                  - Value passed in by user in request
- familyId               - Family ID identifier, usually only used for refresh tokens
- requestId              - Request ID returned as part of the response

```
{
  accessToken: string;
  account: AccountInfo;
  tenantId: string;
  idToken: string;
  scopes: Array<string>;
  authority: string;
  expiresOn: Date | string;
  uniqueId?: string;
  idTokenClaims?: object;
  fromCache?: boolean;
  extExpiresOn?: Date;
  refreshOn?: Date;
  tokenType?: string;
  correlationId?: string;
  requestId?: string;
  state?: string;
  familyId?: string;
  cloudGraphHostName?: string;
  msGraphHost?: string;
  code?: string;
  fromNativeBroker?: boolean;
}
```


#### AccountInfo

Account object with the following signature:
- homeAccountId          - Home account identifier for this account object
- environment            - Entity which issued the token represented by the domain of the issuer (e.g. login.microsoftonline.com)
- tenantId               - Full tenant or organizational id that this account belongs to
- username               - preferred_username claim of the id_token that represents this account
- localAccountId         - Local, tenant-specific account identifer for this account object, usually used in legacy cases
- name                   - Full name for the account, including given name and family name
- idToken                - raw ID token
- idTokenClaims          - Object contains claims from ID token
- nativeAccountId        - The user's native account ID
- tenantProfiles         - <a href="#map">Map</a> of tenant profile objects for each tenant that the account has authenticated with in the browser

```
{
  homeAccountId: string;
  environment: string;
  tenantId: string;
  username: string;
  localAccountId: string;
  name?: string;
  idToken?: string;
  idTokenClaims?: TokenClaims;
  nativeAccountId?: string;
  authorityType?: string;
  tenantProfiles?: Map<string, TenantProfile>;
}
 ```


#### TokenClaims

Type which describes Id Token claims known by MSAL.
- iss    - Issuer
- iat    - Issued at
- nbf    - Not valid before
- oid    - Immutable object identifier, this ID uniquely identifies the user across applications
- sub    - Immutable subject identifier, this is a pairwise identifier - it is unique to a particular application ID
- tid    - Users' tenant or '9188040d-6c67-4c5b-b112-36a304b66dad' for personal accounts.
- tfp    - Trusted Framework Policy (B2C) The name of the policy that was used to acquire the ID token.
- acr    - Authentication Context Class Reference (B2C) Used only with older policies.

```
{
  aud?: string;
  iss?: string;
  iat?: number;
  nbf?: number;
  oid?: string;
  sub?: string;
  tid?: string;
  tfp?: string;
  acr?: string;
  ver?: string;
  upn?: string;
  preferred_username?: string;
  login_hint?: string;
  emails?: string[];
  name?: string;
  nonce?: string;
  exp?: number;
  home_oid?: string;
  sid?: string;
  cloud_instance_host_name?: string;
  cnf?: { kid: string };
  x5c_ca?: string[];
  ts?: number;
  at?: string;
  u?: string;
  p?: string;
  m?: string;
  roles?: string[];
  amr?: string[];
  idp?: string;
  auth_time?: number;
  tenant_region_scope?: string;
  tenant_region_sub_scope?: string;
}
```


#### TenantProfile

Account details that vary across tenants for the same user

</docgen-api>
