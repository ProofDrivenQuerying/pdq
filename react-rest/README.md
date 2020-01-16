
  # PDQ REST API

  The PDQ REST API serves PDQ objects, plans, and run results via HTTP requests. 

  ## Requirements

   * Java 1.8 or higher
   * Maven 3.2 or higher

   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

  ## Dependencies

  Internal: (pdq-)common, (pdq-)gui, (pdq-)planner, (pdq-)reasoning

  External: springframework.boot-2.1.4, org.glassfish.jersey.inject-2.28, org.glassfish.jersey.bundles.repackaged-2.6
  
  **In order to be able to run the API, you will need to include the /demo/ and /services/ folders and all their contents
  in the project's base  directory.**

  ## Installing & running the REST API

  Under the top directory, type:

  	mvn install

  One JAR will be built and placed in the project's "target/" directory.

  	- pdq-rest-<version>.jar, contains the bytecode for the web service
  	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
  	  run it.

  To run the REST API, type:

  	java -jar /path/to/JAR/file

  Open http://localhost:8080/<myQuery>?id=0 to make an API call!
