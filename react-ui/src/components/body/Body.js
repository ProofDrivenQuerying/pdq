//react
import React from 'react';
//components
import SchemaBody from './schemas/SchemaBody';
import QueryBody from './queries/QueryBody';
import PlanBody from './plan/PlanBody';
//redux
import { connect } from 'react-redux';

/**
 * Renders the app's body. Has  a main body that rotates through the states:
 * Schemas, Queries, and Searches.
 *
 * Called from App.js.
 *
 * @author Camilo Ortiz
 */

const Body = ({selectedSchema, body, queryList}) => {
  return (
    <div style={{display:"flex", flexDirection:"row", width:"100vw"}}>

      <div style={{margin: "3rem 1rem 0rem 2rem"}}>
        <SchemaBody/>
      </div>

      <div style={{display:"flex",margin:"3rem 1rem 0rem 1rem"}}>
        <QueryBody/>
      </div>

      <div style={{display:"flex",margin:"3rem 1rem 0rem 1rem"}}>
        <PlanBody/>
      </div>

    </div>
  )
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});



export default connect(mapStatesToProps, null)(Body);
