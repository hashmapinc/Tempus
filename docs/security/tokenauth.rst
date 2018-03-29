################################
Access Token based authenication
################################

Access Token Based Authentication is the default device authentication type. Once the device is created in Tempus, the default access token is generated. It can be changed afterwards. In order to connect the device to a server using Access Token based authentication, the client must specify the access token as part of request URL (for HTTP and CoAP) or as a user name in MQTT connect message. See :doc:`supported protocols API <../api/index>` for more details.

****************
One-Way MQTT SSL
****************

One-way SSL authentication is a standard authentication mode, where your client device verifies the identity of a server using server certificate. In order to run one-way MQTT SSL, the server certificate chain should be signed by authorized CA or client must import the self-signed server certificate (.cer or .pem) to its trust store. Otherwise, a connection will fail with the ‘Unknown CA’ error.
