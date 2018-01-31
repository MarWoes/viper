#!/bin/bash
rm -rf src/main/resources
rm -rf build/grunt
mkdir -p src/main/resources

bower --allow-root install
npm install
grunt
cp -r build/grunt src/main/resources/public

gradle fatJar

rm -rf build/zip/
mkdir -p build/zip

cp build/libs/VIPER-all.jar build/zip/VIPER.jar
cp config.json build/zip/
cp igv.properties build/zip/
cp igv.jar build/zip/
cp start.bat build/zip/

cd build/zip
zip viper-$(git describe --tags).zip *
