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


# delete tempus services
kubectl delete service  cassandra-headless \
                        nifi \
                        tempus \
                        zk-cs \
                        zk-hs

# delete tempus stateful sets
kubectl delete statefulset  cassandra \
                            nifi \
                            tempus \
                            zk

# delete all tempus storage classes
kubectl delete storageclass tempus

# delete tempus pvcs
kubectl delete pvc  cassandra-commitlog-cassandra-0 \
                    cassandra-data-cassandra-0 \
                    nifi-content-repo-dir-nifi-0 \
                    nifi-db-repo-dir-nifi-0 \
                    nifi-flowfile-repo-dir-nifi-0 \
                    nifi-log-dir-nifi-0 \
                    nifi-provenance-repo-dir-nifi-0 \
                    zk-datadir-zk-0 \
                    zk-datadir-zk-1 \
                    zk-datadir-zk-2

# delete all tempus configmaps
kubectl delete configmap tempus-config

# delete all tempus pod disruption budgets
kubectl delete poddisruptionbudget  tempus-budget \
                                    zk-pdb
