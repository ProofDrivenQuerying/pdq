//react
import React from 'react';
//components
import SchemaBlock from './SchemaBlock';
//css
import './schemabody.css';


/**
 * Renders Schema information page
 *
 * @author Camilo Ortiz
 */


const SchemaBody = () => {
    return(
      <div
        style={{border:"1px solid #E0E0E0", borderRadius:"25px",
                boxShadow: "0 0 5px 2px #E0E0E0"}}>

        <header className='body-name-schema'>
          Schemas
        </header>


        <div>
          <div className='schemas'>
            <div>
              <SchemaBlock/>
            </div>

          </div>
        </div>
      </div>
    )
}


export default SchemaBody;
