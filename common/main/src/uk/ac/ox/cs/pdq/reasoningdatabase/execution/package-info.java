// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.reasoningdatabase.execution;

/**
 * @author Mark Ridler
 * 
 * The databasemanagement.execution sub-package contains:
 * 
 *  -- ExecutionManager, which is responsible for executing database requests
 *  -- ExecutorThread, which is a single thread that represents a connection to a remote database provider.
 *  -- Task, which is a Command that will be executed by an ExecutorThread.
 *
 */
