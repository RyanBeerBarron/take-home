# Riot take home assignment

The original [README file](./ORIGINAL.md) 

### Requirements

- Java 25
- Maven (can use `./mvnw`)
**OR**
- Docker
 
### Bullding/Running locally

Run `$ mvn clean package` to build.

To run the code, you will need to specify a secret key for the HMAC algorithm.   
Can either use the environment variable `HMAC_KEY=<my-secret-key>` or   
a java system property, by adding the following argument on the command line `-Dhmac.key=<my-secret-key>`

Then run: `$ java [options...] -jar target/take-home-assignment-1.jar`

### Building using Docker

This comes with Dockerfile to build and run the project without installing the dependencies on your machine.

Run: `$ docker build -t app .` to build the image.  
And: `$ docker run -p <localport>:8000 -e HMAC_KEY=<my-secret-key> app` to start the server.  
