//react
import React from 'react';
//reactstrap
import { Table, Button } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//css
import './relationblock.css';

const RelationAttributeTable = ({relation}) => {
  return(
    <div>
      <span>Attributes</span>
      <Table>
        <thead>
          <tr>
            <th>#</th>
            <th>Name</th>
            <th>Type</th>
          </tr>
        </thead>

        <tbody>
          {relation.attributes.map((attribute, index) => {
            return[
              <tr key={"row"+index}>
                <th scope="row">{index+1}</th>
                <td>{attribute.name}</td>
                <td>{attribute.type}</td>
              </tr>
            ]
          })}

        </tbody>

      </Table>
    </div>
  )
}



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
            {
              //<RelationAttributeTable relation={relation}/>
            }

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
