# Overview #
hub-detect-ws is container-based Web Service for scanning (via the file signature-based iScan) and inspecting (via the Linux package manager-based image inspector) Docker images.

This service is IN DEVELOPMENT / not ready for production use. Anything (including endpoint names) might change before it is released.

# Running in a Minikube Environment

Requirements: bash, minikube, java 8, curl, ports 8080, 8081, 8082, 8083. The directory ~/hub-detect-ws will be created.

If you start Minikube in advance, Minikube should be started with `--memory 8000` (or more). If Minikube is not running, it will be started automatically with `--memory 8000`. The provided scripts will create and use namespace hub-detect-ws, and dir ~/hub-detect-ws.

Step 1: Get the project from github:
```
git clone https://github.com/blackducksoftware/hub-detect-ws
cd hub-detect-ws
```

Step 2: Initialize the configmap and start the minikube dashboard:
```
deployment/kubernetes/mk-initconfig.sh
minikube dashboard
```

Step 3: In the minikube dashboard, in namespace hub-detect-ws, provide connection details to your Hub server by editing the values of properties blackduck.hub.url, blackduck.hub.username, and blackduck.hub.password in Config Map spring-app-config, file application.properties. The properties are only read on startup of the hub-detect-ws service. Any detect property set here will be passed to detect, with a few exceptions. Properties that you cannot change (your changes will be ignored):
* logging.level.com.blackducksoftware.integration (set detectws.logging.level instead)
* detect.source.path 
* detect.excluded.bom.tool.types
* detect.output.path

Step 4: Start the pod:
```
deployment/kubernetes/mk-services-start.sh
```

Step 5: Test the service. 

The following are examples of curl commands you can use to test the service:

```
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/alpine.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/fedora.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
curl -X POST -i http://$(minikube ip):8083/scaninspectimage?detect.docker.tar=/opt/blackduck/shared/target/debian.tar
curl -X GET  -i http://$(minikube ip):8083/ready # wait for a 200 response
```

From each POST to /scaninspectimage, you should get an HTTP 202 response indicating that the request was accepted. While the request is being processed, the /ready endpoint will return 503. When processing of that request has finished, a Scan and a BOM will appear on the Hub's Scans screen, and the /ready endpoint will return 200. 

To delete the pod and the service from the cluster:

```
deployment/kubernetes/mk-services-stop.sh
```

## Troubleshooting

To get the hub-detect-ws pod name, either:
1. See the output of the start script.
1. Run the following command:
```
detectPodName=$(kubectl get pods --namespace hub-detect-ws | grep "hub-detect-ws"  | tr -s " " | cut -d' ' -f1)
```


To get the log from the service:

```
kubectl logs --namespace hub-detect-ws ${detectPodName} -c hub-detect-ws
```

To get logs from the supporting services:

```
kubectl logs --namespace hub-detect-ws ${detectPodName} -c hub-imageinspector-ws-alpine
kubectl logs --namespace hub-detect-ws ${detectPodName} -c hub-imageinspector-ws-centos
kubectl logs --namespace hub-detect-ws ${detectPodName} -c hub-imageinspector-ws-ubuntu
```

# Build
TBD

# Where can I get the latest release? #
You can download the latest source from GitHub: https://github.com/blackducksoftware/hub-detect-ws. 

# Documentation #
hub-detect-ws is under development. There is no documentation yet other than this README file.

## Primary Endpoints ##

1. GET /ready # Make sure this endpoint returns 200 before calling /scaninspectimage
1. POST /scaninspectimage
  * Query parameters: Any detect property related to inspecting/scanning docker tarfiles.
  * Mandatory query param: detect.docker.tar=`<path to Docker image tarfile in the pod>`
  * Optional query params include:
    * detect.project.name=`<Hub project name>`
    * detect.project.version.name=`<Hub project version>`


## Other Endpoints ##

```
GET /trace # get history of http requests
GET /health # check the health of the service
GET /metrics # get Spring Boot-generated metrics in JSON format
GET /prometheus # get Prometheus-generated metrics in Prometheus format
GET /loggers # get list of loggers
POST /loggers/<logger> # Example: curl -i -X POST -H 'Content-Type: application/json' -d '{"configuredLevel": "TRACE"}' http://<IP>:8080/loggers/com.blackducksoftware
```

