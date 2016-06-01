package uk.ac.ox.cs.pdq.services;

import com.google.protobuf.GeneratedMessage;

/**
 * Top level interface for handling messages from the Protobuffer protocol.
 * 
 * @author Julien LEBLAY
 *
 * @param <M> the type of incoming messages
 */
public interface MessageHandler<M extends GeneratedMessage> {
	
	/**
	 * Handle the message.
	 *
	 * @param msg the msg
	 * @return the generated message
	 */
	GeneratedMessage handle(M msg);
}
