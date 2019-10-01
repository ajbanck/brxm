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

import React from 'react';
import { mount } from 'enzyme';
import { MetaComment, META_POSITION_BEGIN } from '@bloomreach/spa-sdk';
import { MetaComment as MetaCommentComponent } from './MetaComment';

describe('MetaComment', () => {
  const meta = new class implements MetaComment {
    getData = jest.fn(() => 'comment-data');
    getPosition = jest.fn(() => META_POSITION_BEGIN as typeof META_POSITION_BEGIN);
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render a comment that contains data', () => {
    const wrapper = mount(<div><MetaCommentComponent meta={meta}/></div>);

    expect(wrapper.html()).toBe('<div><!--comment-data--></div>');
  });

  it('should update if meta data has changed', () => {
    const shouldComponentUpdate = jest.spyOn(MetaCommentComponent.prototype, 'shouldComponentUpdate');
    const wrapper = mount(<MetaCommentComponent meta={meta}/>);
    expect(shouldComponentUpdate).not.toHaveBeenCalled();

    meta.getData.mockReturnValueOnce('comment-data');
    meta.getData.mockReturnValueOnce('new-comment-data');

    wrapper.setProps({ meta });

    expect(shouldComponentUpdate).toHaveBeenCalled();
    expect(shouldComponentUpdate).lastReturnedWith(true);
  });

  it('should remove the comment when the component unmounts', () => {
    const wrapper = mount(<div><MetaCommentComponent meta={meta}/></div>);
    wrapper.unmount();

    expect(wrapper.contains(<MetaCommentComponent meta={meta}/>)).toBe(false);
  });
});
