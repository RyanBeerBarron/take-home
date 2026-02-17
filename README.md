# Riot take home assignment

The original [README file](./ORIGINAL.md) 

The assignment was fun to do. I had never tried to semantically hash/sign any json payload.
I had to read about JSON canonical form. It can quickly be a complicated topic.
I mainly took care of sorting the properties inside json objects and normalizing numbers.

I'm not sure about the string normalizing. Unicode has it's own normalizing forms (NFC, NFD, NFKC and NFKD).
Should these two JSONs produce the same hash/sign ?   
*Both have a string containing 'é' but one uses a single codepoint: U+00E9, and the other contain two codepoints ('e' + '´'): U+0065 with the combining character U+0301*
```json
{
    "field": "é"
}
{
    "field": "é"
}
```

I thought handling string normalization was out of scope for the take home.

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

Then run: `$ java [options...] -jar target/app.jar`

There is a formatter in the project, to use it, run: `$ ./mvnw spotless:apply`

### Building using Docker

This comes with Dockerfile to build and run the project without installing the dependencies on your machine.

Run: `$ docker build -t app .` to build the image.  
And: `$ docker run -p <localport>:8000 -e HMAC_KEY=<my-secret-key> app` to start the server.  
