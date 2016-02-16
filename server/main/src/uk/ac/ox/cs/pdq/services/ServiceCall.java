package uk.ac.ox.cs.pdq.services;

import java.util.concurrent.Callable;

/**
 * A call performed within a service.
 *
 * @author Julien Leblay
 * @param <T> the generic type
 */
public interface ServiceCall<T> extends Callable<T> {
}
