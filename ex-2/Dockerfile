FROM ubuntu

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
RUN apt-get install -y sbt=1.3.6

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

EXPOSE 9000

CMD sbt run