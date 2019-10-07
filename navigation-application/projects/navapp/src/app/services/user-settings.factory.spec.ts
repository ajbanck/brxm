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

import { UserSettings } from '../models/dto/user-settings.dto';

import { userSettingsFactory } from './user-settings.factory';

describe('userSettingsFactory', () => {
  const windowRefMock: any = {
    nativeWindow: {},
  };

  beforeEach(() => {
    spyOn(console, 'error');
  });

  it('should print an error when the global configuration object is not set', () => {
    userSettingsFactory(windowRefMock);

    expect(console.error).toHaveBeenCalledWith('[NAVAPP] The global configuration object is not set');
  });

  it('should return an empty object when the global configuration object is not set', () => {
    const expected: UserSettings = {} as any;

    const actual = userSettingsFactory(windowRefMock);

    expect(actual).toEqual(expected);
  });

  describe('when the global configuration object is set', () => {
    beforeEach(() => {
      windowRefMock.nativeWindow = {
        NavAppSettings: {},
      };
    });

    it('should print an error when the app settings are not set in the global object', () => {
      userSettingsFactory(windowRefMock);

      expect(console.error).toHaveBeenCalledWith('[NAVAPP] User settings part of the global configuration object is not set');
    });

    it('should return an empty object the app settings are not set in the global object', () => {
      const expected: UserSettings = {} as any;

      const actual = userSettingsFactory(windowRefMock);

      expect(actual).toEqual(expected);
    });

    describe('and the user settings object is set', () => {
      const userSettingsMock: UserSettings = {
        userName: 'Some name',
        email: 'some.email@domain.com',
        language: 'en-US',
        timeZone: 'Timezone',
      } as any;

      beforeEach(() => {
        windowRefMock.nativeWindow = {
          NavAppSettings: {
            userSettings: userSettingsMock,
          },
        };
      });

      it('should return the app settings object', () => {
        const expected = userSettingsMock;

        const actual = userSettingsFactory(windowRefMock);

        expect(actual).toEqual(expected);
      });
    });
  });
});
