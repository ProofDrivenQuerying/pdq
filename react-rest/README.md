
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
  	
  Test call to make sure it is working:
  
    http://localhost:8080/initSchemas
   
  Should return the response:
  
    {"schemas":[{"id":0,"name":"schema0","queries":[{"name":"query0","SQL":"SELECT a0.activity_comment\nFROM activityFree AS a0\nWHERE a0.target_pref_name='29'\nAND a0.uo_units='30'","id":0}]},{"id":1,"name":"schema1","queries":[{"name":"query0","SQL":"SELECT a1.city, a1.country2, a1.region, a1.temperature\nFROM YahooPlaceCode AS a0\n\tJOIN YahooWeather AS a1 ON a1.woeid=a0.woeid\n\tJOIN YahooPlaceRelationship AS a2 ON a2.of=a0.woeid AND a2.of=a1.woeid\n\tJOIN YahooPlaces AS a3 ON a3.woeid=a0.woeid AND a3.woeid=a1.woeid AND a3.country=a1.country2 AND a3.woeid=a2.of AND a3.placeTypeName=a2.placeTypeName4 AND a3.name=a2.name4\nWHERE a0.namespace='iso'\nAND a0.code4='FR'\nAND a1.condition='Sunny'\nAND a2.relation='descendants'\nAND a2.placeTypeName4='PointofInterest'\nAND a3.placeTypeName='PointofInterest'","id":0}]},{"id":2,"name":"schema2","queries":[{"name":"query0","SQL":"SELECT a1.w\nFROM S AS a0\n\tJOIN R AS a2 ON a2.x=a0.x\n\tJOIN T AS a1 ON a1.y=a0.y","id":0},{"name":"query1","SQL":"SELECT a1.y\nFROM R AS a0\n\tJOIN S AS a1 ON a1.x=a0.x","id":1}]},{"id":3,"name":"schema3","queries":[{"name":"query0","SQL":"SELECT a0.n_name, a0.n_regionkey, a0.n_comment, a1.c_area\nFROM Nation AS a0\n\tJOIN Country AS a1 ON a1.c_nationkey=a0.n_nationkey","id":0}]},{"id":4,"name":"schema4","queries":[{"name":"query0","SQL":"SELECT a1.s_acctbal, a1.s_name, a0.ps_supplycost, a2.n_regionkey\nFROM partsupp AS a0\n\tJOIN supplier AS a1 ON a1.s_suppkey=a0.ps_suppkey\n\tJOIN nation AS a2 ON a2.n_nationkey=a1.s_nationkey","id":0}]},{"id":5,"name":"schema5","queries":[{"name":"query0","SQL":"SELECT a0.city\nFROM YahooWeather AS a0\n\tJOIN YahooPlaceCode AS a1 ON a1.woeid=a0.woeid\nWHERE a0.condition='Sunny'\nAND a1.namespace='iso'\nAND a1.code4='FR'","id":0}]},{"id":6,"name":"schema6","queries":[{"name":"query0","SQL":"SELECT a0.p_partkey\nFROM part AS a0","id":0}]},{"id":7,"name":"schema7","queries":[{"name":"query0","SQL":"SELECT a0.activity_comment\nFROM activityFree AS a0\nWHERE a0.target_pref_name='29'\nAND a0.uo_units='30'","id":0}]}]}
  
  or something similar.
