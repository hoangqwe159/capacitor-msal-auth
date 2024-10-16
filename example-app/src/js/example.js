import { MsalPlugin } from 'capacitor-msal-auth';

window.testEcho = async () => {
  const inputValue = document.getElementById('echoInput').value;
  MsalPlugin.echo({ value: inputValue });

  await MsalPlugin.initializePcaInstance({
    clientId: '8a165335-2691-493a-bfbb-6ba5a27b44f0',
    authorityUrl: 'https://login.microsoftonline.com/fd5a5762-9274-4086-aefb-aca071a100b3/',
    // tenant: 'fd5a5762-9274-4086-aefb-aca071a100b3',
    domainHint: 'trinoor.com',
    scopes: ['User.Read'],
    keyHash: 'VzSiQcXRmi2kyjzcA+mYLEtbGVs=',
  });

  // await MsalPlugin.login();

  // const accounts = await MsalPlugin.getAccounts();

  // console.log('accounts', accounts);
};
