#!/bin/bash

docker pull blackducksoftware/hub-detect-ws:0.1.0-SNAPSHOT

mkdir /tmp/detectservice
docker pull alpine:latest
docker save -o /tmp/detectservice/alpine.tar alpine:latest

docker pull fedora:latest # will run on centos
docker save -o /tmp/detectservice/fedora.tar fedora:latest

docker pull debian:latest # will run on ubuntu
docker save -o /tmp/detectservice/debian.tar debian:latest

echo Starting detect service container...
docker run -v /tmp/detectservice:/opt/blackduck/hub-detect-ws/target -d --name hub-detect-ws -p 8080:8080 blackducksoftware/hub-detect-ws:0.1.0-SNAPSHOT

echo "Suggested tests:"
echo curl -X POST -i http://localhost:8080/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/alpine.tar
echo curl -X POST -i http://localhost:8080/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/fedora.tar
echo curl -X POST -i http://localhost:8080/scaninspectimage?tarfile=/opt/blackduck/hub-detect-ws/target/debian.tar
