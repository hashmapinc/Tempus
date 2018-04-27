#!/bin/bash

# deploy common
kubectl create -f common.yaml

# deploy zookeeper
kubectl create -f zookeeper.yaml

# deploy cassandra
kubectl create -f cassandra.yaml 

# deploy nifi
kubectl create -f nifi.yaml

# deploy thingsboard
kubectl create -f tempus.yaml
