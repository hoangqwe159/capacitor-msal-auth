import { registerPlugin } from '@capacitor/core';

import type { MsalPluginPlugin } from './definitions';
import { MsalPluginWeb } from './web';

const MsalPlugin = registerPlugin<MsalPluginPlugin>('MsalPlugin', {
  web: () => new MsalPluginWeb(),
});

export * from './definitions';
export { MsalPlugin };
