FROM ubuntu:19.04

RUN apt-get update

RUN apt-get install -y openjdk-8-jdk
ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64/
RUN export JAVA_HOME

ENV SCALA_VERSION 2.12.8
RUN apt-get install -y wget
RUN wget http://scala-lang.org/files/archive/scala-$SCALA_VERSION.deb
RUN dpkg -i scala-$SCALA_VERSION.deb
RUN apt-get update
RUN apt-get install -y scala

RUN apt-get install -y curl gnupg
RUN echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
RUN apt-get update
RUN apt-get install -y sbt

RUN apt-get install -y vim git unzip

RUN apt-get install -y npm
RUN npm install npm@6.8 -g

EXPOSE 8000
EXPOSE 9000
EXPOSE 5000
EXPOSE 8888

WORKDIR /home/dominik-nowogorski/projekt
VOLUME /home/dominik-nowogorski/projekt
