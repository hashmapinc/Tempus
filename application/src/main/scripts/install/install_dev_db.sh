#!/bin/bash
#
# Copyright © 2017-2018 Hashmap, Inc
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


BASE=${project.basedir}/target
CONF_FOLDER=${BASE}/conf
jarfile="${BASE}/tempus-${project.version}-boot.jar"
installDir=${BASE}/data
loadDemo=true


export JAVA_OPTS="$JAVA_OPTS -Dplatform=@pkg.platform@"
export LOADER_PATH=${BASE}/conf,${BASE}/extensions
export SQL_DATA_FOLDER=${SQL_DATA_FOLDER:-/tmp}


run_user=tempus

su -s /bin/sh -c "java -cp ${jarfile} $JAVA_OPTS -Dloader.main=com.hashmapinc.server.TempusInstallApplication \
                    -Dinstall.data_dir=${installDir} \
                    -Dinstall.load_demo=${loadDemo} \
                    -Dspring.jpa.hibernate.ddl-auto=none \
                    -Dinstall.upgrade=false \
                    -Dlogging.config=logback.xml \
                    org.springframework.boot.loader.PropertiesLauncher" "$run_user"

if [ $? -ne 0 ]; then
    echo "Tempus DB installation failed!"
else
    echo "Tempus DB installed successfully!"
fi

exit $?
