PDQ Demo Application

PDQ's user interface.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.8 or higher
  
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies
 
Internal: runtime, planner 

External: jcommander-1.35, prefuse-beta, antlr4-4.3
	
III. Installing and running the demo.

Under the top directory, type:

	mvn install
	
Two JARs will be built and placed in the project's "target/" directory.

	- pdq-demo-<version>.jar, contains the bytecode for the planner
	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
	  run it.

	- pdq-demo-<version>.one-jar.jar, is fully self-contained, and can be
	  run directly.

To run the demo, type:

	java -jar /path/to/JAR/file
	
Option -h/--help will printout all required command line arguments.
