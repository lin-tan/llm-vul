FROM openjdk:8-jdk-alpine
LABEL Yi Wu <yiwu5cs@gmail.com>


RUN  apk add --no-cache curl tar bash procps

# Install Maven 3.8.6
RUN wget https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz -P /tmp
RUN tar -xzf /tmp/apache-maven-3.8.6-bin.tar.gz -C /opt && rm /tmp/apache-maven-3.8.6-bin.tar.gz
ENV PATH $PATH:/opt/apache-maven-3.8.6/bin


# Install Gradle 3.1
RUN wget https://services.gradle.org/distributions/gradle-3.1-bin.zip -P /tmp
# => ERROR [6/8] RUN unzip -d /opt/gradle /tmp/gradle-*.zip && rm /tmp/gradle-*.zip  
# make the dir first
RUN mkdir /opt/gradle
RUN unzip -d /opt/gradle /tmp/gradle-*.zip && rm /tmp/gradle-*.zip
ENV PATH $PATH:/opt/gradle/gradle-3.1/bin

# install python3
# RUN apt-get update
# RUN apt-get install -y python3
# https://stackoverflow.com/questions/62554991/how-do-i-install-python-on-alpine-linux

ENV PYTHONUNBUFFERED=1
RUN apk add --update --no-cache python3 && ln -sf python3 /usr/bin/python
RUN python3 -m ensurepip
RUN pip3 install --no-cache --upgrade pip setuptools

# install git
# --no-cache keeping the container small
RUN apk add --no-cache git

# mkdir /vjbench/
RUN mkdir -p /vjbench/projects
COPY ./build_dataset.py /vjbench
COPY ./VJBench_data.json /vjbench/
RUN python3 /vjbench/build_dataset.py

CMD [""]
