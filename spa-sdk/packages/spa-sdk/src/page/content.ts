/*
 * Copyright 2019 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { MetaCollectionModel, Meta } from './meta';

/**
 * Model of a content item.
 */
export interface ContentModel {
  _meta?: MetaCollectionModel;
  id: string;
  localeString?: string;
  name: string;
  [property: string]: any;
}

/**
 * Content used on the page.
 */
export interface Content {
  /**
   * @return The content id.
   */
  getId(): string;

  /**
   * @return The content locale.
   */
  getLocale(): string | undefined;

  /**
   * @return The content meta-data collection.
   */
  getMeta(): Meta[];

  /**
   * @return The content name.
   */
  getName(): string;

  /**
   * @return The content data as it is returned in the Page Model API.
   */
  getData(): ContentModel;
  getData<T extends Record<string, any>>(): T & ContentModel;
}

export class ContentImpl implements Content {
  constructor(protected model: ContentModel, protected meta: Meta[] = []) {}

  getId() {
    return this.model.id;
  }

  getLocale() {
    return this.model.localeString;
  }

  getMeta() {
    return this.meta;
  }

  getName() {
    return this.model.name;
  }

  getData() {
    return this.model;
  }
}
