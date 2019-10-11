/*
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

import { Injectable } from '@angular/core';
import { ClientErrorCodes } from '@bloomreach/navapp-communication';

import { ConnectionService } from '../../services/connection.service';
import { AppError } from '../models/app-error';
import { CriticalError } from '../models/critical-error';
import { InternalError } from '../models/internal-error';
import { NotFoundError } from '../models/not-found-error';

@Injectable()
export class ErrorHandlingService {
  private appError: AppError;

  constructor(
    private connectionService: ConnectionService,
  ) {
    this.connectionService.onError$.subscribe(error => this.setClientError(error.errorCode, error.message));
  }

  get currentError(): AppError {
    return this.appError;
  }

  private set error(value: AppError) {
    if (value) {
      console.error(value.internalDescription);
    }

    this.appError = value;
  }

  setError(error: AppError): void {
    this.error = error;
  }

  setCriticalError(message: string, internalDescription?: string): void {
    this.error = new CriticalError(message, internalDescription);
  }

  setNotFoundError(publicDescription?: string, internalDescription?: string): void {
    this.error = new NotFoundError(publicDescription, internalDescription);
  }

  setInternalError(publicDescription?: string, internalDescription?: string): void {
    this.error = new InternalError(publicDescription, internalDescription);
  }

  setClientError(errorCode: ClientErrorCodes, message?: string): void {
    const error = new AppError(
      this.mapClientErrorCodeToHttpErrorCode(errorCode),
      this.mapClientErrorCodeToText(errorCode),
      message,
    );

    this.error = error;
  }

  clearError(): void {
    this.appError = undefined;
  }

  private mapClientErrorCodeToHttpErrorCode(code: ClientErrorCodes): number {
    switch (code) {
      case ClientErrorCodes.NotAuthorizedError:
        return 403;

      case ClientErrorCodes.PageNotFoundError:
        return 404;

      default:
        return 500;
    }
  }

  private mapClientErrorCodeToText(code: ClientErrorCodes): string {
    switch (code) {
      case ClientErrorCodes.NotAuthorizedError:
        return 'Not authorized';

      case ClientErrorCodes.PageNotFoundError:
        return 'Page is not found';

      default:
        return 'Something went wrong';
    }
  }
}
