import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(MsalPluginPlugin)
public class MsalPluginPlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "MsalPluginPlugin"
    public let jsName = "MsalPlugin"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "initializePcaInstance", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "login", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "logout", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getAccounts", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = MsalPlugin()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }

    @objc func initializePcaInstance(_ call: CAPPluginCall) {
        guard let bridgeViewController = bridge?.viewController else {
            call.reject("Unable to get Capacitor bridge.viewController")
            return
        }

        implementation.initializePcaInstance(call, bridgeViewController: bridgeViewController)
    }

    @objc func login(_ call: CAPPluginCall) {
        implementation.login(call)
    }

    @objc func logout(_ call: CAPPluginCall) {
        implementation.logout(call)
    }

    @objc func getAccounts(_ call: CAPPluginCall) {
        implementation.getAccounts(call: call)
    }
}
