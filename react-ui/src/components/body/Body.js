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

const Body = ({selectedSchema, setBody, body, relationList}) => {
  return (
    <div>
      <div style={{float: "left", padding: "3rem 3rem 0rem 3rem", height:"32rem",
                    justifyContent: "space-between"}}>
        <SchemaBody/>
      </div>

      <div>
        {relationList.relationList.relations != null ?
          <div style={{display: "flex", flexDirection: "row",
                        padding: "3rem 2rem 0rem 2rem", flexWrap: "wrap"}}>

            <div style={{float: "none", paddingRight: "2rem", marginBottom: "1rem"}}>
              <RelationBody/>
            </div>

            <div style={{float: "none", paddingRight: "2rem", marginBottom: "1rem"}}>
              <QueryBody/>
            </div>

            <div style={{float: "none", width:"92%", marginBottom: "1rem",
                          marginRight: "3.8rem"}}>
              <PlanBody/>
            </div>

          </div>

        :
          null}
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
