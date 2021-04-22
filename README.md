# gutenberg-singapore
I present here a microservice-oriented application that uses some basic Kubernetes features including Ingress. It consists of a collection of separate servers all running in Kubernetes pods. MongoDB is used as a NoSQL database and also runs in a pod. Moreover all backend servers use reactive connection to MongoDB and Spring WebFlux rather than Spring RESTful. All backend servers are tested using JUnit5 test classes.

Here are the prerequisites for running the complete application:

A recent Minikube version running in Virtualbox. (I used 1.18.0)
A recent Apache Maven version installed (I used 3.6.3)

In addition I used Spring Tool Suite for developing this demo but it is not required for running the application.

Here is the list of all 12 containers:

Server            | Image                     | Port         | Function             | Database connection
---------------   | ------------------------- | ------------ | -------------------- | -------------------
books-mongodb     | mongo                     | 27017        | Schemaless database  |
book-service      | gutenberg/book-server     | 8081         | Book requests        | booksonline
review-service    | gutenberg/review-server   | 8082         | Review requests      | booksonline
order-service     | gutenberg/order-server    | 8083         | Order requests       | booksonline
user-service      | gutenberg/user-server     | 8084         | User requests        | booksonline
frontend-service  | gutenberg/frontend-server | 8080         | frontend             | None

A Kubernetes persistence volume (PV) and persistence volume claim (PVC) are used for persistence.

An Ingress is used to allow a direct connection to the application through a virtual host named minikube.gutenberg.

Here are the steps to run the application:

# 1. Images creation

Here we use the docker support of spring-boot. Moreover all images are created in minikube context, this avoids pushing the images to an external repository. To start minikube run this script:

```
#!/bin/bash
# file name createClusterSingapore 
unset KUBECONFIG

minikube start -p singapore \
--memory=10240 \
--cpus=4 \
--disk-size=30g \
--kubernetes-version=v1.20.2 \
--vm-driver=virtualbox

minikube addons enable ingress -p singapore
minikube addons enable metrics-server -p singapore
```

Create a new namespace named singapore and set it to the context:
```
kubectl create namespace singapore
kubectl config set-context $(kubectl config current-context) --namespace=singapor
```

Create a configmap for mongodb database:

```
#!/bin/bash
# file name createMongoConfigmap

kubectl config set-context $(kubectl config current-context) --namespace=singapore

kubectl create configmap mongo-initdb --from-file=gutenberg.js

```

Create a secret by running this script:
```
#!/bin/bash
# file name createMongodbServerCredentials
kubectl config set-context $(kubectl config current-context) --namespace=singapore

kubectl create secret generic mongodb-server-credentials --from-literal=MONGO_INITDB_ROOT_USERNAME=spring --from-literal=MONGO_INITDB_ROOT_PASSWORD=passwoord1234 --save-config
```

Then create a pod that runs a mongodb database by running this command in folder kubernetes:

```
kubectl apply -f deploy-configmap-nopersist.yml
```
Here is the manifest deploy-configmap-nopersist.yml
```
# file name mongodb-deploy-configmap-nopersist.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: mongo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongo
  template:
    metadata:
      labels:
        app: mongo
    spec:    
      containers:
      - image: mongo
        name: mongo
        envFrom:
        - secretRef:
            name: mongodb-server-credentials
        ports:
        - containerPort: 27017
        volumeMounts:
        - name: mongo-initdb
          mountPath: /docker-entrypoint-initdb.d
      volumes:
      - name: mongo-initdb
        configMap:
          name: mongo-initdb  
---         
apiVersion: v1
kind: Service
metadata:
  name: mongodb
spec:
  selector:
    app: mongo
  ports:
    - port: 27017   
```

Once the pod is OK go to folder kubernetes and run the script buildSpring:
```
#!/bin/bash

# should be in kubernetes folder

for server in 'book-server' 'review-server' 'order-server' 'user-server' 'frontend-server';
do
  # kill port-forward if running
  toto=`ps aux | grep 27017`
  a=($toto)
  echo ${a[1]} 
  kill -9 ${a[1]}

  # kill and restart pod, wait for pod OK
  kubectl delete po --all
  kubectl wait --timeout=600s --for=condition=ready pod --all

  # restart port-forward
  ./portForward &

  cd ../$server
  pwd
  ./buildSingapore

  cd ../kubernetes
done;
```

This script creates the 5 Spring Boot images needed to run the application. Kill the MongoDB pod with this command:
```
kubectl delete all --all
```
# 2. Persistence volume creation

In folder kubernetes run the commands:

```
kubectl apply -f mongodb-pv-hostpath.yml
kubectl apply -f mongodb-pvc.yml
```

Here are the manifests:

```
# file name mongodb-pv-hostpath.yml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: mongodb-pv
spec:
  capacity: 
    storage: 1Gi
  accessModes:
    - ReadWriteOnce
    - ReadOnlyMany
  persistentVolumeReclaimPolicy: Recycle
  hostPath:
    path: /tmp/mongodb
```

```
# file name mongodb-pvc.yml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: mongodb-pvc
spec:
  resources:
    requests: 
      storage: 1Gi
  accessModes:
  - ReadWriteOnce
  storageClassName: ""   
```

# 3. Creating the configmaps

Run the scripts:
```
createConfigmapBook
createConfigmapReview
createConfigmapOrder
createConfigmapUser
createConfigmapFrontend
```

# 4. Prepopulating the PV

Run the kubectl command:

```
kubectl apply -f mongodb-deploy-configmap.yml
```
This creates a new pod running a MongoDB database, this time with a PV. When the pod is OK kill it:
```
kubectl delete all --all
```
 
This leaves the PV prepopulated.
 

# 5. Running the application

To start the application go to kubernetes folder and run the command:
```
kubectl apply -k services/overlays/dev
```
When all pods are OK run this command to find minikube IP. The response will be something like:

```
minikube -p singapore ip
192.168.99.115
```

Add this line to the file /etc/hosts:
```
192.168.99.115 minikube.singapore
```

Then hit the browser on minikube.singapore to connect to the frontend. A username and password are required. Here are the prepopulated users:

Username | Password
-------- | --------- 
Carol    | s1a2t3o4r 
Albert   | a5r6e7p8o
Werner   | t4e3n2e1t
Alice    | o8p7e6r5a
Richard  | r1o2t3a4s
Sator    | sator1234 
Arepo    | arepo1234
Tenet    | tenet1234
Opera    | opera1234
Rotas    | rotas1234


To stop the application run the kubectl command:

```
kubectl delete all --all
```

# 6. Accessing MongoDB container
To access the MongoDB container run the command:
```
$ kubectl get po
mongo-6d8d94b7d5-2tp5m      1/1     Running   0          11m

```
Then run the command:

```
kubectl exec -it mongo-6d8d94b7d5-2tp5m -- /bin/bash
```
Then in the pod shell run the command:

```
mongo -u spring -p
```
Enter the password  `passwoord1234`
and then for example to display orders collection:

```
use booksonline
db.orders.find().pretty()
```

# 7. Screen snapshots

Here are some screen snapshots that can be seen by running this application:

Welcome page:
![alt text](images/welcome.png "Welcome page")

Book page:
![alt text](images/book.png "Book page")

Cart page:
![alt text](images/cart.png "Cart page")

Checkout page:
![alt text](images/checkout.png "Checkout page")

Payment page:
![alt text](images/checkoutSuccess.png "Payment page")

To stop the application run this command:

```
kubectl delete all --all
```

Cachan, April 19 2021
 
Dominique Ubersfeld
