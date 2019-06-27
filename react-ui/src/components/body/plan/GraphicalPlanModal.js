import React from "react";
import PDQTree from "./PDQTree";
import { FaRoute } from 'react-icons/fa';
import { Button,
         Modal,
         ModalHeader,
         ModalBody,
         ModalFooter
} from 'reactstrap';

/**
 * The modal in which the plan graph is displayed.
 *
 */

export default class GraphicalPlanModal extends React.Component {
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
        <Button
           outline color="secondary"
           style={this.props.bigButton}
           onClick={() => this.toggle()}>
               {this.props.name} <FaRoute/>
         </Button>

        <Modal
           isOpen={this.state.modal}
           toggle={this.toggle}
           size="lg">
          <ModalHeader toggle={this.toggle}>Graphical Plan</ModalHeader>

          <ModalBody>
          {this.props.graphicalPlan != null ?
           <div style={{display:"flex", alignItems:"center", justifyContent:"center"}}>
            <PDQTree
               data={this.props.graphicalPlan}
               width={750}
               height={600}/>
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
