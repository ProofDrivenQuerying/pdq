PDQ Server Library

The server library features packages and classes for running PDQ as a 
standalone server.

This can be useful for instance to provide optimization services externally to
third party application. Each third party application support is implemented
into a separate module. Currently, the server feature a module to interact
with the LogicBlox database server.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.7 or higher
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. Dependencies

Internal: planner 

External: jcommander-1.35, protobuf-java-2.5.0, logicblox proprietary libraries
	
III. Installation

Under the top directory, type:

	mvn install
	
The JAR will be builder and place in each project's "target/" directory.

To run the server:

	java -jar /path/to/jar/file <command> [module to start]

where command can be any of 'start', 'stop' or 'status'.

Currently, the only supported module is 'logicblox'. If not module is specified,
the command is applied to the master/manager server.
