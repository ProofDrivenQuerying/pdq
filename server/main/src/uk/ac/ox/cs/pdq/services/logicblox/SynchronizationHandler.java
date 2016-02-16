package uk.ac.ox.cs.pdq.services.logicblox;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.services.MessageHandler;

import com.google.protobuf.GeneratedMessage;
import com.logicblox.connect.BloxCommand.CommandResponse;
import com.logicblox.connect.BloxCommand.SynchronizeWorkspace;
import com.logicblox.connect.BloxCommand.SynchronizeWorkspaceResponse;

// TODO: Auto-generated Javadoc
/**
 * Handles workspace synchronization requests coming from the client. 
 * 
 * @author Julien LEBLAY
 */
public class SynchronizationHandler implements MessageHandler<SynchronizeWorkspace> {

	/** Logger. */
	static final Logger log = Logger.getLogger(SynchronizationHandler.class);

	/**  Handle on the LogicBlox service. */
	private final SemanticOptimizationService master;

	/**
	 * Default constructor.
	 *
	 * @param master the master
	 */
	public SynchronizationHandler(SemanticOptimizationService master) {
		this.master = master;
	}

	/* (non-Javadoc)
	 * @see uk.ac.ox.cs.pdq.services.MessageHandler#handle(com.google.protobuf.GeneratedMessage)
	 */
	@Override
	public GeneratedMessage handle(SynchronizeWorkspace command) {
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
		return CommandResponse.newBuilder().setSynchronizeWorkspace(
				SynchronizeWorkspaceResponse.newBuilder()
						.setSuccess(true).build()).build();
	}

}
