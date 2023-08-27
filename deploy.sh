#!/bin/bash
# Delete deployments and services on k8s
kubectl scale deployment veridionds-deployment --replicas=0

kubectl delete deployment veridionds-deployment
kubectl delete service veridionds-service

kubectl delete deployment elasticsearch-deployment
kubectl delete service elasticsearch-service

kubectl delete deployment rabbitmq-deployment
kubectl delete service rabbitmq-service

kubectl delete deployment redis-deployment
kubectl delete service redis-service

# New deploy - Step 1
docker build -t veridionds:3.1.2 .
kubectl apply -f elasticsearch-deployment.yaml
kubectl apply -f elasticsearch-service.yaml
kubectl apply -f rabbitmq-deployment.yaml
kubectl apply -f rabbitmq-service.yaml
kubectl apply -f redis-deployment.yaml
kubectl apply -f redis-service.yaml

# Wait for a while before deploying the app
echo "Waiting for 60 seconds before deploying the app..."
sleep 60

# New deploy - Step 2
kubectl apply -f veridionds-deployment.yaml
kubectl apply -f veridionds-service.yaml