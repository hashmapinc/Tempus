#####################
Kubeless Client Guide
#####################

************
Introduction
************

Kubeless is a serverless framework on top of Kubernetes, which provides a way to deploy & run "functions" on K8s cluster.

Function can be any source code written in PHP, Java 1.8, Node.js, Golang, .Net, Ruby or Python, having a method which takes event and context as parameters. Check this `link`_ for more details on function.

Triggers are associated with functions to invoke them on specific event, examples are HttpTrigger, KafkaTrigger and CronTrigger where Http call will trigger execution of function having HttpTrigger, KafkaTrigger will invoke function once data is published to specified topic and CronTrigger will be invoked on specific schedule.

Kubeless java-client provides APIs to deploy and perform other operations on functions & triggers over k8s cluster.

Below is detailed usage examples.

.. _link: https://kubeless.io/docs/kubeless-functions/

*****
Usage
*****

API Client
==========

Kubeless java-client provides ApiClient from Kubernetes Java client to communicate with K8s API server.

ApiClient can be used depending upon, from where you are going to deploy your function.

* If you will be deploying functions from code running in Pod from same cluster use below code

.. code-block:: java

    import io.kubernetes.client.util.Config;
    ApiClient client = Config.fromCluster()

* From outside cluster

.. code-block:: java

    import io.kubernetes.client.util.Config;
    String filename = "/path/to/kubeconfig";
    ApiClient client = Config.fromConfig(filename)

If you use Config.defaultClient() it attempts to auto detect where the code is being used and creates a client object accordingly.
It first looks for a kubeconfig file in environment variable $KUBECONFIG, then falls back to path $HOME/.kube/config. If that doesn't work, it falls back to an in-cluster mode by detecting a default service account. Lastly, if none of this works, it attempts to connect to the server at http://localhost:8080

Once ApiClient is built set it as Default client to use for further requests as

.. code-block:: java

    Configuration.setDefaultApiClient(client);


Function API
============

KubelessV1beta1FunctionApi contains APIs for communicating with k8s cluster with Kubeless. It contains APIs to

* list all functions
* fetch specific function by name
* delete specific function by name
* create new function
* update existing function

To create a new instance of FunctionApi client you need to pass "namespace" within which functions will be created

.. code-block:: java

    KubelessV1beta1FunctionApi functionApi = new KubelessV1beta1FunctionApi("default");

This will pick up ApiClient created in earlier step from Configuration object.

Below is complete code that will be used to list all functions from default namespace

.. code-block:: java

    import com.hashmapinc.kubeless.apis.KubelessV1beta1FunctionApi;
    import com.hashmapinc.kubeless.models.V1beta1Function;
    import com.hashmapinc.kubeless.models.V1beta1FunctionList;
    import com.squareup.okhttp.Call;
    import com.squareup.okhttp.Response;
    import io.kubernetes.client.ApiClient;
    import io.kubernetes.client.ApiException;
    import io.kubernetes.client.Configuration;
    import io.kubernetes.client.JSON;
    import io.kubernetes.client.util.Config;
    import java.io.IOException;

    public class TestKubelessClient {

        public static void main(String[] args) throws IOException, ApiException {
            ApiClient client = Config.defaultClient();
            Configuration.setDefaultApiClient(client);

            KubelessV1beta1FunctionApi functionApi = new KubelessV1beta1FunctionApi("default");
            Call listFunctionsCall = functionApi.listFunctionsCall();

            Response response = listFunctionsCall.execute();

            JSON json = new JSON();
            V1beta1FunctionList functionList = json.deserialize(response.body().string(), V1beta1FunctionList.class);

            for (V1beta1Function item : functionList.getItems()) {
                System.out.println(item.getMetadata().getName());
            }
        }
    }

As you can see JSON object is used here to deserialize a response from K8s API server, which internally uses Gson registered with few Data converters like Date time.

All the models which are used as Request and Response needs to be Serialized and Deserialized, respectively, using JSON instance only.

Similarly API client can be used to create a function, which requires a target V1beta1Function instance

.. code-block:: java

    public Call createFunctionCall(V1beta1Function function) throws ApiException

Sample of creation of function object is as below

.. code-block:: java

    V1beta1AbstractType<V1beta1FunctionSpec> function = new V1beta1Function()
                .metadata(new V1ObjectMeta()
                        .name("functionName")
                        .namespace("default"))
                .spec(new V1beta1FunctionSpec()
                        .checksum("sha256 of function")
                        .function("function string or url")
                        .dependencies("dependency file file as string if needed")
                        .timeout("180")
                        .handler("functionFilename.methodName")
                        .runtime("Java1.8"));
    KubelessV1beta1FunctionApi functionApi = new KubelessV1beta1FunctionApi("default");
    Call createFunctionsCall = functionApi.createFunctionCall((V1beta1Function) function);

    Response response = createFunctionsCall.execute();

    JSON json = new JSON();
    V1beta1Function functionCreated = json.deserialize(response.body().string(), V1beta1Function.class);



Trigger API
===========

Once function is created next step is to add a trigger which will trigger an execution of function. Let's look at an example of adding Kafka trigger

To create a Kafka trigger we need to provide few labels to match with a function to which we want to add a trigger like below:

.. code-block:: java

    V1beta1AbstractType<V1beta1KafkaTriggerSpec> trigger = new V1beta1KafkaTrigger()
                .metadata(new V1ObjectMeta()
                        .name("TestKafkaTrigger"))
                .spec(new V1beta1KafkaTriggerSpec()
                        .topic("test-topic")
                        .labelSelector(new V1LabelSelector()
                                .putMatchLabelsItem("created-by", "kubeless")
                                .putMatchLabelsItem("function", "functionName")));

Then using Kubeless Trigger API client we can send a request to deploy this trigger on K8s cluster as below,

.. code-block:: java

    KubelessV1beta1KafkaTriggerApi triggerApi = new KubelessV1beta1KafkaTriggerApi("default");
    Call kafkaTriggerCall = triggerApi.createKafkaTriggerCall((V1beta1KafkaTrigger) trigger);
    Response result = kafkaTriggerCall.execute();