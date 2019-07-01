import React from 'react';
import { connect } from 'react-redux';
import { getRelations } from '../../../actions/getRelations';
import { getDependencies } from '../../../actions/getDependencies';
import RelationsModal from './RelationsModal';
import DependenciesModal from './DependenciesModal';
import './schemaname.css';
import { Button, ButtonGroup } from 'reactstrap';

/**
 * SchemaName returns a button for given schema.
 * This file also contains the nested modal components of the selected schema's
 * relation list.
 *
 * @author Camilo Ortiz
 */

 class SchemaName extends React.Component{
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
       <div
         className="schema-name-holder"
         key={this.props.schemaFromList.id}>

       {this.props.schemaList.selectedSID != null &&
           this.props.schemaFromList.id === this.props.schemaList.selectedSID ?

         <div style={{display: "flex", width:"100%"}}>
           <ButtonGroup style={{width:"100%"}}>
             <Button
               color="primary"
               id = {this.props.schemaFromList.id}
               block
              >
               <span className="schema-name">
                 {this.props.schemaFromList.name}
               </span>
              </Button>

              <RelationsModal
                getRelations={this.props.getRelations}
                relationList = {this.props.relationList}
                schemaFromList = {this.props.schemaFromList}
                color={"link"}/>

              <DependenciesModal
                schemaFromList = {this.props.schemaFromList}
                getDependencies = {this.props.getDependencies}
                dependencyLists = {this.props.dependencyLists}
                color={"link"}/>

            </ButtonGroup>
         </div>

         :

         <div style={{display: "flex", width:"100%"}}>
          <ButtonGroup style={{width:"100%"}}>
             <Button
               outline color="secondary"
               id = {this.props.schemaFromList.id}
               block
               onClick={(e) => {
                 this.props.setSchema(this.props.schemaFromList.id);
                 this.props.setQuery(0);
                 }}>
               <span>
                 <span className="schema-name">
                   {this.props.schemaFromList.name}
                 </span>
               </span>
             </Button>
            </ButtonGroup>
         </div>
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

export default connect(mapStatesToProps, mapDispatchToProps)(SchemaName);
