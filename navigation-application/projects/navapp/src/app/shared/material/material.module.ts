/* Copyright 2017-2019 Hippo B.V. (http://www.onehippo.com) */

import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import {
  MatIconModule,
  MatIconRegistry,
  MatRippleModule,
} from '@angular/material';
import { DomSanitizer } from '@angular/platform-browser';

@NgModule({
  exports: [
    MatRippleModule,
    MatIconModule,
    HttpClientModule,
  ],
})
export class MaterialModule {
  constructor(
    iconRegistry: MatIconRegistry,
    donSanitizer: DomSanitizer,
  ) {
    const menuItemIconsList = [
      'br-logo',
      'br-logo.active',
      'audiences',
      'audiences.active',
      'categories',
      'categories.active',
      'default',
      'default.active',
      'documents',
      'documents.active',
      'document-search',
      'document-search.active',
      'experience-manager',
      'experience-manager.active',
      'fast-travel',
      'fast-travel.active',
      'insights',
      'insights.active',
      'projects',
      'projects.active',
      'seo',
      'seo.active',
      'settings',
      'settings.active',
      'site-search',
      'site-search.active',
      'widget',
      'widget.active',
      'extensions',
      'extensions.active',
      'help',
      'user',
    ];

    const serviceIconsList = [
      'nav-collapse',
      'nav-expand',
      'baseline-expand_less',
      'baseline-expand_more',
    ];

    menuItemIconsList.concat(serviceIconsList).forEach(icon => iconRegistry.addSvgIcon(
      icon,
      donSanitizer.bypassSecurityTrustResourceUrl(`navapp/assets/menu-icons/${icon}.svg`)),
    );
  }
}
