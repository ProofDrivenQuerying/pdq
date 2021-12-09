// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

package uk.ac.ox.cs.pdq.rest.jsonobjects.schema;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;

/**
 * What is returned to the client when they dispatch a GET request for a relation. This class
 * defines the structure of a schema's relations.
 *
 * @author Camilo Ortiz
 */
public class RelationArray {
  public String name;
  public int id;
  public Relation[] relations;

  /**
   * Populates JsonRelationList's fields
   *
   * @param schema
   * @param id
   */
  public RelationArray(Schema schema, int id){
    int number_of_relations = schema.getNumberOfRelations();
    uk.ac.ox.cs.pdq.db.Relation[] schema_relations = schema.getRelations();

    this.relations = new Relation[number_of_relations];

    for(int i = 0; i < number_of_relations; i++){
      uk.ac.ox.cs.pdq.db.Relation relation = schema_relations[i];
      String name = relation.getName();

      uk.ac.ox.cs.pdq.db.Attribute[] attributes = relation.getAttributes();
      Integer num_attributes = attributes.length;
      Attribute[] jsonAttributes = new Attribute[num_attributes];

      //make attribute list
      for (int j = 0; j < num_attributes; j++){
        String a_name = attributes[j].getName();
        String a_type = attributes[j].getType().toString();

        jsonAttributes[j] = new Attribute(a_name, a_type);
      }

      AccessMethodDescriptor[] accessMethods = relation.getAccessMethods();
      AccessMethod[] jsonAccessMethods = new AccessMethod[accessMethods.length];

      //make access type list
      for (int k = 0; k < accessMethods.length; k++){
        String a_m_name = accessMethods[k].getName();
        String a_m_type;
        if(accessMethods[k].getNumberOfInputs() == 0){
          a_m_type = "free";
        }else{
          StringBuffer stringBuffer = new StringBuffer();
          stringBuffer.append("limited:");
          char sep = '[';
          for(int input: accessMethods[k].getInputs()){
            stringBuffer.append(sep).append(input);
            sep = ',';
          }
          stringBuffer.append(']');
          a_m_type = stringBuffer.toString();
        }
        jsonAccessMethods[k] = new AccessMethod(a_m_name, a_m_type);
      }

      Relation jsonRelation = new Relation(name, jsonAttributes, jsonAccessMethods);
      relations[i] = jsonRelation;
    }
    this.id = id;
    this.name = "schema"+Integer.toString(id);
  }
}
