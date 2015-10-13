PDQ (Proof-Driven Querying) is a platform for generating query plans over 
semantically-interconnected data sources with diverse access interfaces. 
PDQ unifies a number of application scenarios involving such data sources.

For example, PDQ can provide a solution for querying data available through 
Web-based APIs. For a given query there may be many Web-based sources which can 
be used to answer it, with the sources overlapping in their vocabularies, and 
differing in their access restrictions (required arguments) and cost. 
PDQ can determine if the query can be answered using the data sources, and if 
so can generate the optimal plan.

PDQ can also be applied to more traditional database scenarios, such as 
optimization of constraints in relational databases in the presence of integrity
constraints and query optimization using materialized views.

PDQ works by generating query plans from proofs that a query is answerable. 
The PDQ planner performs optimization via exploring a space of proofs, with 
each proof corresponding to a different plan.

The source code is available for free for non-commercial use.
See the LICENCE file for details.

I. Requirements
   
 * Java 1.8 or higher
   Most projects will also build on Java 1.7, but some projects like the demo
   and the endpoint require Java 1.8 
 * Maven 2 or higher
   You can build each library outside Maven, e.g. in your favorite IDE, but
   you will have to managed dependencies manually.

II. What is in this distribution
 
The top level directory, containing this README, also contains 9 sub-directories
each corresponding to a sub-project.
The internal dependencies between projects are the following (in Maven, each
of these projects is prefixed with "pdq-"):
	- common : none
	- runtime, planner: common
	- server : planner
	- all others: runtime, planner 

External dependencies are managed by Maven automatically. If you wish to build
the projects outside Maven, make sure the following are on the CLASSPATH:
	- common:    junit-4.11, log4j2-1.2, guava-16, commons-lang3-3.3.1, 
	             commons-collections4-4.0, jgrapht-0.8.3, commons-io-2.4
	- planner:   derby-10.10, mysql-connector-5.1.30 jcommander-1.35
	- runtime:   mysql-connector-5.1.30, postgresql-9.1, jersey-client-2.11,
	             jackson-databind-2.2.3, javax.ws.api-2.0, 
	             jackson-jaxrs-json-provider-2.2.3, jcommander-1.35
	- demo:      prefuse-beta, antlr4-4.3
	- endpoint:  jsp-api-2.0, javax.servlet-api-3.1.0
	- server:    protobuf-java-2.5.0, logicblox proprietary libraries
	- benchmark, regression: none 
	
III. Installation

Under the top directory, type:

	mvn install
	
The JAR will be built and placed in each project's "target/" directory.

For running the version module, please refer to each project's README file.
