module.exports = function(grunt) {
  // load grunt tasks based on dependencies in package.json
  require('load-grunt-tasks')(grunt);

  grunt.config.init({
    useminPrepare: {
        html: 'public/index.html',
        options: {
          root: 'public',
          dest: 'build/grunt'
        }
    },

    usemin: {
       html:['build/grunt/index.html']
    },

    copy: {
      fonts: {
        expand: true,
        cwd: 'public/bower_components/bootstrap',
        src: ['fonts/**/*'],
        dest: 'build/grunt/assets'
      },
      html: {
        expand: true,
        cwd: 'public/',
        src: ['viper/**/*.html', "index.html"],
        dest: 'build/grunt/'
      },
      images: {
        expand: true,
        cwd: 'public/images',
        src: ['**/*'],
        dest: 'build/grunt/images'
      },
      favicon: {
        src: 'public/favicon.ico',
        dest: 'build/grunt/favicon.ico'
      }
    }
  });

  grunt.registerTask('default',[
    'copy',
    'useminPrepare',
    'concat',
    'uglify',
    'cssmin',
    'usemin'
    ]);
}
