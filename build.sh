#!/bin/bash

rm -rf src/main/resources
mkdir -p src/main/resources

bower install
cp -r public src/main/resources/public

gradle fatJar
