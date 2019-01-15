# Use Kubernetes scheduler with Spark's Kubernetes capabilities
## Getting Started
This package aims at using livy on top of spark 2.3 (using the kubernetes scheduler).

You can submit a Spark job using two different methods :

#### Using Livy REST interface
To get that up and running, simply apply the following configuration in your Kubernetes cluster : 

```shell
kubectl apply -f livy-deployment.yaml
```

It's not possible yet to upload a jar so your jar has to be available from a URL (or locally), so, you may want to add a s3fs pod. 
This pod aims at mounting a s3 bucket and serving it internally by http (in kubernetes cluster).
It allows to access spark programs executables from s3 through http without making them public.

```shell
kubectl apply -f ../s3fs/s3fs-kubernetes.yaml
```

As a result, you can use Livy to submit a Spark job.

You can use Postman (https://www.getpostman.com/) and import [the provided postman collection in the repo](https://github.com/ttauveron/spark_k8s/blob/master/Livy%20REST%20API.postman_collection.json) (available at the root of the repo) to test Livy REST API.

Sessions are not working with Spark 2.3 Kubernetes features as spark-shell is not yet implemented, therefore, only 2.X request will be working because Spark 2.3 using Kubernetes doesn't support interactive shell.
In the Postman Collection, you can configure a collection variable to define the Livy IP for all requests. Details available in here : https://www.getpostman.com/docs/v6/postman/environments_and_globals/variables#defining-collection-variables

Try the *Batch - Submit jar* request for example.

#### Locally
You can also test it with a local instance of Spark 2.3

First, download Spark 2.3

``` shell
wget -P /opt https://www.apache.org/dist/spark/spark-2.3.0/spark-2.3.0-bin-hadoop2.7.tgz
cd /opt
tar xvzf spark-2.3.0-bin-hadoop2.7.tgz
rm spark-2.3.0-bin-hadoop2.7.tgz
cd spark-2.3.0-bin-hadoop2.7
```
Then create a proxy to access the Kubernetes API from your localhost : 
```shell
kubectl proxy --address 0.0.0.0 --port=8443 --accept-hosts ".*"&
```

Submit a job to the kubernetes cluster :
```shell
bin/spark-submit --master k8s://http://127.0.0.1:8443 \
         --deploy-mode cluster \
         --name spark-pi \
         --class org.apache.spark.examples.SparkPi \
         --conf spark.executor.instances=5 \
         --conf spark.kubernetes.container.image=gnut3ll4/spark:v1.0.2 \
         local:///opt/spark/examples/target/original-spark-examples_2.11-2.3.0.jar
```

The livy-deployment (running on Kubernetes) uses a sidecar container in its pod in order to provide a kubectl proxy. Actually, Spark will be using Kubernetes API to create Spark driver and executors pods.

Finally, if you want to run the livy container locally, you can use Docker without kubernetes with the following commands :

```shell
# Creating the proxy for the Kubernetes cluster
kubectl proxy --address 0.0.0.0 --port=8443 --accept-hosts ".*"

# Replace K8S_API_HOST by your local IP (has to be accessible from a container)
docker run --rm --name livy -d -p 8998:8998 -e K8S_API_HOST=thibaut.dev.ticksmith.com livy
```

## Building the Kubernetes container

Use your spark 2.3 downloaded package to build the docker container that will be automatically created by Kubernetes when submitting a Spark job.

In order to allow Spark to access Azure blob storage or S3 files, add the following lines to **/opt/spark-2.3.0-bin-hadoop2.7/kubernetes/dockerfiles/spark/Dockerfile** before building the docker container.

```Dockerfile
RUN wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.7.4/aws-java-sdk-1.7.4.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.7.3/hadoop-aws-2.7.3.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/com/microsoft/azure/azure-storage/7.0.0/azure-storage-7.0.0.jar && \
    wget -P ${SPARK_HOME}/jars http://central.maven.org/maven2/org/apache/hadoop/hadoop-azure/2.7.5/hadoop-azure-2.7.5.jar
```

Then, build your docker spark image and push it to Dockerhub :

``` shell
bin/docker-image-tool.sh -r gnut3ll4 -t v1.0.2 build
bin/docker-image-tool.sh -r gnut3ll4 -t v1.0.2 push
```
