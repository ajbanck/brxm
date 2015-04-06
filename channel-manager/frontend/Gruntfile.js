/*
 * Copyright 2014-2015 Hippo B.V. (http://www.onehippo.com)
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

module.exports = function (grunt) {
  'use strict';

  function readDeclutterConfig () {
    return grunt.file.readJSON('declutter.config.json');
  }

  function readDeclutteredComponentFiles () {
    var declutterConfig = readDeclutterConfig(),
      components = Object.keys(declutterConfig),
      declutteredFiles = [];

    components.forEach(function (component) {
      var componentRules = declutterConfig[component];
      componentRules.forEach(function (rule) {
        declutteredFiles.push(component + '/' + rule);
      });
    });

    return declutteredFiles;
  }

  // display execution time of each task
  require('time-grunt')(grunt);

  // load all grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // project configuration
  grunt.initConfig({
    build: require('./build.config.js'),

    // watch source files and rebuild when they change
    watch: {
      options: {
        spawn: false,
        interrupt: true,
        livereload: true
      },

      apps: {
        files: [
          '<%= build.ngsource %>/**/*'
        ],
        tasks: ['build']
      },

      extjs: {
        files: ['src/main/resources/**/*.{html,js,css,png,svg}'],
        tasks: ['newer:copy:extjs']
      }
    },

    // clean target (distribution) folder
    clean: {
      target: {
        src: '<%= build.ngtarget %>'
      },

      bower: {
        src: '<%= build.bower %>/**'
      }
    },

    // Compile LessCSS to CSS.
    less: {
      menu: {
        files: {
          '<%= build.ngtarget %>/menu/assets/css/menu.css': '<%= build.ngsource %>/menu/assets/less/menu.less'
        }
      }
    },

    // copy files to target folder
    copy: {
      apps: {
        files: [
          {
            expand: true,
            cwd: '<%= build.ngsource %>',
            dest: '<%= build.ngtarget %>',
            src: [
              '**/*.html',
              '**/*.js',
              '!**/*.spec.js',
              '**/assets/css/*',
              '!**/assets/less/*',
              '**/assets/images/*',
              '**/i18n/*.json'
            ]
          }
        ]
      },

      components: {
        files: [
          {
            expand: true,
            cwd: '<%= build.bower %>',
            dest: '<%= build.ngtarget %>/components',
            src: readDeclutteredComponentFiles()
          }
        ]
      },

      extjs: {
        files: [
          {
            expand: true,
            cwd: '<%= build.extjssource %>',
            src: ['**/*.{html,js,css,png,svg}'],
            dest: '<%= build.extjstarget %>'
          }
        ]
      }
    },

    // add hashes to file names to avoid caching issues
    filerev: {
      css: {
        src: '<%= build.ngtarget %>/**/*.css'
      },
      js: {
        src: '<%= build.ngtarget %>/**/*.js'
      },
      images: {
        src: '<%= build.ngtarget %>/**/*.{gif,png}'
      }
    },

    // replace file references in HTML files with their hashed versions
    usemin: {
      css: '<%= build.ngtarget %>/**/*.css',
      html: '<%= build.ngtarget %>/**/*.html'
    },

    // only use a sub-set of files in Bower components
    declutter: {
      options: {
        rules: readDeclutterConfig()
      },
      files: [
        '<%= build.bower %>/*'
      ]
    },

    // validate source code with jslint
    jshint: {
      options: {
        reporter: require('jshint-stylish'),
        jshintrc: true
      },
      apps: [
        '<%= build.ngsource %>/**/*.js',
        '!<%= build.ngsource %>/**/*.spec.js'
      ],
      tests: [
        '<%= build.ngsource %>/**/*.spec.js'
      ]
    },

    // testing with karma
    karma: {
      options: {
        configFile: 'karma.config.js',
        autoWatch: true
      },

      single: {
        singleRun: true
      },

      continuous: {
        singleRun: false
      }
    }
  });

  grunt.registerTask('default', 'Build and test', [
    'build',
    'test'
  ]);

  grunt.registerTask('build', 'Build everything', [
    'jshint:apps',
    'declutter',
    'clean',
    'less',
    'copy',
    'filerev',
    'usemin'
  ]);

  grunt.registerTask('test', 'Test the source code', [
    'jshint:tests',
    'karma:single'
  ]);

  grunt.registerTask('test:continuous', 'Test the source code continuously', [
    'jshint:tests',
    'karma:continuous'
  ]);
};