//react
import React from 'react';
//components
import SchemaBody from './schemas/SchemaBody';
import RelationBody from './relations/RelationBody';
import QueryBody from './queries/QueryBody';
import PlanBody from './plan/PlanBody';
//reactstrap
import { Button} from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import setBody from '../../actions/setBody';
import { getQueries } from '../../actions/getQueries';

/**
 * Renders the app's body. Has  a main body that rotates through the states:
 * Schemas, Queries, and Searches.
 *
 * Called from App.js.
 *
 * @author Camilo Ortiz
 */

const Body = ({selectedSchema, setBody, body, queryList}) => {
  return (
    <div style={{display:"flex", flexDirection:"row", width:"100vw"}}>

      <div style={{padding: "3rem 1rem 0rem 2rem"}}>
        <SchemaBody/>
      </div>

      <div style={{display:"flex",padding:"3rem 1rem 0rem 1rem"}}>
        <QueryBody/>
      </div>

      <div style={{display:"flex",padding:"3rem 1rem 0rem 1rem"}}>
        <PlanBody/>
      </div>

    </div>
  )
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});

//map actions to props
const mapDispatchToProps = (dispatch) =>({
  setBody: (body_type) => dispatch({ type: body_type})
});


export default connect(mapStatesToProps, mapDispatchToProps)(Body);
