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

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { async, TestBed } from '@angular/core/testing';
import 'zone.js/dist/zone-patch-rxjs-fake-async';

import { ClientApp } from '../client-app/models/client-app.model';
import { ClientAppService } from '../client-app/services/client-app.service';

import { USER_ACTIVITY_DEBOUNCE_TIME } from './user-activity-debounce-time';
import { UserActivityService } from './user-activity.service';

describe('UserActivityService', () => {
  let service: UserActivityService;

  let clientApps: ClientApp[];

  let clientAppServiceMock = {
    activeApp: undefined,
    apps: [],
  };

  beforeEach(() => {
    clientApps = [
      new ClientApp('http://app1.com', jasmine.createSpyObj('ChildApi1', ['onUserActivity'])),
      new ClientApp('http://app2.com', jasmine.createSpyObj('ChildApi2', ['onUserActivity'])),
      new ClientApp('http://app3.com', jasmine.createSpyObj('ChildApi3', ['onUserActivity'])),
    ];
    clientAppServiceMock.apps = clientApps;

    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
      ],
      providers: [
        UserActivityService,
        { provide: ClientAppService, useValue: clientAppServiceMock },
        { provide: USER_ACTIVITY_DEBOUNCE_TIME, useValue: 100 },
      ],
    });

    service = TestBed.get(UserActivityService);
    clientAppServiceMock = TestBed.get(ClientAppService);
  });

  it('should broadcast user activity to app2 and app3 if app1 is active', () => {
    clientAppServiceMock.activeApp = clientAppServiceMock.apps[0];

    service.broadcastUserActivity();

    expect(clientApps[0].api.onUserActivity).not.toHaveBeenCalled();
    expect(clientApps[1].api.onUserActivity).toHaveBeenCalled();
    expect(clientApps[2].api.onUserActivity).toHaveBeenCalled();
  });

  it('should broadcast user activity to app1 and app3 if app2 is active', () => {
    clientAppServiceMock.activeApp = clientAppServiceMock.apps[1];

    service.broadcastUserActivity();

    expect(clientApps[1].api.onUserActivity).not.toHaveBeenCalled();
    expect(clientApps[0].api.onUserActivity).toHaveBeenCalled();
    expect(clientApps[2].api.onUserActivity).toHaveBeenCalled();
  });

  it('should broadcast user activity to app1 and app2 if app3 is active', () => {
    clientAppServiceMock.activeApp = clientAppServiceMock.apps[2];

    service.broadcastUserActivity();

    expect(clientApps[2].api.onUserActivity).not.toHaveBeenCalled();
    expect(clientApps[0].api.onUserActivity).toHaveBeenCalled();
    expect(clientApps[1].api.onUserActivity).toHaveBeenCalled();
  });

  describe('after the first broadcast has triggered', () => {
    beforeEach(() => {
      clientAppServiceMock.activeApp = clientAppServiceMock.apps[1];

      service.broadcastUserActivity();

      (clientApps[0].api.onUserActivity as jasmine.Spy).calls.reset();
      (clientApps[1].api.onUserActivity as jasmine.Spy).calls.reset();
      (clientApps[2].api.onUserActivity as jasmine.Spy).calls.reset();
    });

    it('should not broadcast before time delay has passed', () => {
      service.broadcastUserActivity();

      expect(clientApps[0].api.onUserActivity).not.toHaveBeenCalled();
      expect(clientApps[1].api.onUserActivity).not.toHaveBeenCalled();
      expect(clientApps[2].api.onUserActivity).not.toHaveBeenCalled();
    });

    it('should broadcast next time after the timeout has passed', async(() => {
      setTimeout(() => {
        service.broadcastUserActivity();

        expect(clientApps[0].api.onUserActivity).toHaveBeenCalled();
        expect(clientApps[1].api.onUserActivity).not.toHaveBeenCalled();
        expect(clientApps[2].api.onUserActivity).toHaveBeenCalled();
      }, 200);
    }));
  });
});
