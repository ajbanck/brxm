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

import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

import { Connection } from '../../../models/connection.model';
import { FailedConnection } from '../../../models/failed-connection.model';
import { ConnectionService } from '../../../services/connection.service';
import { ClientAppService } from '../../services/client-app.service';

@Component({
  selector: 'brna-client-app',
  templateUrl: './client-app.component.html',
  styleUrls: ['./client-app.component.scss'],
})
export class ClientAppComponent implements OnInit, AfterViewInit {
  @Input()
  url: string;

  @ViewChild('iframe')
  iframe: ElementRef<HTMLIFrameElement>;

  safeUrl: SafeResourceUrl;

  constructor(
    private domSanitizer: DomSanitizer,
    private connectionService: ConnectionService,
    private clientAppService: ClientAppService,
  ) { }

  ngOnInit(): void {
    this.safeUrl = this.domSanitizer.bypassSecurityTrustResourceUrl(this.url);
  }

  ngAfterViewInit(): void {
    const nativeIFrame = this.iframe.nativeElement;
    const url = nativeIFrame.src;

    this.connectionService
      .connectToIframe(nativeIFrame)
      .then(
        connection => this.clientAppService.addConnection(new Connection(url, connection.api)),
        error => this.clientAppService.addConnection(new FailedConnection(url, error)),
      );
  }
}
