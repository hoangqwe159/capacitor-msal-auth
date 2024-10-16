import type { AccountInfo, AuthenticationResult, IPublicClientApplication } from '@azure/msal-browser';
import { PublicClientApplication } from '@azure/msal-browser';
import { WebPlugin } from '@capacitor/core';

import type { BaseOptions, MsalPluginPlugin } from './definitions';

let instance: IPublicClientApplication | undefined;
export class MsalPluginWeb extends WebPlugin implements MsalPluginPlugin {
  private baseConfig: BaseOptions | undefined;

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }

  public async initializePcaInstance(options: BaseOptions): Promise<void> {
    if (instance) return;

    instance = new PublicClientApplication({
      auth: {
        clientId: options.clientId,
        authority: options.authorityUrl || `https://login.microsoftonline.com/${options.tenant || 'common'}/`,
        ...(options.knownAuthorities ? { knownAuthorities: options.knownAuthorities } : {}),
        redirectUri: window.location.origin, // Points to window.location.origin. You must register this URI on Microsoft Entra admin center/App Registration.
      },
    });

    this.baseConfig = options;

    await instance.initialize();
  }

  public async login(account?: AccountInfo): Promise<AuthenticationResult> {
    if (!instance) {
      throw new Error('PublicClientApplication not initialized');
    }

    if (!this.baseConfig) {
      throw new Error('BaseOptions not initialized');
    }

    try {
      if (account) {
        return await this.acquireTokenSilently(account).catch(async () => {
          return await this.acquireTokenInteractively();
        });
      }

      return await this.acquireTokenInteractively();
    } catch (error) {
      console.error('Error logging in', error);
      throw error;
    }
  }

  // "8d241d8c-3dd3-4ed8-b8bc-b9ea8090cca1"

  public async logout(): Promise<void> {
    if (!instance) {
      throw new Error('PublicClientApplication not initialized');
    }

    return await instance.logoutPopup();
  }

  public async getAccounts(): Promise<AccountInfo[]> {
    if (!instance) {
      throw new Error('PublicClientApplication not initialized');
    }

    return instance.getAllAccounts();
  }

  private async acquireTokenInteractively(): Promise<AuthenticationResult> {
    if (!instance) {
      throw new Error('PublicClientApplication not initialized');
    }

    if (!this.baseConfig) {
      throw new Error('BaseOptions not initialized');
    }

    return await instance.loginPopup({
      scopes: this.baseConfig?.scopes ?? [],
      ...(this.baseConfig?.domainHint ? { extraQueryParameters: { domain_hint: this.baseConfig.domainHint } } : {}),
      prompt: 'select_account',
    });
  }

  private async acquireTokenSilently(account: AccountInfo): Promise<AuthenticationResult> {
    if (!instance) {
      throw new Error('PublicClientApplication not initialized');
    }

    if (!this.baseConfig) {
      throw new Error('BaseOptions not initialized');
    }

    return await instance.acquireTokenSilent({
      scopes: this.baseConfig?.scopes ?? [],
      ...(this.baseConfig?.domainHint ? { extraQueryParameters: { domain_hint: this.baseConfig.domainHint } } : {}),
      account,
    });
  }
}
