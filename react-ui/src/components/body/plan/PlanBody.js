//react
import React from 'react';
//components
import TreeDisplay from './TreeDisplay';
//reactstrap
import { Button, Form, FormGroup, Label, Input, FormText,
          Modal, ModalHeader, ModalBody, ModalFooter} from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import { plan } from "../../../actions/plan.js";
import { getGraphicalPlan } from "../../../actions/getGraphicalPlan.js";
//css
import './planbody.css';
//icons
import { FaCog, FaRegMap } from 'react-icons/fa';

/**
 * Renders the plan/button, graphical plan/button, and plan properties/button
 *
 * @author Camilo Ortiz
 */

 //plan properties form
 const PlanForm = ({toggle}) => {
   return(
     <Form>
      <FormGroup>
        <Label type="select">Search type</Label>
        <Input type="select" name="search_type">
          <option>Optimized</option>
        </Input>
      </FormGroup>

      <FormGroup>
        <Label type="select">Reasoning type</Label>
        <Input type="select" name="reasoning_type">
          <option>Restricted</option>
        </Input>
      </FormGroup>

      <FormGroup>
        <Label>Timeout</Label>
        <Input placeholder="Infinity"/>
      </FormGroup>

      <FormGroup>
        <Label>Blocking intervals</Label>
        <Input placeholder="N/A"/>
      </FormGroup>

      <FormGroup>
        <Label>Max. iterations</Label>
        <Input placeholder="2.14783647E9"/>
      </FormGroup>

      <FormGroup>
        <Label>Match intervals</Label>
        <Input placeholder="1"/>
      </FormGroup>

      <FormGroup>
        <Label>Cost model</Label>
        <Input type="select" name="search_type">
          <option>Blackbox</option>
        </Input>
      </FormGroup>

      <Button color="secondary" block
              style={{padding: "0, 2rem, 0, 1rem"}}
              onClick={toggle}>
        Submit
      </Button>

     </Form>
   )
 }
 //plan properties
 class PlanPropertyModal extends React.Component {
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
         <Button color="link" onClick={this.toggle}><FaCog/></Button>

         <Modal isOpen={this.state.modal} toggle={this.toggle}>
           <ModalHeader toggle={this.toggle}>Plan Properties</ModalHeader>

           <ModalBody>
              <PlanForm toggle={this.toggle}/>
           </ModalBody>

           <ModalFooter>
             <Button color="secondary" onClick={this.toggle}>Cancel</Button>
           </ModalFooter>
         </Modal>
       </div>
     );
   }
 }

//graphical plan modal!!
 class GraphicalPlanModal extends React.Component {
   constructor(props) {
     super(props);
     this.state = {
       modal: false
     };

     this.toggle = this.toggle.bind(this);
     this.openPlan = this.openPlan.bind(this);
   }

   toggle() {
     this.setState(prevState => ({
       modal: !prevState.modal
     }));
   }

   openPlan(id){
     this.props.getGraphicalPlan(id);

     this.toggle();
   }

   render() {
     return (
       <div>
         <Button onClick={() => this.openPlan(this.props.selectedSchema.id)}>Graphical Planner <FaRegMap/></Button>

         <Modal isOpen={this.state.modal} toggle={this.toggle} size="lg">
           <ModalHeader toggle={this.toggle}>Graphical Plan</ModalHeader>

           <ModalBody>
           {this.props.graphicalPlan != null ?
            <div>
             <TreeDisplay data={this.props.graphicalPlan} width={750} height={500}/>
            </div>
            :
            null}

           </ModalBody>

           <ModalFooter>
             <Button color="secondary" onClick={this.toggle}>Cancel</Button>
           </ModalFooter>
         </Modal>
       </div>
     );
   }
 }


const PlanBody = ({selectedSchema, plan, getPlan, graphicalPlan, getGraphicalPlan}) => {
  return(
    <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
        boxShadow: "0 0 5px 2px #E0E0E0", width: "43rem"}}>

      <header className='body-name-plan'>
        Plan
        <div style={{float:"right"}}>
          <PlanPropertyModal/>
        </div>
      </header>


      {selectedSchema.selectedSchema != null ?
        <div className="row">

          <div className='plan'>
            <Button color="secondary"
                    style={{padding: "0, 2rem, 0, 1rem"}}
                    onClick={(e) => getPlan(selectedSchema.selectedSchema.id)}>
              Plan <FaRegMap/>
            </Button>

            {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
                <div style={{whiteSpace:"normal",
                              overflowX:"scroll", padding:"1rem, 1rem, 1rem, 2rem",
                              height:"5rem"}}>

                  {Object.keys(plan.plan)[0]}

                </div>
                :
                null}


              <div style={{margin: "1rem, 1rem, 1rem, 1rem"}}>
                <GraphicalPlanModal graphicalPlan={graphicalPlan.graphicalPlan}
                                    selectedSchema={selectedSchema.selectedSchema}
                                    graphicalPlan={graphicalPlan.graphicalPlan}
                                    getGraphicalPlan={getGraphicalPlan}/>
              </div>

          </div>

        </div>

        :
        null
      }

    </div>
  );
}

//map states to props
const mapStatesToProps = (state) =>{
  return({
    selectedSchema: state.selectedSchema,
    plan: state.plan,
    graphicalPlan: state.graphicalPlan
  })
}

//map actions to props
const mapDispatchToProps = (dispatch) =>({
  getPlan: (id) => dispatch(plan(id)),

  getGraphicalPlan: (id) => dispatch(getGraphicalPlan(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(PlanBody);
