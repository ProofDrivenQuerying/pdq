package uk.ac.ox.cs.pdq.services.logicblox;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Ints;
import com.google.protobuf.GeneratedMessage;
import com.logicblox.common.Option;
import com.logicblox.common.Streams;
import com.logicblox.connect.BloxCommand.Command;

/**
 * Simple protocol for send sequences of Protocol Buffer messages over the same
 * input/output stream. For this, the protocol buffer message needs to be
 * wrapped in a messaging format that indicates the size of the message, since
 * protocol buffer messages do not have terminators.
 *
 * The format we use here is very simple: 4 bytes for the integer size of the
 * data, followed by the data.
 *
 * @author Martin Bravenboer
 * @author Julien Leblay (modified January 2015)
 */
public class DelimitedMessageProtocol {

	/** Logger. */
	private static Logger log = Logger
			.getLogger(DelimitedMessageProtocol.class);

	/**
	 * @param istream
	 *            InputStream
	 * @return Option<? extends GeneratedMessage>
	 * @throws IOException
	 */
	public static Option<? extends GeneratedMessage> receive(
			InputStream istream) throws IOException {
		assert (istream != null);

		final byte[] bytes = new byte[4];
		int numRead = Streams.reliableRead(istream, bytes);
		if (numRead < 4) {
			if (numRead == 0) {
				return Option.none();
			}
			throw new IOException(
					"Expected to read 4 bytes indicating the size of a message,"
					+ " received " + numRead);
		}
		int size = Ints.fromByteArray(bytes);
		BufferedInputStream bis = new BufferedInputStream(istream);
		try {
			bis.mark(size);
			return Option.some(Command.parseFrom(ByteStreams.limit(bis, size)));
		} catch (Exception e) {
			log.trace(e);
			return Option.none();
		}
	}

	/**
	 * Send a message on the given output stream.
	 * 
	 * @param ostream
	 *            OutputStream
	 * @param message
	 *            GeneratedMessage
	 * @throws IOException
	 */
	public static void send(OutputStream ostream, GeneratedMessage message)
			throws IOException {
		int size = message.getSerializedSize();
		ostream.write(Ints.toByteArray(size));
		message.writeTo(ostream);
		ostream.flush();
	}
}
