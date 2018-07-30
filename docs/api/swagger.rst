#############
Admin REST API
##############

*******
Swagger
*******

Tempus Cloud REST API can be explored using Swagger UI.

Once you will install the Tempus Cloud server you can open the Swagger Tempus REST API UI using the following URL:

.. code-block:: bash

    http://YOUR_HOST:PORT/swagger-ui.html

******************
Generate JWT Token
******************

In order to get a JWT token, you need to execute the following request:

In case of local installation:

* replace $TEMPUS_URL with 127.0.0.1:8080

In case of remote installation:

* replace $TEMPUS_URL with <host>:<port> (replacing host and port with the respective host and port of the machine running Tempus).

.. tabs::

    .. tab:: get-token.sh

        .. code-block:: bash

            curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{"username":"demo@hashmapinc.com", "password":"tenant"}' 'http://TEMPUS_URL/api/auth/login'

    .. tab:: response.json

        .. code-block:: bash

            {"token":"$YOUR_JWT_TOKEN", "refreshToken":"$YOUR_JWT_REFRESH_TOKEN"}

*******************************
Set Api Key Authorization Value
*******************************

Now, in the Tempus REST API URL click 'Authorize' button located in the page header, then in the 'Api key authorization' 'value' text field  enter “Bearer $YOUR_JWT_TOKEN”