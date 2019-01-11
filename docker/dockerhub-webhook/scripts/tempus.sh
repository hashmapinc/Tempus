#!/bin/bash

kubectl --kubeconfig /src/scripts/kubeconfig get pods -oname |grep tempus |xargs kubectl --kubeconfig /src/scripts/kubeconfig delete >> ./webhook_log

echo "last updated at: "$(date -u) >> ./webhook_log





