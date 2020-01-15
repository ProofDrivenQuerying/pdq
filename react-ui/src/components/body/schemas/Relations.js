import React from 'react';
import PopoutWindow from '../Popout';
import Button from 'react-bootstrap/Button';
import { Table,
         Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
} from 'reactstrap';
import DropdownButton from 'react-bootstrap/DropdownButton';
import Dropdown from 'react-bootstrap/Dropdown';


export default class Relations extends React.Component{

  componentWillMount(){
    this.props.getRelations(this.props.schemaFromList.id);
  }

  render(){
    return(
      <div>
      { this.props.relationList.relationList.relations != null ?
        <DropdownButton
          id="dropdown-relations"
          title="Relations">

          {this.props.relationList.relationList.relations.map((relation, index)=>{
            return(
              <RelationsModal key={"relation"+index} relation={relation}/>
            )
          })}
        </DropdownButton>
      :
        null
      }
      </div>
    )
  }
}

//nested modal class that displays each relation's information
 class RelationsModal extends React.Component {
   constructor(props) {
     super(props);
     this.state = {
       modal: false
     };

     this.toggle = this.toggle.bind(this);
   }

   toggle() {
     this.setState(prevState => ({
       modal: !prevState.modal
     }));
   }

   render() {
     const popoutContent = (relation) => (
       <div>
          <header>{relation.name}</header>
         <RelationAttributeTable relation={relation}/>
         <RelationAccessTable relation={relation}/>
       </div>
     );
     return (
       <div>
         <Dropdown.Item
            onClick={this.toggle}
          >
              {this.props.relation.name}
          </Dropdown.Item>

         <Modal
          size='lg'
          isOpen={this.state.modal}
          toggle={this.toggle}>

           <ModalHeader toggle={this.toggle}>
             {this.props.relation.name}
             <PopoutWindow
              content={popoutContent(this.props.relation)}
              title={"Relations"}
              />
           </ModalHeader>

           <ModalBody>
              <RelationAttributeTable relation={this.props.relation}/>
              <RelationAccessTable relation={this.props.relation}/>
           </ModalBody>

           <ModalFooter>
             <Button
                variant="secondary"
                onClick={this.toggle}>Cancel</Button>
           </ModalFooter>
         </Modal>
       </div>
     );
   }
 }

//table for displaying each relation's attributes
const RelationAttributeTable = ({relation}) => {
  return(
    <div>
      <h5>Attributes</h5>
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

//table for displaying each relation's access methods
const RelationAccessTable = ({relation}) => {
  return(
    <div>
    {relation.accessMethods.length > 0 ?
      <div>
        <h5>Access Methods</h5>
        <Table>
          <thead>
            <tr>
              <th>#</th>
              <th>Name</th>
              <th>Type</th>
            </tr>
          </thead>

          <tbody>
            {relation.accessMethods.map((method, index) => {
              return[
                <tr key={"row"+index}>
                  <th scope="row">{index+1}</th>
                  <td>{method.name}</td>
                  <td>{method.type}</td>
                </tr>
              ]
            })}

          </tbody>

        </Table>
      </div>
      :
      <div>This relation has no access methods.</div>
    }
    </div>
  )
}
