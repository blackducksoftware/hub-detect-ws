#!/bin/bash

deploymentName=hub-detect-ws
serviceName=${deploymentName}
nameSpace=${deploymentName}
configMapName=spring-app-config

echo "--------------------------------------------------------------"
echo "Deleting service, pod, configmap, namespace"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}" --namespace ${nameSpace}
kubectl delete deployment "${deploymentName}" --namespace ${nameSpace}
##kubectl delete configmap "${configMapName}" --namespace ${nameSpace}
##kubectl delete namespace "${nameSpace}"
