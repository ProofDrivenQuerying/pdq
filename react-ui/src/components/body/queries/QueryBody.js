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

 const QueryList = ({ schemaList, setQuery}) => {
   if(schemaList.schemas.length > 0){
     return schemaList.schemas[schemaList.selectedSID].queries.map((queryFromList, index) => {
       return(
         <div>
         {schemaList.selectedQID === index ?
           <div className="query-name-holder" key={schemaList.selectedSID+"_"+index}>
              <ButtonGroup>
               <Button
                color="primary"
                disabled={false}>
                 <span>
                   <span className="query-name">
                     {queryFromList.name}
                   </span>
                 </span>
               </Button>

               <AddQueryModal
                schemaID={schemaList.selectedSID}
                numQueries={schemaList.schemas[schemaList.selectedSID].queries.length}
                queryFromList={queryFromList}
                id={index}/>

                <Button
                  color="link">
                  <FaTrashAlt/>
                </Button>

              </ButtonGroup>
           </div>
           :
           <div className="query-name-holder" key={schemaList.selectedSID+"_"+index}>
              <ButtonGroup>
               <Button
                outline color="secondary"
                disabled={false}
                onClick={(e) => setQuery(index)}>
                 <span>
                   <span className="query-name">
                     {queryFromList.name}
                   </span>
                 </span>
               </Button>

               <AddQueryModal
                schemaID={schemaList.selectedSID}
                numQueries={schemaList.schemas[schemaList.selectedSID].queries.length}
                queryFromList={queryFromList}
                id={index}/>

              </ButtonGroup>
           </div>
         }
        </div>
       )
     })
   }else{
     return null;
   }
 }


const QueryBody = ({schemaList, setQuery}) => {
  return(
    <div>
      <header className='body-title-query'>
        Queries
      </header>
      <div className="queries">

        <QueryList
          schemaList={schemaList}
          setQuery={setQuery}
          />
      </div>

      <header className='body-title-query'>
        Selected Query
      </header>

      <div>
        <div className='querySQL'>
          {schemaList.schemas.length > 0 ?
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
});

export default connect(mapStatesToProps, mapDispatchToProps)(QueryBody);
