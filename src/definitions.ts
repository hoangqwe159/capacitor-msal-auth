export interface MsalPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
