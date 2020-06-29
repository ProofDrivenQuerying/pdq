// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


/**
 * Serializable array of schemas.
 *
 * @author Camilo Ortiz
 */
public class SchemaArray {

  public SchemaName[] schemas;

  public SchemaArray(SchemaName[] schemas){
    this.schemas = schemas;
  }
}
