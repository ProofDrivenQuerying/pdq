import React from 'react';
import { connect } from 'react-redux';
import { getRelations } from '../../../actions/getRelations';
import { getDependencies } from '../../../actions/getDependencies';
import { getQueries } from '../../../actions/getQueries';
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
       <div
         className="schema-name-holder"
         key={this.props.schemaFromList.id}>

       {this.props.selectedSchema.selectedSchema != null &&
           this.props.schemaFromList.id === this.props.selectedSchema.id ?

         <div style={{display: "flex", width:"100%"}}>
           <ButtonGroup style={{width:"100%"}}>
             <Button
               color="primary"
               id = {this.props.schemaFromList.id}
               block
               onClick={(e) => this.setSchema_getQueries(
                       this.props.schemaFromList,
                       this.props.schemaFromList.id
                     )}
              >
               <span className="schema-name">
                 {this.props.selectedSchema.selectedSchema.name}
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
               onClick={(e) => this.setSchema_getQueries(
                       this.props.schemaFromList, this.props.schemaFromList.id)}>
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
  ...state
});

const mapDispatchToProps = (dispatch) =>({
  setSchema: (schema, id) => dispatch({ type: 'SET', schema: schema, id: id}),
  getRelations: (id) => dispatch(getRelations(id)),
  getQueries: (id) => dispatch(getQueries(id)),
  getDependencies: (id) => dispatch(getDependencies(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(SchemaName);
