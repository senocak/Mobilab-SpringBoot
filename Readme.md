## Simple Account Management Service

Written with SpringBoot

### Features
- Login/Register system
- JWT Token Authentication
- Account System(TRY, USD, EUR)
- Send money to another account
- List incoming or outgoing money transfers

### Pre-Requirements
> Docker must  be installed to build (for integration tests)
> There should be mysql8 installed and run. Create a database named mobilab

### Installation
```sh
>  mvn clean:install
This will create the jar file like senocak-0.0.1-SNAPSHOT.jar
> java -jar senocak-0.0.1-SNAPSHOT.jar
this will run the application with port 8080. Please make sure port is free to use
or
> docker-compose up -d 
This will create the mysql container with database installed with port 3306
and will build the spring project and make it run with port 8080 
```