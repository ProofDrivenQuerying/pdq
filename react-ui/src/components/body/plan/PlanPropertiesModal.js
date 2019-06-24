//react
import React from 'react';
//reactstrap
import { Button, Form, FormGroup, Label, Input, Modal, ModalHeader, ModalBody,
          ModalFooter} from 'reactstrap';
//icons
import { FaCog } from 'react-icons/fa';

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

export default class PlanPropertiesModal extends React.Component {
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
