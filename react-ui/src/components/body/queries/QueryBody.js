import React from 'react';
import { Button, ButtonGroup } from 'reactstrap';
import EditQueryModal from './EditQueryModal';
import { connect } from 'react-redux';
import './querybody.css';

/**
 * Renders query information components
 *
 * @author Camilo Ortiz
 */

 const QueryList = ({queryList, selectedSchema}) => {
   console.log(selectedSchema);
   return(
     <div style={{height: "10rem", display: "flex", flexDirection:"column"}}>

       {queryList.queryList.name != null && selectedSchema != null ?

         <div className="query-name-holder">
            <ButtonGroup>
             <Button
              color="primary"
              disabled={false}>
               <span>
                 <span className="query-name">
                   {queryList.queryList.name}
                 </span>
               </span>
             </Button>

             <EditQueryModal
              selectedSchema={selectedSchema}
              queryList={queryList}/>

            </ButtonGroup>
         </div>
         :
         null
       }

     </div>
   );
 }


const QueryBody = ({queryList, selectedSchema}) => {
  return(
    <div>
      <header className='body-name-query'>
        Queries
      </header>
      <QueryList
        queryList={queryList}
        selectedSchema={selectedSchema}/>

      <header className='body-name-query'>
        Selected Query
      </header>

      <div>
        <div className='querySQL'>
          <div>{queryList.queryList.SQL}</div>
        </div>
      </div>
    </div>
  );
}

//map states to props
const mapStatesToProps = (state) =>({
  queryList: state.queryList,
  selectedSchema: state.selectedSchema
});


export default connect(mapStatesToProps, null)(QueryBody);
