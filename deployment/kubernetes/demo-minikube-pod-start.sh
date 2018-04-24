#!/bin/bash

workingDir="${HOME}/hub-detect-ws"
sharedDir="${workingDir}/shared"
targetImageDir="${sharedDir}/target"
outputDir="${sharedDir}/output"

podName=hub-detect-ws
serviceName=${podName}
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

function waitForPodToStart() {
	requestedPodName=$1
	newPodName=""
	
	echo "Pausing to give the new pod for ${requestedPodName} time to start..."
	sleep 20
	newPodName=$(kubectl get pods --namespace ${nameSpace} | grep "${requestedPodName}"  | tr -s " " | cut -d' ' -f1)
	echo "newPodName: ${newPodName}"

	podIsRunning=false
	counter=0
	while [[ $counter -lt 10 ]]; do
		echo the counter is $counter
		kubectl get pods --namespace ${nameSpace}
		newPodStatus=$(kubectl get pods --namespace ${nameSpace} | grep "${requestedPodName}"  | tr -s " " | cut -d' ' -f3)
		echo "newPodStatus: ${newPodStatus}"
		if [ "${newPodStatus}" == "Running" ]; then
			echo "The new pod running container ${requestedPodName} is ready"
			break
		else
			echo "The new pod is NOT ready"
		fi
		echo "Pausing to give the new pod time to start..."
		sleep 15
		counter=$((counter+1))
	done
	if [ "${newPodStatus}" != "Running" ]; then
		echo "The new pod for container ${requestedPodName} never started!"
		exit -1
	fi
	echo "New Pod ${newPodName}, is running container ${requestedPodName}"
}

mkdir -p ${targetImageDir}
mkdir ${outputDir}

chmod 777 ${workingDir}
chmod 777 ${sharedDir}
chmod 777 ${targetImageDir}
chmod 777 ${outputDir}

ensureKubeRunning
rm -f "${targetImageDir}/alpine.tar"
rm -f "${targetImageDir}/fedora.tar"
rm -f "${targetImageDir}/debian.tar"

echo "--------------------------------------------------------------"
echo "Pulling/saving the target images"
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
echo "Pre-processing kube yaml file"
echo "--------------------------------------------------------------"
sed "s+@WORKINGDIR@+${workingDir}+g" deployment/kubernetes/kube-pod.yml > ${workingDir}/kube-pod.yml

echo "--------------------------------------------------------------"
echo "Creating service"
echo "--------------------------------------------------------------"
kubectl create -f deployment/kubernetes/kube-service.yml
echo "Pausing to give the hub-detect-ws service time to start..."
sleep 10

echo "--------------------------------------------------------------"
echo "Creating pod"
echo "--------------------------------------------------------------"
kubectl create -f ${workingDir}/kube-pod.yml
waitForPodToStart ${podName}


echo "--------------------------------------------------------------"
echo "To use service to get BDIO for alpine"
echo "--------------------------------------------------------------"
clusterIp=$(minikube ip)
##servicePort=$(kubectl describe services hub-detect-ws --namespace ${nameSpace}|grep -v '^Type:'|grep NodePort|awk '{print $3}'|sed 's/\/TCP//')
servicePort=8083
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/shared/target/alpine.tar"
echo "${cmd}"
######$cmd
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for fedora"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/shared/target/fedora.tar"
echo "${cmd}"
######$cmd
echo "--------------------------------------------------------------"
echo "To use service to get BDIO for debian"
echo "--------------------------------------------------------------"
cmd="curl -X POST -i http://${clusterIp}:${servicePort}/scaninspectimage?tarfile=/opt/blackduck/shared/target/debian.tar"
echo "${cmd}"
######$cmd
