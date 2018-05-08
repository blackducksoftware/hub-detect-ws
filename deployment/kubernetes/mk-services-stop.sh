#!/bin/bash

detectDeploymentName=hub-detect-ws
detectServiceName=${detectDeploymentName}

imageInspectorDeploymentName=hub-imageinspector-ws
imageInspectorServiceName=${imageInspectorDeploymentName}

nameSpace=${detectServiceName}
configMapName=spring-app-config

echo "--------------------------------------------------------------"
echo "Deleting service, pod, configmap, namespace"
echo "--------------------------------------------------------------"
kubectl delete service "${detectServiceName}" --namespace ${nameSpace}
kubectl delete service "${imageInspectorServiceName}" --namespace ${nameSpace}
kubectl delete deployment "${detectDeploymentName}" --namespace ${nameSpace}
kubectl delete deployment "${imageInspectorDeploymentName}" --namespace ${nameSpace}
#
# Usually don't want to delete these
#
##kubectl delete configmap "${configMapName}" --namespace ${nameSpace}
##kubectl delete namespace "${nameSpace}"
