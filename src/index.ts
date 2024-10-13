import { registerPlugin } from '@capacitor/core';

import type { MsalPluginPlugin } from './definitions';

const MsalPlugin = registerPlugin<MsalPluginPlugin>('MsalPlugin', {
  web: () => import('./web').then((m) => new m.MsalPluginWeb()),
});

export * from './definitions';
export { MsalPlugin };
