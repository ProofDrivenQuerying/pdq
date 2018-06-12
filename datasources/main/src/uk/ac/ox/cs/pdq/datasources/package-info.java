package uk.ac.ox.cs.pdq.datasources;

/**
	@author Mark Ridler
	
	This package contains:
	
	- AccessException.java
 		* Access exception implementation .
	- ExecutableAccessMethod.java
 		* This class extends the functionality of an AccessMethodDescriptor used in
 		* common with attribute mapping. Different accessMethod types such as database
 		* or webservice access methods can have different set of attributes, mapping of
 		* such attributes to the relation's attributes happens here.
	- Pipelineable.java
 		* Common interface to pipelinable iterators, i.e. iterators that iterate over
 		* a set of input tuples, and return output tuple one at a time.
	- RelationAccessWrapper.java
 		* I understqnd that there are two views of database objects reflected in the code in common. 
 		* On is the traditional where we don't have access restrictions, hence we have normal relations etc.
 		* The other is the "access restrictions" perspective and this is why this object exists. Is this the case? 
 		* By putting this in a package called wrappers and naming it a wrapper you don't do justice to it if it's the main
 		* "access restriction perspective" object.
 		* 
 		* The Wrapper interface provide access functions.
	- ResettableIterator.java
 		* An iterator that can be reset, i.e. the cursor can be placed back to the
 		* beginning of the underlying Iterable at any time.
	- ResettableTranslatingIterator.java
 		* An iterator that can be reset, i.e. the cursor can be placed back to the
 		* beginning of the underlying Iterable at any time.
	- TranslatingIterator.java
 		* An iterator consuming another iterator.

**/