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

	public static final String DEFAULT_WORKSPACE_NAME = "default";
	
	private final Map<String, Context> contexts = new LinkedHashMap<>();

	public ContextRepository() {
		this.contexts.put(DEFAULT_WORKSPACE_NAME,
				new Context(new Workspace(DEFAULT_WORKSPACE_NAME)));
	}
	
	/**
	 * @return the default context.
	 */
	public Context defaultContext() {
		return this.contexts.get(DEFAULT_WORKSPACE_NAME);
	}
	
	/**
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
