# Riot take home assignment

The original [README file](./ORIGINAL.md) 

### Requirements

- Java 25
- Maven (can use `./mvnw`)

### Build Instruction

Run `$ mvn clean package` to build.  

### To run

You will need to specify a secret key for the HMAC algorithm.   
Can either use the environment variable `HMAC_KEY=<my-secret-key>` or   
a java system property, by adding the following argument on the command line `-Dhmac.key=<my-secret-key>`


Then run: `$ java [options...] -jar target/take-home-assignment-1.jar`
