#!/bin/bash

podName=hub-detect-ws
serviceName=hub-detect-ws

echo "--------------------------------------------------------------"
echo "Deleting deployment, service"
echo "--------------------------------------------------------------"
kubectl delete service "${serviceName}"
kubectl delete pod "${podName}"
