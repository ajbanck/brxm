/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
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

const ANGULAR_MATERIAL_SIDENAV_EASING = [0.25, 0.8, 0.25, 1];
const ANGULAR_MATERIAL_SIDENAV_ANIMATION_DURATION_MS = 400;

export class ScalingService {

  constructor($rootScope, $window, OverlaySyncService) {
    'ngInject';

    this.$rootScope = $rootScope;
    this.OverlaySyncService = OverlaySyncService;

    this.pushWidth = 0; // all sidenavs are initially closed
    this.viewPortWidth = 0; // unconstrained
    this.scaleFactor = 1.0;
    this.scaleDuration = ANGULAR_MATERIAL_SIDENAV_ANIMATION_DURATION_MS;
    this.scaleEasing = ANGULAR_MATERIAL_SIDENAV_EASING;

    angular.element($window).bind('resize', () => {
      if (this.hippoIframeJQueryElement) {
        $rootScope.$apply(() => this._updateScaling(false));
      }
    });
  }

  init(hippoIframeJQueryElement) {
    this.hippoIframeJQueryElement = hippoIframeJQueryElement;
    this._updateScaling(false);
  }

  setPushWidth(pushWidth) {
    this.pushWidth = pushWidth;
    this._updateScaling(true);
  }

  setViewPortWidth(viewPortWidth) {
    this.viewPortWidth = viewPortWidth;
    this._updateViewPortWidth();
  }

  getScaleFactor() {
    return this.scaleFactor;
  }

  _updateViewPortWidth() {
    if (!this.hippoIframeJQueryElement) {
      return;
    }

    const elementsToScale = this.hippoIframeJQueryElement.find('.cm-scale');
    const maxWidthCSS = this.viewPortWidth === 0 ? 'none' : `${this.viewPortWidth}px`;
    elementsToScale.css('max-width', maxWidthCSS);

    this._updateScaling(false);
    this.OverlaySyncService.syncIframe();
  }

  /**
   * Update the iframe shift, if necessary
   *
   * The iframe should be shifted right (by controlling the left-margin) if the sidenav is open,
   * and if the viewport width is less than the available canvas
   *
   * @param animate  flag indicating whether any shift-change should be automated or immediate.
   * @returns {*[]}  canvasWidth is the maximum width available to the iframe
   *                 viewPortWidth indicates how many pixels wide the iframe content should be rendered.
   */
  _updateIframeShift(animate) {
    const currentShift = parseInt(this.hippoIframeJQueryElement.css('margin-left'), 10);
    const canvasWidth = this.hippoIframeJQueryElement.find('.channel-iframe-canvas').width() + currentShift;
    const viewPortWidth = this.viewPortWidth === 0 ? canvasWidth : this.viewPortWidth;
    const canvasBorderWidth = canvasWidth - viewPortWidth;
    const targetShift = Math.min(canvasBorderWidth, this.pushWidth);

    if (targetShift !== currentShift) {
      if (animate) {
        this.hippoIframeJQueryElement.velocity('finish');
        this.hippoIframeJQueryElement.velocity({
          'margin-left': targetShift,
        }, {
          duration: this.scaleDuration,
          easing: this.scaleEasing,
        });
      } else {
        this.hippoIframeJQueryElement.css('margin-left', targetShift);
      }
    }

    return [canvasWidth, viewPortWidth];
  }

  _updateScaling(animate) {
    if (!this.hippoIframeJQueryElement) {
      return;
    }

    const [canvasWidth, viewPortWidth] = this._updateIframeShift(animate);
    const visibleCanvasWidth = canvasWidth - this.pushWidth;
    const oldScale = this.scaleFactor;
    const newScale = (visibleCanvasWidth < viewPortWidth) ? visibleCanvasWidth / viewPortWidth : 1;

    const startScaling = oldScale === 1.0 && newScale !== 1.0;
    const stopScaling = oldScale !== 1.0 && newScale === 1.0;
    const animationDuration = (startScaling || stopScaling) ? this.scaleDuration : 0;

    const elementsToScale = this.hippoIframeJQueryElement.find('.cm-scale');
    elementsToScale.velocity('finish');

    if (startScaling || stopScaling) {
      const iframeBaseJQueryElement = this.hippoIframeJQueryElement.find('.channel-iframe-base');
      const currentOffset = iframeBaseJQueryElement.scrollTop();
      const targetOffset = startScaling ? newScale * currentOffset : currentOffset / oldScale;

      if (animate) {
        elementsToScale.velocity('scroll', {
          container: iframeBaseJQueryElement,
          offset: targetOffset - currentOffset,
          duration: animationDuration,
          easing: this.scaleEasing,
          queue: false,
        });
      } else {
        elementsToScale.scrollTop(targetOffset);
      }
    }

    if (animate) {
      elementsToScale.velocity({
        scale: newScale,
      }, {
        duration: animationDuration,
        easing: this.scaleEasing,
      });
    } else {
      elementsToScale.css('transform', `scale(${newScale})`);
    }

    this.scaleFactor = newScale;
  }

}
