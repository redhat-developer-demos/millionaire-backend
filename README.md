# millionaire-backend project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Security and authentification

In dev mode the security is disabled. 
In prod mode you will need a running Keycloak instance. 

You can just do : `docker run --name keycloak -e KEYCLOAK_USER=admin -e KEYCLOAK_PASSWORD=admin -p 8180:8080 jboss/keycloak` 

Then you access the Keycloak console `http://localhost:8180/auth` with `admin/admin` and you can import the realm available at the root of this repo `game-realm.json` and you will be all set, 2 users are available : `sebi/sebi` and `alex/alex` 


## Packaging and running the application

The application can be packaged using `./mvnw package`.
It produces the `millionaire-backend-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/millionaire-backend-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/millionaire-backend-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

## Deploy to OpenShift

https://www.youtube.com/watch?v=YxqWa6DKq_8
