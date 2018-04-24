#!/bin/bash

podName=hub-detect-ws
serviceName=${podName}
nameSpace=${podName}
configMapName=spring-app-config

echo "--------------------------------------------------------------"
echo "Deleting service, pod, configmap, namespace"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}" --namespace ${nameSpace}
kubectl delete pod "${podName}" --namespace ${nameSpace}
kubectl delete configmap "${configMapName}" --namespace ${nameSpace}
kubectl delete namespace "${nameSpace}"
