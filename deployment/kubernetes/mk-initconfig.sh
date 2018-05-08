#!/bin/bash

podName=hub-detect-ws
nameSpace=${podName}
minikubeMemory=8000

function ensureKubeRunning() {
	kubeRunning=$(minikube status | grep "minikube: Running" | wc -l)
	if [[ ${kubeRunning} -eq 0 ]]; then
		echo "Starting minikube"
		minikube start --memory ${minikubeMemory}
	else
		echo "--------------------------------------------------------------------"
		echo "minikube is already running"
		echo "Remember, minikube should be started with --memory ${minikubeMemory} (or more)"
		echo "--------------------------------------------------------------------"
	fi
	eval $(minikube docker-env)
}

ensureKubeRunning

echo "--------------------------------------------------------------"
echo "Creating namespace"
echo "--------------------------------------------------------------"
kubectl create -f deployment/kubernetes/kube-namespace.yml
sleep 5

echo "--------------------------------------------------------------"
echo "Creating configMap"
echo "--------------------------------------------------------------"
kubectl --namespace hub-detect-ws create configmap spring-app-config --from-file=deployment/kubernetes/application.properties
sleep 5

echo "Configure configmap, and then run demo-minikube-pod-start.sh"
