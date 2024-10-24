import type { AccountInfo } from '@azure/msal-browser';

interface AuthenticationResult {
  accessToken: string;
  account: AccountInfo;
  tenantId: string;
  idToken: string;
  scopes: string[];
  authority: string;
  expiresOn: Date | string | null;
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
