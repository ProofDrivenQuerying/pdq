
  # PDQ RESTful web service

  The PDQ RESTful web service converts important PDQ objects, computations, and
  results into JSON objects and hosts them via a REST API.

  The source code is available for free for non-commercial use.
  See the LICENCE file for details.

  ## Requirements

   * Java 1.8 or higher
   * Maven 3.2 or higher

   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

  ## Dependencies

  Internal: (pdq-)common, (pdq-)gui, (pdq-)planner, (pdq-)reasoning

  External: springframework.boot-2.1.4, org.glassfish.jersey.inject-2.28, org.glassfish.jersey.bundles.repackaged-2.6

  ## Installing & running the RESTful web service

  Under the top directory, type:

  	mvn install

  One JAR will be built and placed in the project's "target/" directory.

  	- pdq-rest-<version>.jar, contains the bytecode for the web service
  	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
  	  run it.

  To run the RESTful web service, type:

  	java -jar /path/to/JAR/file

  Open http://localhost:8080/<myQuery>?id=0 to make an API call!
