#
# Copyright © 2016-2018 The Thingsboard Authors
# Modifications © 2017-2018 Hashmap, Inc
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

FROM java:8-alpine

ENV SPARK_HOME=/opt/spark
ENV LIVY_HOME=/opt/livy
ENV HADOOP_CONF_DIR=/etc/hadoop/conf
ENV SPARK_USER=ticksmith

ARG AWS_JAVA_SDK_VERSION=1.7.4
ARG HADOOP_AWS_VERSION=2.7.3

WORKDIR /opt

RUN apk add --update openssl wget bash && \
    wget -P /opt https://www.apache.org/dist/spark/spark-2.3.1/spark-2.3.1-bin-hadoop2.7.tgz && \
    tar xvzf spark-2.3.1-bin-hadoop2.7.tgz && \
    rm spark-2.3.1-bin-hadoop2.7.tgz && \
    ln -s /opt/spark-2.3.1-bin-hadoop2.7 /opt/spark

RUN wget http://mirror.its.dal.ca/apache/incubator/livy/0.5.0-incubating/livy-0.5.0-incubating-bin.zip && \
    unzip livy-0.5.0-incubating-bin.zip && \
    rm livy-0.5.0-incubating-bin.zip && \
    ln -s /opt/livy-0.5.0-incubating-bin /opt/livy && \
    mkdir /var/log/livy && \
    ln -s /var/log/livy /opt/livy/logs && \
    cp /opt/livy/conf/log4j.properties.template /opt/livy/conf/log4j.properties

RUN wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/$HADOOP_AWS_VERSION/hadoop-aws-$HADOOP_AWS_VERSION.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/$AWS_JAVA_SDK_VERSION/aws-java-sdk-$AWS_JAVA_SDK_VERSION.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/com/microsoft/azure/azure-storage/7.0.0/azure-storage-7.0.0.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/org/apache/hadoop/hadoop-azure/2.7.5/hadoop-azure-2.7.5.jar
    

EXPOSE 8998


ADD livy.conf /opt/livy/conf
ADD spark-defaults.conf /opt/spark/conf/spark-defaults.conf
ADD entrypoint.sh /entrypoint.sh

ENV PATH="/opt/livy/bin:${PATH}"

ENTRYPOINT ["/entrypoint.sh"]
CMD ["livy-server"]
