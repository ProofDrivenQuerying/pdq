import React from 'react';
import { Button } from 'reactstrap';
import { connect } from 'react-redux';
import './schemablock.css';
import setSchema from '../../../actions/setSchema';
import { getRelations } from '../../../actions/getRelations';
import { getQueries } from '../../../actions/getQueries';

/**
 * Renders selectable boxes containing all of the available schema.
 * Called from SchemaBody.js
 *
 * @author Camilo Ortiz
 */



const SchemaBlock = ({ schemaList, setSchema, selectedSchema, getRelations, getQueries }) => {
  function setSchema_getRelations(schemaFromList, id){
     setSchema(schemaFromList, id);
     getRelations(id);
     getQueries(id);
   }

  //return a SchemaBlock for each schema in schemalist got in app constructor
  if(!schemaList.isFetching){
    return schemaList.schemaList.map((schemaFromList, index) => {
      return(
        <div className="schema-name-holder" key={schemaFromList.id}>
        {selectedSchema.selectedSchema != null &&
          schemaFromList.id === selectedSchema.id ?
          <Button color="primary" id = {schemaFromList.id} block
                  onClick={(e) => setSchema_getRelations(schemaFromList, schemaFromList.id)}>
            <span>
                <span className="schema-name">
                  {selectedSchema.selectedSchema.name}
                </span>
            </span>
          </Button>
          :
          <Button outline color="secondary" id = {schemaFromList.id} block
                  onClick={(e) => setSchema_getRelations(schemaFromList, schemaFromList.id)}>
            <span>
              <span className="schema-name">
                {schemaFromList.name}
              </span>
            </span>
          </Button>
        }
        </div>
      )
    })
  }else{
    return(
      <div>
        Loading...
      </div>
    )
  }
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});


//map actions to props
const mapDispatchToProps = (dispatch) =>({
  setSchema: (schema, id) => dispatch({ type: 'SET', schema: schema, id: id}),
  getRelations: (id) => dispatch(getRelations(id)),
  getQueries: (id) => dispatch(getQueries(id))
});

//connect component to redux store

export default connect(mapStatesToProps, mapDispatchToProps)(SchemaBlock);
