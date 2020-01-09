import React from 'react';
import { connect } from 'react-redux';
import { getRelations } from '../../../actions/getRelations';
import { getDependencies } from '../../../actions/getDependencies';
import RelationsModal from './RelationsModal';
import DependenciesModal from './DependenciesModal';
import Button from 'react-bootstrap/Button'
import ButtonGroup from 'react-bootstrap/ButtonGroup';
import ListGroup from 'react-bootstrap/ListGroup';


/**
 * SchemaName returns a button for given schema.
 * This file also contains the nested modal components of the selected schema's
 * relation list.
 *
 * @author Camilo Ortiz
 */

 class SchemaItem extends React.Component{
   constructor(props){
     super(props);
     this.toggleRelationsModal = this.toggleRelationsModal.bind(this);
       this.state = {
         modalRelationsOpen: false
       };
     }

     toggleRelationsModal(){
       this.setState({
         modalRelationsOpen: !this.state.modalRelationsOpen
       });
     }

    openRelations(id){
      this.props.getRelations(id);

      this.toggleRelationsModal();
    }


   render(){
     return(
       <div key={this.props.schemaFromList.id}>
       {
         /*
          First check whether we have loaded the schemas from the backend
          before rendering. If we haven't, return null.

          Next, there is a conditional. If the SchemaItem's id is
          the user selected schema, display the buttons for the relations modal
          and the dependencies modal.
         */
       }
       {this.props.schemaList.selectedSID != null ?
            <div>
              {this.props.schemaFromList.id === this.props.schemaList.selectedSID ?
                <ListGroup horizontal variant='flush'>
                  <ListGroup.Item active id = {this.props.schemaFromList.id} block>
                    {this.props.schemaFromList.name}
                  </ListGroup.Item>

                  <ListGroup.Item action>
                    <RelationsModal
                      getRelations={this.props.getRelations}
                      relationList = {this.props.relationList}
                      schemaFromList = {this.props.schemaFromList}
                      />
                  </ListGroup.Item>

                  <ListGroup.Item action>
                    <DependenciesModal
                      schemaFromList = {this.props.schemaFromList}
                      getDependencies = {this.props.getDependencies}
                      dependencyLists = {this.props.dependencyLists}
                      color={"link"}/>
                  </ListGroup.Item>
                </ListGroup>
                :
                <ListGroup.Item
                id = {this.props.schemaFromList.id}
                action
                onClick={(e) => {
                  this.props.setSchema(this.props.schemaFromList.id);
                  this.props.setQuery(0);
                  }}
                >
                  {this.props.schemaFromList.name}
                </ListGroup.Item>
              }
          </div>
          :
          null
        }
      </div>
     )
   }
 }

//redux
const mapStatesToProps = (state) =>({
  relationList: state.relationList,
  dependencyLists: state.dependencyLists,
  schemaList: state.schemaList
});

const mapDispatchToProps = (dispatch) =>({
  setSchema: (id) => dispatch({ type: 'SET_S_ID', id: id}),
  setQuery: (id) => dispatch({ type: 'SET_Q_ID', id: id}),
  getRelations: (id) => dispatch(getRelations(id)),
  getDependencies: (id) => dispatch(getDependencies(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(SchemaItem);
