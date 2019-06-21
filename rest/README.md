
  PDQ RESTful web service

  The PDQ RESTful web service employs a Spring Boot controller to direct GET requests to Java methods that extract
  useful information from PDQ modules and convert that information into JSON objects for consumption by the
  front-end.

  The source code is available for free for non-commercial use.
  See the LICENCE file for details.

  I. Requirements

   * Java 1.8 or higher
   * Maven 3.2 or higher

   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

  II. Dependencies

  Internal: (pdq-)common, (pdq-)qui, (pdq-)planner, (pdq-)reasoning

  External: springframework.boot-2.1.4

  III. Installing & running the RESTful web service

  Under the top directory, type:

  	mvn install

  One JAR will be built and placed in the project's "target/" directory.

  	- pdq-rest-<version>.jar, contains the bytecode for the web service
  	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
  	  run it.

  To run the RESTful web service, type:

  	java -jar /path/to/JAR/file

  Open http://localhost:8080/<myQuery>?id=0 to make an API call!

