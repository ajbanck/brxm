/*!
 * Copyright 2019 BloomReach. All rights reserved. (https://www.bloomreach.com/)
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

import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';
import { ClientErrorCodes } from '@bloomreach/navapp-communication';
import { filter } from 'rxjs/operators';

import { ClientAppService } from '../client-app/services/client-app.service';
import { InternalError } from '../error-handling/models/internal-error';
import { AppSettings } from '../models/dto/app-settings.dto';

import { APP_SETTINGS } from './app-settings';
import { BusyIndicatorService } from './busy-indicator.service';
import { ConnectionService } from './connection.service';

@Injectable({
  providedIn: 'root',
})
export class AuthService {

  constructor(
    private connectionService: ConnectionService,
    private clientAppService: ClientAppService,
    private busyIndicatorService: BusyIndicatorService,
    @Inject(APP_SETTINGS) private appSettings: AppSettings,
    @Inject(DOCUMENT) private document: Document,
  ) {
    this.connectionService
      .onError$
      .pipe(filter(error => error.errorCode === ClientErrorCodes.NotAuthorizedError))
      .subscribe(error => this.logout(error.errorCode.toString()));
  }

  async loginAllResources(): Promise<void> {
    const loginResources = this.appSettings.loginResources || [];
    const loginPromises = loginResources.map(resource => {
      return this.connectionService
        .createConnection(resource.url)
        .finally(() => {
          this.connectionService.removeConnection(resource.url);
        });
    });

    try {
      await Promise.all(loginPromises);
    } catch (error) {
      const internalError = new InternalError(error.message);
      this.logout(internalError.code.toString());
    }
  }

  async logout(loginMessageKey: string): Promise<void> {
    this.busyIndicatorService.show();

    const logoutResources = this.appSettings.logoutResources || [];
    const logoutResourcePromises = logoutResources.map(resource => this.connectionService.createConnection(resource.url));
    const logoutAppPromises = this.clientAppService.apps.map(app => app.api.logout());

    try {
      await Promise.all(logoutResourcePromises);
      await Promise.all(logoutAppPromises);
    } finally {
      const loginLocation = this.getLoginLocation(loginMessageKey);
      this.document.location.replace(loginLocation);
      this.busyIndicatorService.hide();
    }
  }

  private getLoginLocation(loginMessageKey: string): string {
    const queryParams = loginMessageKey && `/?loginmessage=${loginMessageKey}` || '/';
    const baseUrl = this.appSettings.basePath;
    return `${baseUrl}${queryParams}`;
  }
}
