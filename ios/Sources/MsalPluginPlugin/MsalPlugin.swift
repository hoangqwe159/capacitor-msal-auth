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

    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }

     @objc public func initializePcaInstance(_ call: CAPPluginCall) {
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
    
    @objc private func acquireTokenSilently(identifier: String, call: CAPPluginCall) {
        guard let applicationContext = self.applicationContext else {
            call.reject("PublicClientApplication not initialized")
            return
        }
        
        do {
            // Try to find the account by homeAccountId first
            if let account = try applicationContext.account(forUsername: identifier) {
                acquireTokenSilentlyWithAccount(MSALAccount: account, call: call)
                return
            }

            // If not found, try to find the account by username
            let accounts = try applicationContext.allAccounts()
            if let account = accounts.first(where: { $0.username == identifier }) {
                acquireTokenSilentlyWithAccount(account: account, call: call)
                return
            }
        } catch {
        }
    }
}
