//react
import React from 'react';
//reactstrap
import { Button, Form, FormGroup, Label, Input, FormText,
          Modal, ModalHeader, ModalBody, ModalFooter} from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import { plan } from "../../../actions/plan.js";
//css
import './planbody.css';
//icons
import { FaCog, FaRegMap } from 'react-icons/fa';

/**
 * Renders the plan button and the associated schema's plan when clicked
 *
 * @author Camilo Ortiz
 */

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

const PlanBody = ({selectedSchema, plan, getPlan}) => {
  return(
    <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
        boxShadow: "0 0 5px 2px #E0E0E0", width: "43rem"}}>

      <header className='body-name-plan'>
        Plan
      </header>

      <PlanPropertyModal/>




      {selectedSchema.selectedSchema != null ?
        <div>
          <div className='plan'>

            {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
                <div style={{whiteSpace:"normal", width:"85%",
                              overflowX:"scroll", padding:"1rem, 1rem, 1rem, 2rem"}}>
                  {Object.keys(plan.plan)[0]}

                  {
                    // <br/>
                    // Cost: {Object.values(plan.plan)[0].cost}
                    // <br/>
                    // Value: {Object.values(plan.plan)[0].value}
                  }

                </div>
                :
                null}

          </div>
          <Button color="secondary" block
                  style={{padding: "0, 2rem, 0, 1rem"}}
                  onClick={(e) => getPlan(selectedSchema.selectedSchema.id)}>
            Plan
          </Button>
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
    plan: state.plan
  })
}

//map actions to props
const mapDispatchToProps = (dispatch) =>({
  getPlan: (id) => dispatch(plan(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(PlanBody);
