#!/bin/bash

workingDir="${HOME}/hub-detect-ws"
sharedDir="${workingDir}/shared"
targetImageDir="${sharedDir}/target"
outputDir="${sharedDir}/output"

detectServiceName=hub-detect-ws
detectDeploymentName=${detectServiceName}

imageInspectorServiceName=hub-imageinspector-ws
imageInspectorDeploymentName=${imageInspectorServiceName}

nameSpace=${detectServiceName}
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

function waitForPodToStart() {
	waitForDeploymentName=$1
	newPodName=""
	
	echo "Pausing to give the new pod for ${waitForDeploymentName} time to start..."
	sleep 20
	newPodName=$(kubectl get pods --namespace ${nameSpace} | grep "${waitForDeploymentName}"  | tr -s " " | cut -d' ' -f1)
	echo "newPodName: ${newPodName}"

	podIsRunning=false
	counter=0
	while [[ $counter -lt 10 ]]; do
		echo the counter is $counter
		kubectl get pods --namespace ${nameSpace}
		newPodStatus=$(kubectl get pods --namespace ${nameSpace} | grep "${waitForDeploymentName}"  | tr -s " " | cut -d' ' -f3)
		echo "newPodStatus: ${newPodStatus}"
		if [ "${newPodStatus}" == "Running" ]; then
			echo "The new pod running container ${waitForDeploymentName} is ready"
			break
		else
			echo "The new pod is NOT ready"
		fi
		echo "Pausing to give the new pod time to start..."
		sleep 15
		counter=$((counter+1))
	done
	if [ "${newPodStatus}" != "Running" ]; then
		echo "The new pod for container ${waitForDeploymentName} never started!"
		exit -1
	fi
	echo "New Pod ${newPodName}, is running container ${waitForDeploymentName}"
}

mkdir -p ${targetImageDir}
mkdir ${outputDir}

chmod 777 ${workingDir}
chmod 777 ${sharedDir}
chmod 777 ${targetImageDir}
chmod 777 ${outputDir}

ensureKubeRunning

echo "--------------------------------------------------------------"
echo "Pulling/saving the target images (if they don't already exist)"
echo "--------------------------------------------------------------"
if [ ! -f "${targetImageDir}/alpine.tar" ] ; then
	docker pull "alpine:latest"
	docker save -o "${targetImageDir}/alpine.tar" "alpine:latest"
	chmod a+r "${targetImageDir}/alpine.tar"
fi

if [ ! -f "${targetImageDir}/fedora.tar" ] ; then
	docker pull "fedora:latest"
	docker save -o "${targetImageDir}/fedora.tar" "fedora:latest"
	chmod a+r "${targetImageDir}/fedora.tar"
fi

if [ ! -f "${targetImageDir}/debian.tar" ] ; then
	docker pull "debian:latest"
	docker save -o "${targetImageDir}/debian.tar" "debian:latest"
	chmod a+r "${targetImageDir}/debian.tar"
fi

echo "--------------------------------------------------------------"
echo "Pre-processing kube yaml files"
echo "--------------------------------------------------------------"
sed "s+@WORKINGDIR@+${workingDir}+g" deployment/kubernetes/kube-detect-service.yml > ${workingDir}/kube-detect-service.yml
sed "s+@WORKINGDIR@+${workingDir}+g" deployment/kubernetes/kube-imageinspector-service.yml > ${workingDir}/kube-imageinspector-service.yml

echo "--------------------------------------------------------------"
echo "Creating services and deployments"
echo "--------------------------------------------------------------"
kubectl create -f ${workingDir}/kube-imageinspector-service.yml
waitForPodToStart ${imageInspectorDeploymentName}

kubectl create -f ${workingDir}/kube-detect-service.yml
waitForPodToStart ${detectDeploymentName}
detectPodName=${newPodName}

echo "--------------------------------------------------------------"
echo "Getting detect pod name"
echo "--------------------------------------------------------------"
echo "The detect pod name is: ${detectPodName}"

echo "--------------------------------------------------------------"
echo "To use service to get BDIO for alpine"
echo "--------------------------------------------------------------"
clusterIp=$(minikube ip)
##servicePort=$(kubectl describe services hub-detect-ws --namespace ${nameSpace}|grep -v '^Type:'|grep NodePort|awk '{print $3}'|sed 's/\/TCP//')
servicePort=8083
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/alpine.tar"
echo "${cmd}"
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for fedora"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/fedora.tar"
echo "${cmd}"
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for debian"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/debian.tar"
echo "${cmd}"
