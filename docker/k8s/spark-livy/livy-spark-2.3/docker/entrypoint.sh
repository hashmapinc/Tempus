#!/bin/bash
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


set -e


if [ "$K8S_API_HOST" ]
then
   sed -i -e "s~\${k8s_api_host}~$K8S_API_HOST~" \
        /opt/livy/conf/livy.conf
fi

if [ "$AWS_ACCESS_KEY" ] && [ "$AWS_SECRET_KEY" ]
then
   sed -i -e "s~\${aws_access_key}~$AWS_ACCESS_KEY~" \
       -e "s~\${aws_secret_key}~$AWS_SECRET_KEY~" \
       /opt/spark/conf/spark-defaults.conf
fi

if [ "$SPARK_KUBERNETES_IMAGE" ]
then
   sed -i -e "s~\${spark_kubernetes_image}~$SPARK_KUBERNETES_IMAGE~" \
       /opt/spark/conf/spark-defaults.conf
fi

if [ "$AZURE_STORAGE_ACCOUNT" ] && [ "$AZURE_STORAGE_ACCESS_KEY" ]
then
    sed -i -e "s~\${azure_storage_account}~$AZURE_STORAGE_ACCOUNT~" \
        -e "s~\${azure_storage_access_key}~$AZURE_STORAGE_ACCESS_KEY~" \
        /opt/spark/conf/spark-defaults.conf
fi

exec "$@"
