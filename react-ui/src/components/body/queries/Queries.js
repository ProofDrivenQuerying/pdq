import React from 'react';
import QueryItem from './QueryItem';
import { connect } from 'react-redux';
import ListGroup from 'react-bootstrap/ListGroup';

/**
 * Renders query information components
 *
 * @author Camilo Ortiz
 */

const Queries = ({schemaList, setQuery, removeQuery}) => {
  return(
    <div>
      <h4 className='my-2'>
        {schemaList.isFetching ?
          <div>Queries</div>
        :
          <div>{schemaList.schemas[schemaList.selectedSID].name}'s Queries</div>
        }
      </h4>

      <QueryList schemaList={schemaList}/>

      <h4 className='my-2'>
        Selected Query
      </h4>

      <QuerySQL schemaList={schemaList}/>
    </div>
  );
}

const QueryList = ({ schemaList}) => {
  if(!schemaList.isFetching){
    return (
      <div>
        <style type="text/css">
          {`
          .list-group-half {
            height: calc((100vh - 12rem) / 2);
            max-height: calc((100vh - 12rem) / 2)
            margin-bottom: 10px;
            overflow:scroll;
            -webkit-overflow-scrolling: touch;
          }
          `}
        </style>
        <ListGroup variant='half'>
          {schemaList.schemas[schemaList.selectedSID].queries.map((queryFromList, index) =>
            <QueryItem
              queryFromList={queryFromList}
              key={"query"+index}/>
          )}
        </ListGroup>
      </div>
  );
  }
  else{
    return null;
  }
}

const QuerySQL = ({schemaList}) => {
  if(!schemaList.isFetching && schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID] != null){
    return (
      <p className='half'>
        {schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
      </p>
    );
  }
  else{
    return null;
  }
}

//map states to props
const mapStatesToProps = (state) =>({
  schemaList: state.schemaList,
});

export default connect(mapStatesToProps)(Queries);
