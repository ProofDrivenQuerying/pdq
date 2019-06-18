//react
import React from 'react';
//reactstrap
import { Button } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//css
import './relationblock.css';


const RelationBlock = ({ relationList }) => {
  if(relationList.relationList.relations.length > 0){
    return relationList.relationList.relations.map((relation, index) => {
      return(
        <div className = "relation-name-holder" key={"_r"+index}>
          <Button color="secondary"
                   block
                  style={{height: "8rem", width: "8rem", textOverflow: "ellipsis",
                  whiteSpace: "nowrap", overflow: "hidden"}}>
              <span className="relation-name">
                {relation.name}
              </span>
          </Button>

        </div>
      )
    })
  }else{
    return null;
  }
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});


export default connect(mapStatesToProps, null)(RelationBlock);
