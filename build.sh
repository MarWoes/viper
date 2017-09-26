#!/bin/bash
rm -rf src/main/resources
rm -rf build/grunt
mkdir -p src/main/resources

bower --allow-root install
npm install
grunt
cp -r build/grunt src/main/resources/public

gradle fatJar
