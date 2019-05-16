import penpal from 'penpal';

import { ChildConnectConfig, ChildPromisedApi } from './api';

export function connectToChild({
  iframe,
  methods,
}: ChildConnectConfig): Promise<ChildPromisedApi> {
  const proxyConfig: ChildConnectConfig = {
    iframe,
    methods,
  };

  return penpal.connectToChild(proxyConfig).promise;
}
