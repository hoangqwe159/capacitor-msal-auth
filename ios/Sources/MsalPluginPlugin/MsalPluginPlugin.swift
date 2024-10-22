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
        CAPPluginMethod(name: "echo", returnType: CAPPluginReturnPromise)
    ]
    private let implementation = MsalPlugin()

    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation.echo(value)
        ])
    }
    
    @objc func initializePcaInstance(_ call: CAPPluginCall) {
        implementation.initializePcaInstance(call)
    }
}
