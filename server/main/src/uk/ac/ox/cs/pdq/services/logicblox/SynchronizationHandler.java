package uk.ac.ox.cs.pdq.services.logicblox;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.services.MessageHandler;

import com.google.protobuf.GeneratedMessage;
import com.logicblox.connect.BloxCommand.CommandResponse;
import com.logicblox.connect.BloxCommand.SynchronizeWorkspace;
import com.logicblox.connect.BloxCommand.SynchronizeWorkspaceResponse;

/**
 * Handles workspace synchronization requests coming from the client. 
 * 
 * @author Julien LEBLAY
 */
public class SynchronizationHandler implements MessageHandler<SynchronizeWorkspace> {

	/** Logger. */
	static final Logger log = Logger.getLogger(SynchronizationHandler.class);

	/**  Handle on the LogicBlox-communicating master service. */
	private final SemanticOptimizationService master;

	/**
	 * Default constructor.
	 *
	 * @param master the master
	 */
	public SynchronizationHandler(SemanticOptimizationService master) {
		this.master = master;
	}

	/**
	 *  Handles the LB message, called "command", which is a BloxCommand.SynchronizeWorkspace object defined in the external 
	 *	LB library. As soon as a request comes in, it checks for the "Action" it contains. Action is defined in the external LB lib
	 *  and is ADD or REMOVE. It then checks the "Kind" (also defined in the external lib) of the message. Kinds are PREDICATE,
	 *  CONSTRAINT or RULE. Depending on the "Kind" of the message either a PredicateDeclaration object (in the case of PREDICATE)
	 *  or a Rule object (in the cases of CONSTRAINT or RULE) is extracted from the message, and added or removed (depending on the
	 *  action) from the context associated with this message's workspace. PredicateDeclaration and Rule are google protobuf objects
	 *  defined in the external lib. Context unwraps these objects using the ProtoBufferUnwrapper.java
	 */
	@Override
	public GeneratedMessage handle(SynchronizeWorkspace command) {
		
		if(SemanticOptimizationService.filterLBname(command.getName()))
			log.info("Igonring "+command.getName());
		else
		{
			log.debug("Synchronizing workspace: ");
			Context context = this.master.resolve(command.getWorkspace());
			try {

				switch(command.getAction()) {
				case ADD:
					log.debug("Adding " + command.getName());
					switch(command.getKind()) {
					case PREDICATE:
						context.putRelation(command.getName(), command.getPredicate());
						break;
					case CONSTRAINT:
						context.putDependency(command.getName(), command.getRule());
						break;
					case RULE:
						context.putView(command.getName(), command.getRule());
						break;
					}
					break;
				case REMOVE:
					log.debug("Removing: " + command.getName());
					context.remove(command.getName());
					break;
				}
			} catch (Exception e) {
				log.warn(e);
				return CommandResponse.newBuilder()
						.setSynchronizeWorkspace(SynchronizeWorkspaceResponse.newBuilder()
								.setSuccess(false).build()).build();
			}
		}
		return CommandResponse.newBuilder().setSynchronizeWorkspace(
				SynchronizeWorkspaceResponse.newBuilder()
						.setSuccess(true).build()).build();
	}

}
