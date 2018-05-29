package uk.ac.ox.cs.pdq.datasources.services;

// This package represents the new implementation of processing to create a RESTExecutableAccessMethod and call it.
// AccessEvent, AccessPreProcessor and AccessPostProcessor are used to encapsulate the behaviour of Usage Policies
// from the policies package. JsonResponseUnmarshaller, ResponseUnmarshaller and XmlResponseUnmarshaller are used to
// unmarshal the response from REST. RESTRequestEvent and RESTREsponseEvent represent the timing of events around the
// call to REST. RESTExecutableAccessMethod is the main file that does most of the specification processing.
//
// Mark Ridler