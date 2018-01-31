FROM java:8
MAINTAINER Marius Wöste

COPY . /viper

RUN apt-get update && \
  apt-get install -y xvfb gradle curl wget gtk+3.0 zip && \
  curl -sL https://deb.nodesource.com/setup_6.x | bash - && \
  apt-get install -y nodejs && \
  npm config set registry http://registry.npmjs.org/ && \
  npm install -g bower && \
  npm install -g grunt-cli

RUN cd /viper && \
  curl -o igv.jar https://uni-muenster.sciebo.de/index.php/s/7YptrvcDLz56tn7/download

CMD cd /viper && ./build.sh && gradle test && cp build/zip/*.zip /artifacts/
