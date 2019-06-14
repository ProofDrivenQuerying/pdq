import React from 'react';
import SchemaBody from './schemas/SchemaBody';
import RelationBody from './schemas/RelationBody';
import QueryBody from './queries/QueryBody';
import SearchBody from './searches/SearchBody';
import { connect } from 'react-redux';
import { Button} from 'reactstrap';
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
      <div style={{float: "left", padding: "3rem 3rem 0rem 3rem", height:"32rem"}}>
        <SchemaBody/>
        <div>
          {// selectedSchema.id != null ?
          //   <div className="next-button" style={{
          //     padding: '1rem', display:"flex", flexDirection:"column",
          //     justifyContent: "center"
          //   }}>
          //     <Button type="submit"
          //             className="btn btn-primary"
          //             onClick={() => {setBody('query')}}></Button>
          //   </div>
          //   :
          //   null
        }
        </div>
      </div>

      <div style={{float: "none", padding: "3rem 4rem 0rem 21rem", height:"32rem"}}>
        {relationList.relationList.relations != null ?
          <RelationBody/>
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
