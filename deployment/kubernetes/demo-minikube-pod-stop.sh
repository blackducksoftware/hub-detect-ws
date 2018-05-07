#!/bin/bash

deploymentName=hub-detect-ws
serviceName=${deploymentName}
nameSpace=${deploymentName}
configMapName=spring-app-config

echo "--------------------------------------------------------------"
echo "Deleting service, pod, configmap, namespace"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}" --namespace ${nameSpace}
kubectl delete service hub-imageinspector-ws --namespace ${nameSpace}
kubectl delete deployment "${deploymentName}" --namespace ${nameSpace}
kubectl delete deployment hub-imageinspector-ws --namespace ${nameSpace}
#
# Usually don't want to delete these
#
##kubectl delete configmap "${configMapName}" --namespace ${nameSpace}
##kubectl delete namespace "${nameSpace}"
