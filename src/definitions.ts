import type { AccountInfo, AuthenticationResult } from '@azure/msal-browser';

export interface BaseOptions {
  clientId: string;
  tenant?: string;
  domainHint?: string;
  authorityType?: 'AAD' | 'B2C';
  authorityUrl?: string;
  knownAuthorities?: string[];
  keyHash?: string;
  brokerRedirectUriRegistered?: boolean;
  scopes?: string[];
  redirectUri?: string;
}
export interface LoginOptions extends BaseOptions {
  scopes?: string[];
}
export declare type LogoutOptions = BaseOptions;
export interface MsalPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  initializePcaInstance(options: BaseOptions): Promise<void>;
  login(account?: { identifier?: string }): Promise<AuthenticationResult>;
  logout(): Promise<void>;
  getAccounts(): Promise<{
    accounts: AccountInfo[];
  }>;
}
