#!/bin/bash

targetImageDir=~/tmp/shared/target

deploymentName=hub-detect-ws
serviceName=hub-detect-ws

function ensureKubeRunning() {
	kubeRunning=$(minikube status | grep "minikube: Running" | wc -l)
	if [[ ${kubeRunning} -eq 0 ]]; then
		echo "Starting minikube"
		minikube start
	else
		echo "minikube is already running"
	fi
	eval $(minikube docker-env)
}

function waitForPodToStart() {
	newContainerName=$1
	newPodName=""
	
	echo "Pausing to give the new pod for ${newContainerName} time to start..."
	sleep 15
	newPodName=$(kubectl get pods | grep "${newContainerName}"  | tr -s " " | cut -d' ' -f1)
	echo "newPodName: ${newPodName}"

	podIsRunning=false
	counter=0
	while [[ $counter -lt 10 ]]; do
		echo the counter is $counter
		kubectl get pods
		newPodStatus=$(kubectl get pods | grep "${newContainerName}"  | tr -s " " | cut -d' ' -f3)
		echo "newPodStatus: ${newPodStatus}"
		if [ "${newPodStatus}" == "Running" ]; then
			echo "The new pod running container ${newContainerName} is ready"
			break
		else
			echo "The new pod is NOT ready"
		fi
		echo "Pausing to give the new pod time to start..."
		sleep 10
		counter=$((counter+1))
	done
	if [ "${newPodStatus}" != "Running" ]; then
		echo "The new pod for container ${newContainerName} never started!"
		exit -1
	fi
	echo "New Pod ${newPodName}, is running container ${newContainerName}"
}

ensureKubeRunning
mkdir -p ${targetImageDir}
#rm -f "${targetImageDir}/alpine.tar"
#rm -f "${targetImageDir}/fedora.tar"
#rm -f "${targetImageDir}/debian.tar"

#echo "--------------------------------------------------------------"
#echo "Pulling/saving the target images"
#echo "--------------------------------------------------------------"
#docker pull "alpine:latest"
#docker save -o "${targetImageDir}/alpine.tar" "alpine:latest"
#chmod a+r "${targetImageDir}/alpine.tar"

#docker pull "fedora:latest"
#docker save -o "${targetImageDir}/fedora.tar" "fedora:latest"
#chmod a+r "${targetImageDir}/fedora.tar"

#docker pull "debian:latest"
#docker save -o "${targetImageDir}/debian.tar" "debian:latest"
#chmod a+r "${targetImageDir}/debian.tar"

echo "--------------------------------------------------------------"
echo "Creating service"
echo "--------------------------------------------------------------"
kubectl create -f src/main/resources/kube-service.yml
echo "Pausing to give the hub-detect-ws service time to start..."
sleep 10

echo "--------------------------------------------------------------"
echo "Creating deployment"
echo "--------------------------------------------------------------"
kubectl create -f src/main/resources/kube-deployment.yml
waitForPodToStart ${deploymentName}


echo "--------------------------------------------------------------"
echo "To use service to get BDIO for alpine"
echo "--------------------------------------------------------------"
clusterIp=$(minikube ip)
##servicePort=$(kubectl describe services hub-detect-ws|grep -v '^Type:'|grep NodePort|awk '{print $3}'|sed 's/\/TCP//')
servicePort=8083
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/alpine.tar"
echo "${cmd}"
######$cmd
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for fedora"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/fedora.tar"
echo "${cmd}"
######$cmd
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for debian"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/debian.tar"
echo "${cmd}"
######$cmd
