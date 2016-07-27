/*
 * Copyright 2016 Hippo B.V. (http://www.onehippo.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */'use strict';

var fs = require('fs');

module.exports = function (grunt) {

  // load all grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // display execution time of each task
  require('time-grunt')(grunt);

  var buildConfig = require('./build.config.js');

  grunt.initConfig({

    build: buildConfig,

    copy: {
      js: {
        files: [
          {
            expand: true,
            nonull: true,
            cwd: '<%= build.npmDir %>/jstimezonedetect/dist',
            src: 'jstz.min.js',
            dest: '<%= build.jstimezonedetect.target %>/'
          }
        ]
      }
    },

    clean: {
      // clean npm components
      npm: {
        src: '<%= build.npmDir %>'
      },
    },
  });

  grunt.registerTask('default', ['build']);

  // build
  grunt.registerTask('build', [
    'copy:js'
  ]);
};
