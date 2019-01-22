# Spark on Kubernetes

This repo is decomposed in several proofs of concept :

<!--ts-->
   * [Proofs of concept](#proofs-of-concept)
      * [Livy + Spark 2.3 using Kubernetes scheduler](#livy--spark-23-using-kubernetes-scheduler)
      * [S3FS](#s3fs)
<!--te-->

Proofs of concept
=================

**Note for the following sections**

Whether you're using kops to create a cluster on AWS or, say an AKS cluster (Kubernetes as a Service on Azure), you will need to setup your AWS and Azure credentials in Kubernetes to access respectively your S3 bucket and your Azure blob storage container.

Create those credentials using the following commands before applying the following Kubernetes configurations:

``` shell
kubectl create secret generic aws \
    --from-literal=accesskey=$(aws configure get aws_access_key_id) \
    --from-literal=secretkey=$(aws configure get aws_secret_access_key)

```


Livy + Spark 2.3 using Kubernetes scheduler
-------------------------------------------

This proof of concept allows you to use Spark 2.3 Kubernetes features.

Spark 2.3 is able to submit Spark jobs (only in cluster deploy mode) to a Kubernetes cluster.

That means you cannot use it with a spark-shell, only spark-submit and you cannot upload your jar with spark-submit, so it has to be available locally or through http (yet).

It will use the Kubernetes scheduler to create Spark drivers and executors dynamically.

Refer to the [Livy/Spark README](./livy-spark-2.3/README.md) for more details.

#### Testing autoscaling

```shell
./bin/spark-submit --class org.apache.spark.examples.SparkPi --deploy-mode client --master spark://192.168.99.100:30077 examples/jars/spark-examples_2.11-2.2.1.jar 10000
```
Stimulate CPU usage in a pod :

``` shell
dd if=/dev/urandom | bzip2 -9 >> /dev/null
```

S3FS
----

This is kind of a hack to expose your Spark jobs (jars) hosted on S3 so that they can be used with Spark 2.3 using Kubernetes scheduler.

The Spark 2.3 container is responsible for downloading dependencies (this is done in its initcontainer).

However, the initcontainer doesn't seem able (yet) to download the Spark jar containing the job with s3://.

S3FS is, consequently, responsible of mounting a S3 bucket as a file system in a container and exposing its content through HTTP, internally to the Kubernetes cluster.

This allows the initcontainer to download the job through HTTP.
