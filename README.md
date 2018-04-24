# Overview #
hub-detect-ws is container-based Web Service for scanning (via the file signature-based iScan) and inspecting (via the Linux package manager-based image inspector) Docker images.

This service is IN DEVELOPMENT / not ready for production use. Anything (including endpoint names) might change before it is released.

# Quick Start in a Minikube Environment

Requirements: bash, minikube, java 8, curl, port 8080, 8081, 8082, 8083. The directory ~/hub-detect-ws will be created.

If you start Minikube in advance, Minikube should be started with `--memory 8000` (or more). If Minikube is not running, it will be started automatically with `--memory 8000`. The provided scripts will create and use namespace hub-detect-ws, and dir ~/hub-detect-ws.

Step 1: Get the project from github:
```
git clone https://github.com/blackducksoftware/hub-detect-ws
cd hub-detect-ws
```

Step 2: Initialize the configmap and start the minikube dashboard:
```
deployment/kubernetes/demo-minikube-pod-initconfig.sh
minikube dashboard
```

Step 3: In the minikube dashboard, in namespace hub-detect-ws, provide connection details to your Hub server by editing the values of hub.url, hub.username, and hub.password in Config Map spring-app-config, file application.properties.

Step 4: Start the pod:
```
deployment/kubernetes/demo-minikube-pod-start.sh
```

Step 5: Test the service. 

The following are examples of curl commands you can use to test the service:

```
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/alpine.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/fedora.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/debian.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
```

To each POST to /scaninspectimage, you should get an HTTP 202 response indicating that the request was accepted. While the request is being processed, the /ready endpoint will return 503. When processing of that request has finished, a Scan and a BOM will appear on the Hub's Scans screen, and the /ready endpoint will return 200. 

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

To delete the hub-detect-ws namespace from the cluster:

```
deployment/kubernetes/demo-minikube-pod-stop.sh
```

# Build #
TBD

# Where can I get the latest release? #
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-detect-ws. 

# Documentation #
hub-detect-ws is under development. You can use the provided scripts to try a pre-release version in either a Kubernetes or a Docker environment.

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

## Other Endpoints ##

```
GET /trace # get history of http requests
GET /health # check the health of the service
GET /metrics # get Spring Boot-generated metrics in JSON format
GET /prometheus # get Prometheus-generated metrics in Prometheus format
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.blackducksoftware
```

