### Simple Account Management Service
- Login/Register system
- JWT Token Authentication
- Account System(TRY, USD, EUR)
- Send money to another account
- List incoming or outgoing money transfers
- Unit Tests(60) and Integration Tests(16). Total: 76 (Total Coverage: %98 Classes and %93 Methods and %83 Lines)
- Dockerized both backend and db containers

### Pre-Requirements
- Docker must  be installed to build (for integration tests and development)
- There should be mysql8 installed and run(considering that port is 3306). Create a database named mobilab and provide the valid username and password inside application.yml. No need to db migration, project will seed dummy datas
- **Please make sure 3306 and 8080 ports are free to use** (If not, they can changed in application.yml)

### Instructions For Production
```sh
>  mvn clean:install
This will create the jar file as "senocak-0.0.1-SNAPSHOT.jar" inside /target folder
> java -jar senocak-0.0.1-SNAPSHOT.jar
this will run the application with port 8080
```
or
```sh
> docker-compose up -d 
This will create the mysql container with database installed with port 3306
and will build the spring project and make it run with port 8080
```

### Instructions For Development
- Make sure the url in AppConstants (CURRENCY_TOKEN) is valid. Since Free api is used, the usage of the api will be fully expired somewhere.
- Change the base url for **jdbc** in application.yml from **db** to **localhost**
