import Foundation
import Capacitor
import MSAL

@objc public class MsalPlugin: NSObject {
    private var applicationContext: MSALPublicClientApplication?
    private var clientId: String?
    private var authorityType: String?
    private var authorityUrl: URL?
    private var scopes: [String] = []
    private var redirectUri: String?
    private var bridgeViewController: UIViewController?

    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }

    @objc public func initializePcaInstance(_ call: CAPPluginCall, bridgeViewController: UIViewController) {
        guard let _clientID = call.getString("clientId") else {
            call.reject("Invalid client ID specified.")
            return
        }

        let _tenant = call.getString("tenant")
        let _authorityType = call.getString("authorityType") ?? "AAD"

        if _authorityType != "AAD" && _authorityType != "B2C" {
            call.reject("authorityType must be one of 'AAD' or 'B2C'")
            return
        }

        guard let _authorityURL = URL(string: call.getString("authorityUrl") ?? "https://login.microsoftonline.com/\(_tenant ?? "common")") else {
            call.reject("Invalid authorityUrl or tenant specified")
            return
        }

        let _scopes = call.getArray("scopes", String.self) ?? []

        self.clientId = _clientID
        self.authorityUrl = _authorityURL
        self.scopes = _scopes
        self.authorityType = _authorityType
        self.bridgeViewController = bridgeViewController

        do {
            let authority = self.authorityType == "AAD"
                ? try MSALAADAuthority(url: _authorityURL) : try MSALB2CAuthority(url: _authorityURL)

            let msalConfiguration = MSALPublicClientApplicationConfig(clientId: _clientID, redirectUri: nil, authority: authority)
            msalConfiguration.knownAuthorities = [authority]
            self.applicationContext = try MSALPublicClientApplication(configuration: msalConfiguration)

            call.resolve()
            return
        } catch {
            print(error)

            call.reject("Failed to initialize MSAL Public Client Application instance.")
            return
        }
    }

    @objc public func login(_ call: CAPPluginCall) {
        if let identifier = call.getString("identifier") {
            acquireTokenSilently(identifier: identifier, call: call)
        } else {
            acquireTokenInteractively(call: call)
        }
    }

    @objc public func logout(_ call: CAPPluginCall) {
        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }

        do {
            let accounts = try applicationContext.allAccounts()
            for account in accounts {
                try applicationContext.remove(account)
            }
            call.resolve()
        } catch {
            call.reject("Failed to logout: \(error.localizedDescription)")
        }
    }

    func getAccounts(call: CAPPluginCall) {
        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }

        do {
            let accounts = try applicationContext.allAccounts()
            let accountsArray = accounts.map { accountToJSObject($0) }

            var response = JSObject()
            response["accounts"] = JSArray(accountsArray)
            call.resolve(response)
        } catch {
            call.reject("Failed to get accounts: \(error.localizedDescription)")
        }
    }

    func accountToJSObject(_ msalAccount: MSALAccount) -> JSObject {
        var account = JSObject()

        account["username"] = msalAccount.username
        account["environment"] = msalAccount.environment
        account["idTokenClaims"] = dictionaryToJSObject(msalAccount.accountClaims ?? [:])
        account["identifier"] = msalAccount.identifier
        account["homeAccountId"] = msalAccount.homeAccountId?.identifier
        account["isSSOAccount"] = msalAccount.isSSOAccount
        return account
    }

    @objc private func acquireTokenSilently(identifier: String, call: CAPPluginCall) {
        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }

        do {
            let account = try applicationContext.account(forUsername: identifier)
            acquireTokenSilentlyWithAccount(account: account, call: call)
        } catch {
            do {
                let account = try applicationContext.account(forIdentifier: identifier)
                acquireTokenSilentlyWithAccount(account: account, call: call)
            } catch {
                acquireTokenInteractively(call: call)
            }
        }
    }

    @objc private func acquireTokenSilentlyWithAccount(account: MSALAccount, call: CAPPluginCall) {
        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }

        let parameters = MSALSilentTokenParameters(scopes: self.scopes, account: account)
        applicationContext.acquireTokenSilent(with: parameters) { (result, error) in
            if error != nil {
                self.acquireTokenInteractively(call: call)
                return
            }
            guard let result = result else {
                call.reject("Failed to acquire token silently")
                return
            }
            self.handleAuthenticationResult(result: result, call: call)
        }

    }

    @objc private func acquireTokenInteractively(call: CAPPluginCall) {
        guard let bridgeViewController = self.bridgeViewController else {
            call.reject("bridgeViewController not initialized")
            return
        }

        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }

        let webviewParameters = MSALWebviewParameters(authPresentationViewController: bridgeViewController)
        let parameters = MSALInteractiveTokenParameters(scopes: self.scopes, webviewParameters: webviewParameters)

        parameters.promptType = .selectAccount

        applicationContext.acquireToken(with: parameters) { (result, error) in
            if error != nil {
                call.reject("Failed to acquire token interactively")
                return
            }
            guard let result = result else {
                call.reject("Failed to acquire token interactively")
                return
            }
            self.handleAuthenticationResult(result: result, call: call)
        }
    }

    @objc private func handleAuthenticationResult(result: MSALResult, call: CAPPluginCall) {
        var ret = JSObject()
        var account = JSObject()

        account["username"] = result.account.username
        account["environment"] = result.account.environment
        account["idTokenClaims"] = dictionaryToJSObject(result.account.accountClaims ?? [:])
        account["identifier"] = result.account.identifier
        account["idToken"] = result.idToken
        account["homeAccountId"] = result.account.homeAccountId?.identifier
        account["isSSOAccount"] = result.account.isSSOAccount

        ret["accessToken"] = result.accessToken
        ret["authorizationHeader"] = result.authorizationHeader
        ret["authenticationScheme"] = result.authenticationScheme
        ret["expiresOn"] = result.expiresOn?.description
        ret["scopes"] = result.scopes
        ret["idToken"] = result.idToken
        ret["authority"] = result.authority.description
        ret["uniqueId"] = result.tenantProfile.identifier
        
        ret["account"] = account

        call.resolve(ret)
    }

    func dictionaryToJSObject(_ dictionary: [String: Any]) -> JSObject {
        var jsObject = JSObject()
        for (key, value) in dictionary {
            jsObject[key] = value as? JSValue
        }
        return jsObject
    }
}
