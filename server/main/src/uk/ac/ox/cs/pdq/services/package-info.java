package uk.ac.ox.cs.pdq.services;

/**
	@author George Konstantinidis

	This package contains classes that attempt to support optimization of external rule-based systems with a particular 
	focus on the LogciBlox database system.
	
	A "service" is a very generic process that listens to a port, and receives incoming connection.
	One can start, run and stop a service.
	
	The concrete service, with which in mind this package was developed, is services.logicblox.SemanticOptimizationService
	which listens for outside connection, accepts connection and registers schema information, 
	integrity constraints, and views. It also receives updates on this registered information and adds/removes elements 
	accordingly. It's main task is to receive query optimization requests and calls the PDQ planner to try to rewrite 
	the corresponding queries using the view and constraints information registered so far.
	
	All incoming and outgoing messages follow google's protobuffer protocol.
	
	Subpackages in this package include:
	 
	-services: the top level package including
		-MessageHandler.java: Top level interface for handling messages from the Protobuffer protocol 
		-Service.java: Top-level interface for all services
		-ServiceCall.java: Interface for calls performed within a service. 
		Known implementations are:
			(1) services.logicblox.SemanticOptimizationService.DefaultServiceCall whose code lies in 
		       SemanticOptimizationService.java and handles LB schema update requests, "topological order" update requests, 
		       and rule optimization requests.
			(2) services.ServiceManager.ExecuteCommandCall whose code lies in ServiceManager.java and handles commands 
			   to start, stop and show the status of a service.
		-ServiceFactory.java: Associates implementations of services to a given name, and then "produces" those services on 
		request by their name.
		-ServiceManager.java: Top-level service that provides the means of managing other services (starting, stop, status, etc.)
		-TestService.java A dummy test service which simply echoes whatever incoming messages it gets.
		
	-services.logicblox:
		-Context.java: The context holds information about Logicblox workspaces. It is essentially a wrapper for a schema
		(including views/constraints). It is in charge of converting and storing LB-data structures to PDQ-data 
		structures. It supports updates on the schema/context.
		It also maintains a BiMap<String,Long> called topoOrder which stores a "rank" for all relations; when the query 
		comes all relations of lower rank than that of the query relations become inaccessible.
		-ContextRepository.java: A container for contexts, indexed by workspace names. Every LB workspace is associated
		with a context. LB supports more than one workspaces (essentially switching between workspaces).
		-DelimetedMessageProtocol.java: Simple protocol for sending/receiving sequences of Protocol Buffer messages 
		(messages are "BloxCommand" objects defined in the external LB lib) over the same input/output stream. 
		It uses a LB jar to encode/decode messages.
		-LogicBloxParameters.java: Holds logicblox-specific parameters. It also registers where the PDQ hook within LB lies.
		-SemanticOptimizationService.java: The "main" semantic-optimization service for the LogixBlox database. It receives LB
		messages and depending on their kind it instantiates an OptimizationHandler, a SynchronizationHandler or a TopoOrderHandler.
		-OptimizationHandler.java:Handles the LB message, called "command", which is a BloxCommand object defined in the external 
		LB library. As soon as a request comes it creates a ProtoBufferUnwrapper object for the schema associated with the incoming
		message's workspace. Then:
	 		(1) It uses the ProtoBufferUnwrapper object to transform the LB "rule" in the message to a PDQ ConjunctiveQuery, 
	 		(2) it instantiates a LogicBloxDelegateCostEstimator, which in turn asks LB to cost the original "non-optimized" rule
			(3) it calls the planner (passing the LB cost estimator above as a parameter) to optimize the rule and return a DAGPlan
			(4) If the plan return has lower cost than the original query it uses the DAGPlanToConjunctiveQuery object to create a
	 		    ConjunctiveQuery, 
	 		    (4.1) It uses a QueryToProtoBuffer object (which is a Rewriter) to write the query into a google protobuf Rule object,
	 		    and uses the external LB lib to transform this into a google protobuf message (a BloxCommand) delivering it to 
	 		    SemanticOptimizationService which sends it to LB.
		-SynchronizationHandler.java:  Handles the LB message, called "command", which is a BloxCommand.SynchronizeWorkspace object 
		defined in the external LB library. As soon as a request comes in, it checks for the "Action" it contains. Action is defined
		in the external LB lib and is ADD or REMOVE. It then checks the "Kind" (also defined in the external lib) of the message. 
		Kinds are PREDICATE, CONSTRAINT or RULE. Depending on the "Kind" of the message either a PredicateDeclaration object (in the 
		case of PREDICATE) or a Rule object (in the cases of CONSTRAINT or RULE) is extracted from the message, and added or removed 
		(depending on the action) from the context associated with this message's workspace. PredicateDeclaration and Rule are 
		google protobuf objects defined in the extrenal lib. Context unwraps these objects using the ProtoBufferUnwrapper.java
		-TopoOrderUpdateHandler.java: Handles update messages on the topological ordering of predicates and rules, in the 
		database-lifetime execution graph of LB.
		As soon as a request comes in, we resolve the associated PDQ context. The incoming message has a list of 
		BoxCommand.TopoOrderUpdate.Entry objects. Each entry contains a long number "rank" and a string "name", representing the 
		topological order of the relation with that name inside LB. These entries are delegated to Context: if the rank is < 0 
		the object is removed for the topoOrder map that Context maintains (probably meaning that the relation is not relevant 
		any more). Else a new entry in the Context's map is registered (overwriting older ones with the same rank).
		
	-services.logicblox.cost:		
		-LogicBloxDelegateCostEstimator.java (I think I found a bug line 177): Interacts with the Logicblox server to determine the cost of the given query.
		 It support cost estimation for a ConjuctiveQuery rule or for a DAGPlan.
		 It the input is a DAGPlan it transforms it in a ConjunctiveQuery rule by using the classes DAGPlanToConjunctiveQuery.java,
		 QueryHeadProjector.java, Deskolemizer.java (and PullEqualityRewriter.java). Then goes to the ConjunctiveQuery rule optimization case.
		 If the rule is recursive, and the default upper bound cost is returned. 
	     Else the following happen:
	  		(1) it uses the QueryToProtoBufferRewriter to rewrite the input ConjunctiveQuery object to an LB/google-protobuf Rule object 
	  		and through calls to the LB linked lib this gets transformed to a (externally defined) CommandResponse object, 
	  		which models the respond to a command/message.
	  		(2) It then uses the DelimitedMessageProtocol.java to send the message to LB (if something goes while using the 
	  		QueryToProtoBufferRewriter wrong it sends an exception message to LB). 
	   		(3) It then receives a message, through DelimitedMessageProtocol, which is expected to be an LB/google-protobuf (externally
	   		defined) "Option" object.
	   		(4) If Option.isSome() is false it means that we received a proble LB message that checks whether PDQ is alive, and we ignore it.
	   		If Option.isSome() is true it means we can extract a RuleCostEstimate LB/google-protobuf (externally defined) object.
	   		On this we can get a cost, which this estimator does, and returns it.
			
	NEED A BIT MORE WORK ON THE FOLLOWING		
	-services.logicblox.rewrite: Utility Rewriters.
		-DAGPlanToConjunctiveQuery.java: Rewrites DAGPlans to conjunctive queries.
		-Deskolemizer.java: Deskolemizes an input formula, by replacing any Skolem terms with fresh variables.
		-ProtoBufferUnwrapper.java: Transforms LB/google-protobuf objects into PDQ objects. It instantiates a new Schema builder, 
		for the schema associated with the workspace of the request. 
		 The reason for mainting a builder is ??
		-PullEqualityRewriter.java: Rewriter that produces an output formula equivalent to the input one, where constants in 
		 atoms have moved as external equality predicates.
		-PushEqualityRewriter.java: Rewriter that produces an output formula equivalent to the input one, containing no equality 
		 predicates.
		-QueryHeadProjector.java:
		-QueryToProtoBuffer.java:  Converts queries to Logicblox-ready protocol buffer messages.
		-SentenceToRule.java:
	
**/