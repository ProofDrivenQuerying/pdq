//react
import React from 'react';
//reactstrap
import { Button} from 'reactstrap';
//actions
import { connect } from 'react-redux';
//css
import './querybody.css';



/**
 * Renders Schema information page
 *
 * @author Camilo Ortiz
 */


const QueryBody = ({queryList}) => {
    return(
      <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
          boxShadow: "0 0 5px 2px #E0E0E0", width: "25rem"}}>

        <header className='body-name-query'>
          Query
        </header>

        <div>
          <div className='queries'>
            <div>{queryList.queryList}</div>
          </div>
        </div>
      </div>
    )
}

//map states to props
const mapStatesToProps = (state) =>({
  queryList: state.queryList
});


export default connect(mapStatesToProps, null)(QueryBody);
