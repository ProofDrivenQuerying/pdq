//react
import React from 'react';
//reactstrap
import { Button } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import setSchema from '../../../actions/setSchema';
import { getRelations } from '../../../actions/getRelations';
import { getQueries } from '../../../actions/getQueries';
//css
import './schemablock.css';
//img
import moreDots from '../../../img/threeDots.png';

/**
 * SchemaBlock returns a button for each schema name in schemaList.
 *
 * Conditional: if the schema name's id matches the one from schemaList, it
 * gets highlighted
 *
 * @author Camilo Ortiz
 */


const SchemaBlock = ({ schemaList, setSchema, selectedSchema, getRelations, getQueries }) => {
  function setSchema_getRelations(schemaFromList, id){
     setSchema(schemaFromList, id);
     getRelations(id);
     getQueries(id);
   }

  if(!schemaList.isFetching){
    return schemaList.schemaList.map((schemaFromList, index) => {
      return(
        <div className="schema-name-holder" key={schemaFromList.id}>
        {
          //conditional:
        }
        {selectedSchema.selectedSchema != null &&
            schemaFromList.id === selectedSchema.id ?

          <div style={{display: "flex"}}>
            <Button color="primary" id = {schemaFromList.id} block
                    onClick={(e) => setSchema_getRelations(schemaFromList, schemaFromList.id)}>
              <span>
                  <span className="schema-name">
                    {selectedSchema.selectedSchema.name}
                  </span>
              </span>
            </Button>

            <Button color="link">
              <img src={moreDots} className="threeDots" alt="more"/>
            </Button>
          </div>

          :

          <div style={{display: "flex"}}>
            <Button outline color="secondary" id = {schemaFromList.id} block
                    onClick={(e) => setSchema_getRelations(schemaFromList, schemaFromList.id)}>
              <span>
                <span className="schema-name">
                  {schemaFromList.name}
                </span>
              </span>
            </Button>

            <Button color="link">
              <img src={moreDots} className="threeDots" alt="more"/>
            </Button>
          </div>
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
