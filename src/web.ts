import { WebPlugin } from '@capacitor/core';

import type { MsalPluginPlugin } from './definitions';

export class MsalPluginWeb extends WebPlugin implements MsalPluginPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
