1.  DAGOptimized / DAGILP slower to find a find a first plan than generic.
	How to reproduce
	On test/dag/benchmark/case_004 to 006, no plan is found when the time limit 
	is 1 minute. Generic find plans within that time limit.

2. In some case, apparently linked with multi-threading, a timeout is reached
without properly throwing a LimitedReachedException. This causes the planner to
occasionally complete silently without finding a plan. In regression testing,
where there is an expected plan, this is reported as an error, although the 
actual reason why no plan was found is the time limit (not an error 
stricto sensu). 