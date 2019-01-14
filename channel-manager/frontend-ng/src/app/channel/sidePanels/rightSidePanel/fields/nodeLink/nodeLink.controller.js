/*
 * Copyright 2018 Hippo B.V. (http://www.onehippo.com)
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

class nodeLinkController {
  constructor($element, $scope, $timeout, PickerService) {
    'ngInject';

    this.$element = $element;
    this.$scope = $scope;
    this.$timeout = $timeout;
    this.PickerService = PickerService;
  }

  $onInit() {
    if (this.index === 0) {
      this.$scope.$on('primitive-field:focus', ($event, focusEvent) => this.onFocusFromParent(focusEvent));
    }

    // material doesn't do that in our case because the input field is read-only
    // @see https://github.com/angular/material/blob/master/src/components/input/input.js#L397
    this.$scope.$watch(
      () => this.ngModel.$invalid && this.ngModel.$touched,
      isInvalid => this.mdInputContainer && this.mdInputContainer.setInvalid(isInvalid),
    );
  }

  onFocusFromParent(focusEvent) {
    // Don't let the click event bubble through the label as it can trigger an
    // unexpected click on the input element
    focusEvent.preventDefault();

    if (this.ngModel.$modelValue === '') {
      this.openLinkPicker();
    } else {
      this._focusClearButton();
    }
  }

  _focusClearButton() {
    this.$element.find('.hippo-node-link-clear').focus();
  }

  _focusSelectButton() {
    this.$element.find('.hippo-node-link-select').focus();
  }

  // set "hasFocus" to false after a short timeout to prevent the bottom-border styling
  // of the link picker to flicker while tabbing; it *can* trigger a blur event, followed by
  // a immediate focus event, in which case the blue bottom border will be removed and added
  // again, resulting in annoying flickering of the UI.
  blur($event) {
    if (this.mdInputContainer) {
      this.mdInputContainer.setFocused(false);
    }

    this.blurPromise = this.$timeout(() => {
      this.hasFocus = false;
      this.onBlur($event);
    }, 10);
  }

  focus($event) {
    if (this.mdInputContainer) {
      this.mdInputContainer.setFocused(true);
    }

    if (this.blurPromise) {
      this.$timeout.cancel(this.blurPromise);
    }
    this.hasFocus = true;
    this.onFocus($event);
  }

  openLinkPicker() {
    const uuid = this.ngModel.$modelValue;
    return this.PickerService.pickLink(this.config.linkpicker, { uuid })
      .then(link => this._onLinkPicked(link))
      .catch(() => this._focusSelectButton());
  }

  _onLinkPicked(link) {
    if (this.linkPicked) {
      this._focusSelectButton();
    }
    this.linkPicked = true;
    this.displayName = link.displayName;
    this.ngModel.$setViewValue(link.uuid);
  }

  clear() {
    this.linkPicked = false;
    this.displayName = '';
    this.ngModel.$setTouched();
    this.ngModel.$setViewValue('');
    this._focusSelectButton();
  }
}

export default nodeLinkController;
