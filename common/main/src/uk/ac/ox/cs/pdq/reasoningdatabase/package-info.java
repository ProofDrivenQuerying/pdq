// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase;

/**
 * The database management package contains the interface towards external or
 * internal database implementations. This interface is the
 * DatabaseManager.java. <br>
 * Current implementations: <br>
 * - ExternalDatabaseManager: this class will create a connection to a postgres
 * sql server. It is possible add further DB implementations.<br>
 * - LogicalDatabaseInstance: Similar to the external one, but it will map
 * multiple instances over one physical database by adding an id for each fact
 * and a mapping table that describes which fact belongs to which instance.<br>
 * - InternalDatabaseManager: does the same as the LogicalDatabaseInstance
 * without the need for an external database.
 * 
 * @author Gabor Gyorkei
 */
