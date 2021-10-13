// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.logging;

/**
	@author Mark Ridler
	
	This package implements various kinds of logger. For example:
	
	- ChainedStatistics.java
		* Statistics logger that works by appending logs from a sequence of delegate
		* loggers.
	- DynamicStatistics.java
	 	* A  logger allows logging data point as key-value pair,
	 	* dynamic in the sense that it is without prior knowledge of what is to be logged.
	 	* The class also offers some facilities to increment value that are numeric.
	- Periods.java
	 `	* Helper class for representing time intervals of various granularities.
	- ProgressLogger.java
 		* Common interface for ad-hoc loggers, i.e. performance and progress loggers.
 		* The functionalities provided by the interface are disjoint from Log4j.
 	- SimpleProgressLogger.java
 		* This logger simply outputs dots, and can be used to observed the progress
 		* of a process.
 	- SimpleStatisticsCollector.java
 		* Simple statistics collector, It can measure how long certain functions take.
	- StatisticsCollector.java
 		* This class help collecting statistics without prior knowledge of what is to be
 		* logged.
 		* Logged items are identified by a key (String). Logging event can be time
 		* interval, numeric increment or ad-hoc Object values.
 	- StatisticsLogger.java
  		* Top-level class for all statistics logger.
 	- StatKey.java
 		* Keys to be used in statistics collections.
	- Timed.java
 		* Annotation used to distinguish method that can automatically be timed through
 		* Aspect Oriented Programming.
**/
