// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

/**
 *  Serializable Attribute class.
 *
 * @author Camilo Ortiz
 */
public class Attribute {
  public String name;
  public String type;

  public Attribute(String name, String type){
    this.name = name;
    this.type = type;
  }
}
