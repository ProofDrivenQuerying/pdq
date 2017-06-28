package uk.ac.ox.cs.pdq.datasources.services;

/**
	@author Efthymia Tsamoura
	This package defines service related wrappers. 
	The Service interface defines the methods of a web-accessible service wrapper.
	The RequestEvent and ResponseEvent interfaces define the request and response events of a service.
	The top-level wrapper interface is uk.ac.ox.cs.pdq.db.wrappers.RelationAccessWrapper.
	
	The packages includes a ServiceBuilder class which creates service objects.   
	The current implementation supports REST web services. 
	
	The .policies package defines the polices of the services, 
	i.e., the paging limit, if the service returns data into pages (see PagingLimit class), 
	the number of allowed access requests per unit time (see RequestAllowance class), 
	or the number of data we are allowed to access per unit time (see ResultAllowance class).
	
	The .rest package defines classes related to creating REST services. This package 
	implements the interfaces of the top level service package.
	
**/