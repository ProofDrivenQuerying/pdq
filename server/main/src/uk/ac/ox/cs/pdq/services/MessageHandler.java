package uk.ac.ox.cs.pdq.services;

import com.google.protobuf.GeneratedMessage;

// TODO: Auto-generated Javadoc
/**
 * Top level interface for handling message from the Protobuffer protocol.
 * 
 * @author Julien LEBLAY
 *
 * @param <M> the type of incoming messages
 */
public interface MessageHandler<M extends GeneratedMessage> {
	
	/**
	 * Handle.
	 *
	 * @param msg the msg
	 * @return the generated message
	 */
	GeneratedMessage handle(M msg);
}
