FROM centos:7
RUN mkdir -p /data/demoTest

ADD jdk-8u301-linux-x64.tar.gz /usr/local
COPY DubboTest-1.0-SNAPSHOT.jar /data/demoTest
COPY config.properties /data/demoTest

ENV JAVA_HOME /usr/local/jdk1.8.0_301
ENV PATH $PATH:$JAVA_HOME/bin

WORKDIR /data/demoTest

ENTRYPOINT ["java","-jar","DubboTest-1.0-SNAPSHOT.jar"]
