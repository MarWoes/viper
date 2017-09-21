#!/bin/bash
rm -rf src/main/resources
mkdir -p src/main/resources

bower --allow-root install
cp -r public src/main/resources/public

gradle fatJar
