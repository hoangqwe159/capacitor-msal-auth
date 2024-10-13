import { MsalPlugin } from 'capacitor-msal-auth';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    MsalPlugin.echo({ value: inputValue })
}
