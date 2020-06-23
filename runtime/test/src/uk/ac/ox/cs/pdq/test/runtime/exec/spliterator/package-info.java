// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.test.runtime.exec.spliterator;

/**
	@author Mark Ridler
	
	This package contains unit tests:
	
	- AccessTest.java
		* The following are integration tests: Access plans are constructed & executed.
	- CartesianProductTest.java
 		* The following are integration tests: CartesianProduct plans are constructed &
 		* executed.
	- DependentJoinTest.java
 		* this test case is very slow, so disabling it for now. Last time I run it one failed, the rest run successfully.
	- NestedLoopJoinTest.java
		* The following are integration tests: NestedLoop joins are constructed &
 		* executed.
	- ProjectionTest.java
		* The following are integration tests: Projections are constructed & executed.
	- SelectionTest.java
		* The following are integration tests: Selections are constructed & executed.
	- SymmetricMemoryHashJoinTest.java
		* The following are integration tests: SymmetricMemoryHash joins are constructed &
 		* executed.
	- TPCHelper.java
		* Creates a load of attributes, relations and access methods.
	
**/