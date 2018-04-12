#!/bin/bash

podName=hub-detect-ws
serviceName=${podName}
nameSpace=${podName}

echo "--------------------------------------------------------------"
echo "Deleting service, pod, namespace"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}" --namespace ${nameSpace}
kubectl delete pod "${podName}" --namespace ${nameSpace}
kubectl delete namespace "${nameSpace}"
