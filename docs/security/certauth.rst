#####################################
X.509 Certificate Based Authenication
#####################################

X.509 Certificate Based Authentication is used in Two-Way SSL connection. In this case, the certificate itself is the client’s ID, thus, Access Token is no longer needed.

Instructions below will describe how to generate a client-side certificate and connect to the server that is running MQTT over SSL. You will need to have the public key of the server certificate in PEM format. See :doc:`following instructions <mqttssl>` for more details on server-side configuration.

********************
Set keygen variables
********************

Export the following enviroment variable, and update the values if needed:

.. code-block:: bash

    DOMAIN_SUFFIX="$(hostname)"
    ORGANIZATIONAL_UNIT=Tempus
    ORGANIZATION=Tempus
    CITY=Roswell
    STATE_OR_PROVINCE=GA
    TWO_LETTER_COUNTRY_CODE=US

    SERVER_KEYSTORE_PASSWORD=server_ks_password
    SERVER_KEY_PASSWORD=server_key_password

    SERVER_KEY_ALIAS="serveralias"
    SERVER_FILE_PREFIX="mqttserver"
    SERVER_KEYSTORE_DIR="/etc/tempus/conf/"

    CLIENT_KEYSTORE_PASSWORD=password
    CLIENT_KEY_PASSWORD=password

    CLIENT_KEY_ALIAS="clientalias"
    CLIENT_FILE_PREFIX="mqttclient"

************************
Generating SSL Key Pair
************************

Execute the folling commnaded to generate the SSL key pair.

.. code-block:: bash

    keytool -genkeypair -v \
    -alias $SERVER_KEY_ALIAS \
    -dname "CN=$DOMAIN_SUFFIX, OU=$ORGANIZATIONAL_UNIT, O=$ORGANIZATION, L=$CITY, ST=$STATE_OR_PROVINCE, C=$TWO_LETTER_COUNTRY_CODE" \
    -keystore $SERVER_FILE_PREFIX.jks \
    -keypass $SERVER_KEY_PASSWORD \
    -storepass $SERVER_KEYSTORE_PASSWORD \
    -keyalg RSA \
    -keysize 2048 \
    -validity 9999

    keytool -export \
    -alias $SERVER_KEY_ALIAS \
    -keystore $SERVER_FILE_PREFIX.jks \
    -file $SERVER_FILE_PREFIX.pub.pem -rfc \
    -storepass $SERVER_KEYSTORE_PASSWORD

    keytool -export \
    -alias $SERVER_KEY_ALIAS \
    -file $SERVER_FILE_PREFIX.cer \
    -keystore $SERVER_FILE_PREFIX.jks \
    -storepass $SERVER_KEYSTORE_PASSWORD \
    -keypass $SERVER_KEY_PASSWORD

    mkdir -p $SERVER_KEYSTORE_DIR
    cp $SERVER_FILE_PREFIX.jks $SERVER_KEYSTORE_DIR

The keytool commands outputs the following files:

* **CLIENT_FILE_PREFIX.jks** - Java Keystore file with the server certificate imported
* **CLIENT_FILE_PREFIX.nopass.pem** - Client certificate file in PEM format to be used by non-java client
* **CLIENT_FILE_PREFIX.pub.pem** - Client public key

*************************************************
Provision Client Public Key as Device Credentials
*************************************************

Go to **Tempus Web UI -> Devices -> Your Device -> Device Credentials**. Select X.509 Certificate device credentials, insert the contents of **CLIENT_FILE_PREFIX.pub.pem** file and click save. Alternatively, the same can be done through the REST API.
