package uk.ac.ox.cs.pdq.test.util;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.log4j.Logger;

import uk.ac.ox.cs.pdq.EventHandler;
import uk.ac.ox.cs.pdq.planner.io.xml.ProofWriter;
import uk.ac.ox.cs.pdq.planner.reasoning.Proof;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;

/**
 * Write a proof from an proof event into a file.
 * 
 * @author Julien Leblay
 */
public class ProofEventHandler implements EventHandler {

	/** Logger. */
	private static Logger log = Logger.getLogger(ProofEventHandler.class);

	/** The path to the file where the proof is to be written */
	private final String filePath;
	
	/**
	 * Default constructor
	 * @param path
	 */
	public ProofEventHandler(String path) {
		this.filePath = path;
	}
	
	/**
	 * @param p Proof
	 */
	@Subscribe
	public void processEvent(Proof p) {
		Preconditions.checkArgument(p != null);
		log.info("Writing proof to file '" + this.filePath + "'");
		try (PrintStream o = new PrintStream(this.filePath)) {
			new ProofWriter().write(o, p);
		} catch (FileNotFoundException e) {
			log.error(e);
		}
	}
}
