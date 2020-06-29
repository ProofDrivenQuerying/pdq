// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;


/**
 * Serializable Relation class.
 *
 * @author Camilo Ortiz
 */
public class Relation {
  public String name;
  public Attribute[] attributes;
  public AccessMethod[] accessMethods;

  public Relation(String name, Attribute[] attributes, AccessMethod[] accessMethods){
    this.name = name;
    this.attributes = attributes;
    this.accessMethods = accessMethods;
  }
}
