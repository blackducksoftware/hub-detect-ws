# Overview #
A container-based Web Service for scanning (via the file signature-based iScan) and inspecting (via the Linux package manager-based image inspector) Docker images.

This service is IN DEVELOPMENT / not ready for production use. Anything (including endpoint names) might change before it is released. Current state: The service only runs iScan on the target image tarfile; it does not run the image inspector yet.

# Quick Start in a Minikube Environment
Minikube should be started with --memory 8000 (or more). If minikube is not running, it will be started automatically with --memory 8000. The scripts will create and use namespace hub-detect-ws, and dir ~/hub-detect-ws.

Get the project from github:
```
git clone https://github.com/blackducksoftware/hub-detect-ws
cd hub-detect-ws
```

Initialize the configmap and start the minikube dashboard:
```
deployment/kubernetes/demo-minikube-pod-initconfig.sh
minikube dashboard
```

In the minikube dashboard, in namespace hub-detect-ws, edit the values of hub.url, hub.username, and hub.password in Config Map spring-app-config, file application.properties.

Start the pod:
```
deployment/kubernetes/demo-minikube-pod-start.sh
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/alpine.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/fedora.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/debian.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
```
You should get an HTTP 202 response indicating that the request was accepted. While the request is being processed, the ready endpoint will return 503. When processing of that request has finished, a Scan and a BOM will appear on the Hub's Scans screen, and the ready endpoint will return 200. 

To get the log from the service:
```
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-detect-ws
```
To get logs from the supporting services:
```
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-alpine
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-centos
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-ubuntu
```
Subsequent runs on the same tarfile will hit a permission error writing the container filesystem output file. Executing "rm -rf ~/hub-detect-ws/shared/output/*.gz" between runs will avoid this.

# Build #
TBD

# Where can I get the latest release? #
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-detect-ws. 

Ty try it in a Kubernetes environment, you use these bash scripts as a starting point: https://github.com/blackducksoftware/hub-detect-ws/blob/master/deployment/kubernetes/demo-minikube-initconfig.sh, https://github.com/blackducksoftware/hub-detect-ws/blob/master/deployment/kubernetes/demo-minikube-start.sh, https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/demo-minikube-stop.sh.

# Documentation #
hub-detect-ws is under development. You can use the provided bash scripts to try a pre-release version in either a Kubernetes or a Docker environment.

You only need files in the src/main/resources directory (and images that they download from Docker Hub), but it may be easiest to clone the whole repo. For the relative paths to be correct, execute the scripts (src/main/resources/demo-*.sh) from the top level directory (the one that contains build.gradle). Whichever script you use, you'll want to read the script to understand what it's doing.

## Usage ##

This application can only handle one /scaninspectimage request at a time. Before you call /scaninspectimage, call /ready and make sure you get HTTP status 200 (indicating that the service is ready for another /scaninspectimage request). An HTTP status of 503 indicates that the service is busy processing a request.

/scaninspectimage (when successful) will return HTTP status 202, indicating that the request was accepted. Sometime later detect will finish and upload the results to the Hub in the given Hub project name/version.

## Primary Endpoints ##

GET /ready # Make sure this endpoint returns 200 before calling /scaninspectimage
POST /scaninspectimage
* Mandatory query param: tarfile=`<path to Docker image tarfile>`
* Optional query params:
  * hubprojectname=`<Hub project name>`
  * hubprojectversion=`<Hub project version>`
  * codelocationprefix=`<Hub CodeLocation name prefix>` # currently ignored
  * cleanup=`<cleanup working dirs when done: true or false; default: true>` # currently ignored

## Trying hub-detect-ws in a Kubernetes (minikube) environment ##

deployment/kubernetes/demo-minikube-pod-initconfig.sh and deployment/kubernetes/demo-minikube-pod-start.sh are shell scripts that uses minikube to get a pod running. You can then use curl commands to test the service.
deployment/kubernetes/demo-minikube-pod-stop.sh will delete the hub-detect-ws namespace from the cluster.

Once the pod is running you can communicate with it via port 8083. The demo-minikube-pod-start.sh script prints examples of curl commands you can use to test the service. The service exposes a /scaninspectimage endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns HTTP status 202 if the request is succesfully received. At that point the service will start scanning and inspecting the image. Eventually it will upload the resulting BDIO files to the Hub. You can use the /ready endpoint to determine when the service is ready to accept another request. If /ready returns 503, it is still busy processing the last request. If it returns 200, the service is ready for another request.

## Other Endpoints ##

Requirements: bash, minikube, java 8, curl, port 8080. It creates a ~/hub-detect-ws dir.

The script will start a 1-container pod, and expose port 8083. It exposes a "scaninspectimage" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns HTTP status 200 if the request is succesfully received. (Once implemented:) At that point the service will start scanning and inspecting the image. Eventually it will upload the resulting BDIO files to the Hub.

## Configuring the service ##

You can configure the service by changing the values in the spring-app-config Config Map.

```
GET /trace # get history of http requests
GET /health # check the health of the service
GET /metrics # get Spring Boot-generated metrics in JSON format
GET /prometheus # get Prometheus-generated metrics in Prometheus format
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.blackducksoftware
```

