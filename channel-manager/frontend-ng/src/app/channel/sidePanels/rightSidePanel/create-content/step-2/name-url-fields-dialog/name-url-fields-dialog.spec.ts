/*
 * Copyright 2017-2018 Hippo B.V. (http://www.onehippo.com)
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule} from '@angular/forms';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material';

import {NameUrlFieldsDialogComponent} from './name-url-fields-dialog';
import {NameUrlFieldsComponent} from './../../name-url-fields/name-url-fields.component';
import {SharedModule} from '../../../../../../shared/shared.module';
import {HintsComponent} from '../../../../../../shared/components/hints/hints.component';
import {CreateContentService} from '../../create-content.service';
import {CreateContentServiceMock} from '../../create-content.mocks.spec';

xdescribe('NameUrlFields Component', () => {
  let fixture: ComponentFixture<NameUrlFieldsDialogComponent>;
  let component: NameUrlFieldsDialogComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        HintsComponent,
        NameUrlFieldsComponent,
        NameUrlFieldsDialogComponent
      ],
      imports: [
        BrowserAnimationsModule,
        FormsModule,
        SharedModule
      ],
      providers: [
        {provide: CreateContentService, useClass: CreateContentServiceMock},
        {provide: MatDialogRef, useValue: {}},
        {provide: MAT_DIALOG_DATA, useValue: {}}
      ]
    });

    fixture = TestBed.createComponent(NameUrlFieldsDialogComponent);
    component = fixture.componentInstance;

    component.ngOnInit();
    fixture.detectChanges();
  });
});
