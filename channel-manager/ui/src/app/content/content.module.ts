/*!
 * Copyright 2020 Bloomreach. All rights reserved. (https://www.bloomreach.com/)
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

import { NgModule } from '@angular/core';

import { SharedModule } from '../shared/shared.module';

import { NG1_CONTENT_SERVICE } from './services/ng1/content.ng1.service';
import { NG1_WORKFLOW_SERVICE } from './services/ng1/workflow.ng1.service';

@NgModule({
  imports: [
    SharedModule,
  ],
  providers: [
    { provide: NG1_CONTENT_SERVICE, useValue: window.angular.element(document.body).injector().get('ContentService') },
    { provide: NG1_WORKFLOW_SERVICE, useValue: window.angular.element(document.body).injector().get('WorkflowService') },
  ],
})
export class ContentModule {
}