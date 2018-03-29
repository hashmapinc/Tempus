#############
MQTT over SSL
#############

Tempus provides the ability to run MQTT server over SSL. Both one-way and two-way SSL are supported. To enable SSL, you will need to obtain a valid or generate a self-signed SSL certificate and add it to the keystore. Once added, you will need to specify the keystore information in **tempus.yml** file. See the instructions on how to generate SSL certificate and use it in your Tempus installation below. You can skip certificate generation step if you already have a certificate.

**********************************
Self-signed certificate generation
**********************************

**Note** This step requires Linux based OS with Java installed.

Set and export the following enviroment variables.  Updating the values where needed.  For example:

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

    CLIENT_TRUSTSTORE="client_truststore"
    CLIENT_KEY_ALIAS="clientalias"
    CLIENT_FILE_PREFIX="mqttclient"

where

* **DOMAIN_SUFFIX** - Corresponds to **CN** value of the certificate. Must correspond to the target server domain (wildcards are allowed). Defaults to the current hostname
* **ORGANIZATIONAL_UNIT** - Corresponds to **OU** value of the certificate.
* **ORGANIZATION** - Corresponds to **O** value of the certificate.
* **CITY** - Corresponds to **L** value of the certificate.
* **STATE_OR_PROVINCE** - Corresponds to **ST** value of the certificate.
* **TWO_LETTER_COUNTRY_CODE** - Corresponds to **C** value of the certificate.
* **SERVER_KEYSTORE_PASSWORD** - Server Keystore password
* **SERVER_KEY_PASSWORD** - Server Key password. May or may not be the same as SERVER_KEYSTORE_PASSWORD
* **SERVER_KEY_ALIAS** - Server key alias. Must be unique within the keystore
* **SERVER_FILE_PREFIX** - Prefix to all server keygen-related output files
* **SERVER_KEYSTORE_DIR** - The default location where the key would be optionally copied. Can be overriden by -d option in server.keygen.sh script or entered manually upon the scrip run

The rest of the values are not important for the server keystore generation
To run the server keystore generation, use following commands.

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


The keytool will used the configuration specified and will generate the following output files:

* **SERVER_FILE_PREFIX.jks** - Java keystore file. This is the file which will be used by Tempus MQTT Service
* **SERVER_FILE_PREFIX.cer** - Server public key file. It will be then imported to client’s .jks keystore file.
* **SERVER_FILE_PREFIX.pub.pem** - Server public key in **PEM** format, which can be then used as a keystore or imported by non-Java clients.

To copy the keystore file, upload it manually to a directory which is in server’s classpath. You may want to modify owner and permissions for the keystore file:

.. code-block:: bash

    sudo chmod 400 /etc/tempus/conf/mqttserver.jks
    sudo chown tempus:tempus /etc/tempus/conf/mqttserver.jks

********************
Server configuration
********************

Locate your **tempus.yml** file and uncomment the lines after `“# Uncomment the following lines to enable ssl for MQTT”:`

.. code-block:: bash

    # MQTT server parameters
    mqtt:
    bind_address: "${MQTT_BIND_ADDRESS:0.0.0.0}"
    bind_port: "${MQTT_BIND_PORT:8883}"
    adaptor: "${MQTT_ADAPTOR_NAME:JsonMqttAdaptor}"
    timeout: "${MQTT_TIMEOUT:10000}"
    # Uncomment the following lines to enable ssl for MQTT
    ssl:
        key_store: mqttserver.jks
        key_store_password: server_ks_password
        key_password: server_key_password
        key_store_type: JKS

You may also want to change **mqtt.bind_port** to 8883 which is recommended for MQTT over SSL servers.
The key_store Property must point to the **.jks** file location. **key_store_password** and **key_password** must be the same as were used in keystore generation.

**NOTE:** Tempus supports **.p12** keystores as well. if this is the case, set **key_store_type** value to ‘**PKCS12**’
After these values are set, launch or restart your tempus server.

***************
Client Examples
***************

See following resources:

* :doc:`deviceauth` for authentication options overview
* :doc:`tokenauth` for example of one-way SSL connection
* :doc:`certauth` for example of two-way SSL connection