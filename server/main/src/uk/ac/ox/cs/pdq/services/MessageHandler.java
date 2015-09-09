package uk.ac.ox.cs.pdq.services;

import com.google.protobuf.GeneratedMessage;

/**
 * Top level interface for handling message from the Protobuffer protocol.
 * 
 * @author Julien LEBLAY
 *
 * @param <M> the type of incoming messages
 */
public interface MessageHandler<M extends GeneratedMessage> {
	GeneratedMessage handle(M msg);
}
