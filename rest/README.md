# rest
A higher level of abstraction of the PDQ application for use with Spring Boot's RESTful web service for Java backend

for an example visit: https://spring.io/guides/gs/rest-service/

## to install
* you'll need JDK 1.8 or later
* maven 3.2+
  * `brew install maven`

* install dependencies:
  * `mvn install`
  * `mvn install:install-file -Dfile=<path-to-common.jar>`
  * `mvn install:install-file -Dfile=<path-to-gui.jar>`
  * `mvn install:install-file -Dfile=<path-to-planner.jar>`
  * `mvn install:install-file -Dfile=<path-to-reasoning.jar>`



## to run
* run jar:
  `java -jar target/fake_pdq-0.1.0.jar`
