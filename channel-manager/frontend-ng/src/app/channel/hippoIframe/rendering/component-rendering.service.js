/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class ComponentRenderingService {
  constructor(
    $log,
    $q,
    HippoIframeService,
    PageStructureService,
    SpaService,
  ) {
    'ngInject';

    this.$log = $log;
    this.$q = $q;
    this.HippoIframeService = HippoIframeService;
    this.PageStructureService = PageStructureService;
    this.SpaService = SpaService;
  }

  async renderComponent(componentId, properties) {
    const component = this.PageStructureService.getComponentById(componentId);
    if (!component) {
      this.$log.warn(`Cannot render unknown component with ID '${componentId}'`);

      throw new Error(`Cannot render unknown component with ID '${componentId}'.`);
    }

    // let the SPA render the component; if it returns false, we render the component instead
    if (this.SpaService.renderComponent(component, properties)) {
      return;
    }

    try {
      // eslint-disable-next-line consistent-return
      return await this.PageStructureService.renderComponent(component, properties);
    } catch (error) {
      // component being edited is removed (by someone else), reload the page
      this.HippoIframeService.reload();

      throw error;
    }
  }
}

export default ComponentRenderingService;
