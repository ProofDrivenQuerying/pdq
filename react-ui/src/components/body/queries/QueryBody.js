import React from 'react';
import { Button } from 'reactstrap';
import { connect } from 'react-redux';
import './querybody.css';

/**
 * Renders query information components
 *
 * @author Camilo Ortiz
 */

 const QueryList = ({queryList}) => {
   return(
     <div style={{height: "12rem", display: "flex", flexDirection:"column"}}>

       <header className='body-name-query'>
         Queries
       </header>

       {queryList.queryList.name != null?
         <div className="query-name-holder">
           <Button
            color="primary"
            disabled={true}>
             <span>
               <span className="query-name">
                 {queryList.queryList.name}
               </span>
             </span>
           </Button>
         </div>
         :
         null
       }

     </div>
   );
 }


const QueryBody = ({queryList}) => {
  return(
    <div>
      <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
          boxShadow: "0 0 5px 2px #E0E0E0", width: "15rem"}}>

          <QueryList queryList={queryList}/>

        <header className='body-name-query'>
          Selected Query
        </header>

        <div>
          <div className='querySQL'>
            <div>{queryList.queryList.SQL}</div>
          </div>
        </div>
      </div>
    </div>
  );
}

//map states to props
const mapStatesToProps = (state) =>({
  queryList: state.queryList
});


export default connect(mapStatesToProps, null)(QueryBody);
