package uk.ac.ox.cs.pdq.rest.jsonobjects;

import uk.ac.ox.cs.pdq.db.Schema;
import uk.ac.ox.cs.pdq.db.Relation;
import uk.ac.ox.cs.pdq.db.Attribute;
import uk.ac.ox.cs.pdq.db.AccessMethodDescriptor;

/**
 * What is returned to the client when they dispatch a GET request for a relation. This class
 * defines the structure of a schema's relations.
 *
 * @author Camilo Ortiz
 */
public class JsonRelationList{
  public String name;
  public int id;
  public JsonRelation[] relations;

  /**
   * Populates JsonRelationList's fields
   *
   * @param schema
   * @param id
   */
  public JsonRelationList(Schema schema, int id){
    int number_of_relations = schema.getNumberOfRelations();
    Relation[] schema_relations = schema.getRelations();

    this.relations = new JsonRelation[number_of_relations];

    for(int i = 0; i < number_of_relations; i++){
      Relation relation = schema_relations[i];
      String name = relation.getName();

      Attribute[] attributes = relation.getAttributes();
      Integer num_attributes = attributes.length;
      JsonAttribute[] jsonAttributes = new JsonAttribute[num_attributes];

      //make attribute list
      for (int j = 0; j < num_attributes; j++){
        String a_name = attributes[j].getName();
        String a_type = attributes[j].getType().toString();

        jsonAttributes[j] = new JsonAttribute(a_name, a_type);
      }

      AccessMethodDescriptor[] accessMethods = relation.getAccessMethods();
      JsonAccessMethod[] jsonAccessMethods = new JsonAccessMethod[accessMethods.length];

      //make access type list
      for (int k = 0; k < accessMethods.length; k++){
        String a_m_name = accessMethods[k].getName();
        String a_m_type;
        if(accessMethods[k].getNumberOfInputs() == 0){
          a_m_type = "free";
        }else{
          a_m_type = "limited";
        }
        jsonAccessMethods[k] = new JsonAccessMethod(a_m_name, a_m_type);
      }

      JsonRelation jsonRelation = new JsonRelation(name, jsonAttributes, jsonAccessMethods);
      relations[i] = jsonRelation;
    }
    this.id = id;
    this.name = "schema"+Integer.toString(id);
  }
}
