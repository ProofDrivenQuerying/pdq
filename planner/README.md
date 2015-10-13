PDQ Planner Library

The planner library features classes and algorithm for planning purposes. Among
other, the project currently hosts two families of planning algorithms, named
linear and DAG planners respectively.
This projects also contains reasoning and cost estimation-related code, although
these may be moved to a separate project later on.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies
 
Internal: (pdq-)common
External: derby-10.10, mysql-connector-5.1.30
	
III. Installing & running the planner

Under the top directory, type:

	mvn install
	
Two JARs will be built and placed in the project's "target/" directory.

	- pdq-planner-<version>.jar, contains the bytecode for the planner
	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
	  run it.

	- pdq-planner-<version>.one-jar.jar, is fully self-contained, and can be
	  run directly.

To run the planner, type:

	java -jar /path/to/JAR/file --help
	
This will printout all required command line arguments.

Most planner parameters can be passed through the command line, however, you 
may want to set those parameters in a separate file.
See pdq-planner.properties for an overview of all parameters that can be used.

Example:
	mvn install
	java -jar target/pdq-planner-<version>.one-jar.jar \
			-c examples/example_01/case.properties     \
			-s examples/example_01/schema.xml          \
			-q examples/example_01/query.xml           \
			-v
