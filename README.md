# Overview #
A container-based Web Service for scanning (via the file signature-based iScan) and inspecting (via the Linux package manager-based image inspector) Docker images.

This service is IN DEVELOPMENT / not ready for production use. Anything (including endpoint names) might change before it is released. Current state: The service only runs iScan on the target image tarfile; it does not run the image inspector yet.

# Quick Start in a Docker Environment #
Docker must be running.

```
git clone https://github.com/blackducksoftware/hub-detect-ws
cd hub-detect-ws
src/main/resources/demo-docker.sh
curl -X POST -i http://localhost:8080/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/alpine.tar

```
You should get an HTTP 202 response indicating that the request was accepted. When it's done, a Scan will appear on the Hub's Scans screen. To get the log:
```
docker logs hub-detect-ws
```

# Quick Start in a Minikube Environment
Minikube must be running, started with: minikube start --memory 8000. The script will create and use namespace hub-detect-ws, and dir ~/hub-detect-ws.

```
git clone https://github.com/blackducksoftware/hub-detect-ws
cd hub-detect-ws
src/main/resources/demo-minikube-pod-start.sh
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/alpine.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/fedora.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?tarfile=/opt/blackduck/shared/target/debian.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
```
You should get an HTTP 202 response indicating that the request was accepted. When it's done, a Scan will appear on the Hub's Scans screen. To get the logs:
```
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-detect-ws
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-alpine
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-centos
kubectl logs --namespace hub-detect-ws hub-detect-ws -c hub-imageinspector-ws-ubuntu
```

# Build #
TBD

# Where can I get the latest release? #
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-detect-ws. 

To try it in a Docker environment, you can use this bash script as a starting point: https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/demo-docker.sh.

Ty try it in a Kubernetes environment, you use these bash scripts as a starting point: https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/demo-minikube-start.sh, https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/demo-minikube-stop.sh. They depend on: https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/kube-deployment.yml, https://github.com/blackducksoftware/hub-detect-ws/blob/master/src/main/resources/kube-service.yml.

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

src/main/resources/demo-minikube-start.sh is a shell script that uses minikube to get a pod running, and then executes (and echo's) some curl commands to test the service.
src/main/resources/demo-minikube-stop.sh will delete the deployment and service that the start script creates.

Requirements: bash, minikube, java 8, curl, port 8080. It creates a ~/tmp/target dir.

The script will start a 1-container pod, and expose port 8080. It exposes a "scaninspectimage" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns HTTP status 200 if the request is succesfully received. (Once implemented:) At that point the service will start scanning and inspecting the image. Eventually it will upload the resulting BDIO files to the Hub.

## Configuring the service ##

No configuration is required yet (because the real work is mocked).

## Trying hub-detect-ws in a Docker environment ##

src/main/resources/demo-docker.sh is a shell script that uses docker to get a container running, and then suggests (echo's) some curl commands to test the service.

Requirements: bash, docker, java 8, curl, port 8080, and a /tmp dir.

The script will start a containerized web service running on port 8080. It exposes a "scaninspectimage" endpoint that takes a path to a Docker image tarfile (the output of a "docker save" command), and returns HTTP status 200 if the request is succesfully received. (Once implemented:) At that point the service will start scanning and inspecting the image. Eventually it will upload the resulting BDIO files to the Hub.

## Other Endpoints ##

```
GET /trace # get history of http requests
GET /health # check the health of the service
GET /metrics # get Spring Boot-generated metrics in JSON format
GET /prometheus # get Prometheus-generated metrics in Prometheus format
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.blackducksoftware
```

