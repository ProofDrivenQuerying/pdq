PDQ regression test library

Packages and classes for detecting regressions in the code.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
   
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies

Internal: runtime, planner 

External: jcommander-1.35
	
III. Installation

Under the top directory, type:

	mvn install
	
Two JARs will be built and placed in the project's "target/" directory.

	- pdq-regression-<version>.jar, contains the bytecode for the regression project
	  only, i.e. you need to make sure all dependencies are on the CLASSPATH to
	  run it.

	- pdq-regression-<version>.one-jar.jar, is fully self-contained, and can be
	  run directly.

To run the regression tests, type:

	java -jar /path/to/JAR/file --help
	
This will printout all required command line arguments.

Regression parameters can be passed through the command line, however, you 
may want to set those parameters in a separate file.
See pdq-regression.properties for an overview of all parameters that can be 
used.
In addition to those, planner- and runtime-specific parameters can be set in
specific files.
When specifying an input directory, the program will typically search 
recursively for directory containing the following files:
	- case.properties: parameters specific to a single execution
	- schema.xml: the input schema
	- query.xml: the input query
	- expected-plan.xml: this plan with which the output of the planner will be 
	  compared, or if testing the runtime, the plan whose result you which to 
	  compare with.

