import React from 'react';
import SchemaBody from './schemas/SchemaBody';
import QueryBody from './queries/QueryBody';
import PlanBody from './plan/PlanBody';
import { connect } from 'react-redux';

/**
 * Renders the app's body. Displays SchemaBody, QueryBody, and PlanBody
 * components.
 * Called from App.js.
 *
 * @author Camilo Ortiz
 */

const Body = ({selectedSchema, body, queryList}) => {
  return (
    <div style={{display:"flex", flexDirection:"row", width:"100vw"}}>

      <div style={{margin: "3rem 1rem 0rem 2rem",
                   border:"2px solid #E0E0E0",
                   borderRadius:"25px",
                   width: "14rem"}}
      >
        <SchemaBody/>
      </div>

      <div style={{margin:"3rem 1rem 0rem 1rem",
                   border:"2px solid #E0E0E0",
                   borderRadius:"25px",
                   width: "15rem"}}
      >
        <QueryBody/>
      </div>

      <div style={{margin:"3rem 1rem 0rem 1rem",
                   border:"2px solid #E0E0E0",
                   borderRadius:"25px",
                   width: "43rem"}}
      >
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
