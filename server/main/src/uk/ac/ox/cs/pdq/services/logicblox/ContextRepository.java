package uk.ac.ox.cs.pdq.services.logicblox;

import java.util.LinkedHashMap;
import java.util.Map;

import uk.ac.ox.cs.pdq.services.logicblox.Context.Workspace;

/**
 * A container for contexts, indexed by workspace names.
 * 
 * @author Julien Leblay
 */
public class ContextRepository {

	/** The Constant DEFAULT_WORKSPACE_NAME. */
	public static final String DEFAULT_WORKSPACE_NAME = "default";
	
	/** A map of different contexts. */
	private final Map<String, Context> contexts = new LinkedHashMap<>();

	/**
	 * Instantiates a new context repository.
	 */
	public ContextRepository() {
		this.contexts.put(DEFAULT_WORKSPACE_NAME,
				new Context(new Workspace(DEFAULT_WORKSPACE_NAME)));
	}
	
	/**
	 * The default context.
	 *
	 * @return the default context.
	 */
	public Context defaultContext() {
		return this.contexts.get(DEFAULT_WORKSPACE_NAME);
	}
	
	/**
	 * Resolves the input string name into a context.
	 *
	 * @param ws a workspace name
	 * @return the context who workspace has the given name. If no such context
	 * exists, a fresh one is created.
	 */
	public Context resolve(String ws) {
	    Context result = this.contexts.get(ws);
	    if (result == null) {
			result = new Context(new Workspace(ws));
			this.contexts.put(ws, result);
	    }
	    return result;
	}
}
