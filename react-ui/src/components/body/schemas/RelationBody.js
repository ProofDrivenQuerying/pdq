import React from 'react';
import { Button} from 'reactstrap';
import RelationBlock from './RelationBlock';
import './relationbody.css';


/**
 * Renders Schema information page
 *
 * @author Camilo Ortiz
 */


const RelationBody = () => {
    return(
      <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
          boxShadow: "0 0 5px 2px #E0E0E0"}}>

        <header className='body-name-relation'>
          Relations
        </header>

        <div>
          <div className='relations'>
            <div>
              <RelationBlock/>
            </div>
          </div>
        </div>
      </div>
    )
}


export default RelationBody;
