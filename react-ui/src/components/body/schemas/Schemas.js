// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import { connect } from 'react-redux';
import SchemaItem from './SchemaItem';
import ListGroup from 'react-bootstrap/ListGroup';

/**
 * Renders the schema information component.
 *
 * @author Camilo Ortiz
 */

const Schemas = ({ schemaList}) => {
  /*
    Contains a Header and SchemaList (a list of selectable schemas)
  */
    return(
      <div>
        <h4 className='my-2'>
          Schemas
        </h4>

        <SchemaList schemaList={schemaList}/>
      </div>
    )
}

const SchemaList = ({ schemaList}) => {
  /*
    SchemaList maps the schemas stored in the state (schemaList) to SchemaItem
    components to create a list of schemas
  */
  if(!schemaList.isFetching){
    const schemas = schemaList.schemas.map((schemaFromList, index) =>
        <SchemaItem
          schemaFromList={schemaFromList}
          key={"schema"+index}/>
      );
      return (
        <div>
          <style type="text/css">
            {`
            .list-group-scroll {
              max-height: calc(100vh - 8.1rem);
              margin-bottom: 10px;
              overflow:scroll;
              -webkit-overflow-scrolling: touch;
            }
            `}
          </style>
          <ListGroup variant='scroll'>
            {schemas}
          </ListGroup>
        </div>
    );
  }
  else{
    return null;
  }
}

//redux
const mapStatesToProps = (state) =>{
  return({
    schemaList: state.schemaList
  })
};

export default connect(mapStatesToProps, null)(Schemas);
