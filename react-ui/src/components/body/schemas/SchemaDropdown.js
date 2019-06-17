//react
import React from 'react';
//reactstrap
import { Button, ButtonDropdown, DropdownToggle, DropdownMenu, Table,
        DropdownItem, Modal, ModalHeader, ModalBody, ModalFooter } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import setSchema from '../../../actions/setSchema';
import { getRelations } from '../../../actions/getRelations';
import { getQueries } from '../../../actions/getQueries';
//css
import './schemadropdown.css';
//img
import moreDots from '../../../img/threeDots.png';

/**
 * SchemaDropdown returns a button for the schemaFromList.
 *
 * Conditional: if the schema name's id matches the one from schemaList, it
 * gets highlighted
 *
 * Highlighted buttons also get a dropdown that opens a number of modals for more info
 *
 * @author Camilo Ortiz
 */

//table for displaying each relation's attributes
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
 //table for displaying each relation's access methods
 const RelationAccessTable = ({relation}) => {
   return(
     <div>
     {relation.accessMethods.length > 0 ?
       <div>
         <span>Access Methods</span>
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

//nested modal class that displays each relation's information
 class NestedRelationsModal extends React.Component {
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
     return (
       <div>
         <Button color="link" onClick={this.toggle}>{this.props.relation.name}</Button>

         <Modal isOpen={this.state.modal} toggle={this.toggle}>
           <ModalHeader toggle={this.toggle}>{this.props.relation.name}</ModalHeader>
           <ModalBody>
            <RelationAttributeTable relation={this.props.relation}/>
            <RelationAccessTable relation={this.props.relation}/>
           </ModalBody>

           <ModalFooter>
             <Button color="secondary" onClick={this.toggle}>Cancel</Button>
           </ModalFooter>
         </Modal>
       </div>
     );
   }
 }

//main component class, renders each schema name and handles opening modals
class SchemaDropdown extends React.Component{
  constructor(props){
    super(props);
    this.toggle = this.toggle.bind(this);
    this.toggleRelationsModal = this.toggleRelationsModal.bind(this);
      this.state = {
        dropdownOpen: false,
        modalRelationsOpen: false
      };
    }

    toggle() {
      this.setState({
        dropdownOpen: !this.state.dropdownOpen
      });
    }

    toggleRelationsModal(){
      this.setState({
        modalRelationsOpen: !this.state.modalRelationsOpen
      });
    }

  setSchema_getQueries(schemaFromList, id){
     this.props.setSchema(schemaFromList, id);
     this.props.getQueries(id);
   }

   openRelations(id){
     this.props.getRelations(id);

     this.toggleRelationsModal();
   }

  render(){
    return(
      <div className="schema-name-holder" key={this.props.schemaFromList.id}>

      {this.props.selectedSchema.selectedSchema != null &&
          this.props.schemaFromList.id === this.props.selectedSchema.id ?

        <div style={{display: "flex"}}>
          <Button color="primary" id = {this.props.schemaFromList.id} block
                  onClick={(e) => this.setSchema_getQueries(
                    this.props.schemaFromList, this.props.schemaFromList.id)}>
            <span>
                <span className="schema-name">
                  {this.props.selectedSchema.selectedSchema.name}
                </span>
            </span>
          </Button>

          <ButtonDropdown color="link" isOpen={this.state.dropdownOpen}
                          toggle={this.toggle} direction="right">
            <DropdownToggle color="link">
              <img src={moreDots} className="threeDots" alt="more"/>
            </DropdownToggle>

            <DropdownMenu>
              <DropdownItem onClick={()=> this.openRelations(this.props.schemaFromList.id)}>
                Relations
              </DropdownItem>

              {
                //relations modal
              }
              <Modal isOpen={this.state.modalRelationsOpen} toggle={this.toggleRelationsModal}>
                <ModalHeader toggle={this.toggleRelationsModal}>Relations</ModalHeader>
                <ModalBody>
                { this.props.relationList.relationList.relations != null ?
                  <ul>
                  {this.props.relationList.relationList.relations.map((relation, index)=>{
                    return(
                      <li key={"relation"+index}>
                        <NestedRelationsModal relation={relation}/>
                      </li>
                    )
                  })}
                </ul>
                :
                null
                }
                </ModalBody>

                <ModalFooter>
                  <Button color="secondary" onClick={this.toggleRelationsModal}>Cancel</Button>
                </ModalFooter>
              </Modal>

              {
                //<DropdownItem>Dependencies</DropdownItem>
              }
              <DropdownItem>Queries</DropdownItem>

            </DropdownMenu>
          </ButtonDropdown>


        </div>

        :

        <div style={{display: "flex"}}>
          <Button outline color="secondary" id = {this.props.schemaFromList.id} block
                  onClick={(e) => this.setSchema_getQueries(
                    this.props.schemaFromList, this.props.schemaFromList.id)}>
            <span>
              <span className="schema-name">
                {this.props.schemaFromList.name}
              </span>
            </span>
          </Button>
        </div>
      }
      </div>
    )
  }
}

//map states to props
const mapStatesToProps = (state) =>({
  ...state
});


//map actions to props
const mapDispatchToProps = (dispatch) =>({
  setSchema: (schema, id) => dispatch({ type: 'SET', schema: schema, id: id}),
  getRelations: (id) => dispatch(getRelations(id)),
  getQueries: (id) => dispatch(getQueries(id))
});

//connect component to redux store
export default connect(mapStatesToProps, mapDispatchToProps)(SchemaDropdown);
