import React from 'react';
import { Button, ButtonGroup } from 'reactstrap';
import { FaTrashAlt } from 'react-icons/fa';
import AddQueryModal from './AddQueryModal';
import { connect } from 'react-redux';
import './querybody.css';

/**
 * Renders query information components
 *
 * @author Camilo Ortiz
 */

 const QueryList = ({ schemaList, setQuery, removeQuery}) => {
   if(schemaList.schemas.length > 0){
     return schemaList.schemas[schemaList.selectedSID].queries.map((queryFromList, index) => {
       return(
         <div key={schemaList.selectedSID+"_"+index}>
           <div className="query-name-holder">
              <ButtonGroup>
              {schemaList.selectedQID === index ?
                 <Button
                  color="primary">
                   <span>
                     <span className="query-name">
                       {queryFromList.name}
                     </span>
                   </span>
                 </Button>
                 :
                 <Button
                  outline color="secondary"
                  onClick={(e) => setQuery(index)}>
                   <span>
                     <span className="query-name">
                       {queryFromList.name}
                     </span>
                   </span>
                 </Button>}

               <AddQueryModal
                schemaID={schemaList.selectedSID}
                numQueries={schemaList.schemas[schemaList.selectedSID].queries.length}
                queryFromList={queryFromList}
                id={index}
                />

                { //Delete button not implemented in backend yet:

                  // index === 0 ?
                  // null
                  // :
                  // <Button
                  //   color="link"
                  //   onClick={(e)=> {
                  //     removeQuery(schemaList.selectedSID, index)
                  //     setQuery(0)
                  //   }}
                  //   >
                  //   <FaTrashAlt/>
                  // </Button>
                }
              </ButtonGroup>
           </div>
        </div>
       )
     })
   }else{
     return null;
   }
 }


const QueryBody = ({schemaList, setQuery, removeQuery}) => {
  return(
    <div>
      <header className='body-title-query'>
        Queries
      </header>

      <div className="queries">
        <QueryList
          schemaList={schemaList}
          setQuery={setQuery}
          removeQuery={removeQuery}
          />
      </div>

      <header className='body-title-query'>
        Selected Query
      </header>

      <div>
        <div className='querySQL'>
          {schemaList.schemas.length > 0 && schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID] !== undefined?
            <div>{schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}</div>
            :
            null
          }
        </div>
      </div>
    </div>
  );
}

//map states to props
const mapStatesToProps = (state) =>({
  schemaList: state.schemaList,
});

const mapDispatchToProps = (dispatch) =>({
  setQuery: (id) => dispatch({ type: 'SET_Q_ID', id: id}),

  removeQuery: (schemaID, queryID) => dispatch({
    type: 'REMOVE_SCHEMALISTQUERY',
    schemaID: schemaID,
    queryID: queryID
  })

});

export default connect(mapStatesToProps, mapDispatchToProps)(QueryBody);
