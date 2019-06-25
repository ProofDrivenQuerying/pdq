//react
import React from "react";
import TreeDisplay from "./TreeDisplay";
//reactstrap
import { Button, Modal, ModalHeader, ModalBody,
          ModalFooter} from 'reactstrap';
//icons
import { FaRoute } from 'react-icons/fa';

export default class GraphicalPlanModal extends React.Component {
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
    this.toggle();
  }

  render() {
    return (
      <div>
        <Button
           outline color="secondary"
           style={{float: "left", height:"4rem",
                   margin:"1rem 1rem 1rem 1rem", width: "11rem"}}
           onClick={() => this.openPlan(this.props.selectedSchema.id)}>
               Visualise plan exploration <FaRoute/>
         </Button>

        <Modal
           isOpen={this.state.modal}
           toggle={this.toggle}
           size="lg">
          <ModalHeader toggle={this.toggle}>Graphical Plan</ModalHeader>

          <ModalBody>
          {this.props.graphicalPlan != null ?
           <div style={{display:"flex", alignItems:"center", justifyContent:"center"}}>
            <TreeDisplay
               data={this.props.graphicalPlan}
               width={750}
               height={500}/>
           </div>

           :

           null}
          </ModalBody>

          <ModalFooter>
            <Button
               color="secondary"
               onClick={this.toggle}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    );
  }
}
