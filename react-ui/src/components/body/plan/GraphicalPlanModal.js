import React from "react";
import PDQTree from "./PDQTree";
import PopoutWindow from '../Popout';
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
      modal: false,
    };

    this.toggle = this.toggle.bind(this);
  }

  toggle() {
    this.setState(prevState => ({
      modal: !prevState.modal
    }));
  }

  render() {
    const graphicalPlanContent = (graphicalPlan) => (
      <div>
        <header>Graphical Plan</header>
        <div style={{
          display:"flex",
          alignItems:"center",
          justifyContent:"center",
          overflow: "hidden"
        }}>
         <PDQTree
            data={graphicalPlan}
            width={750}
            height={370}/>
        </div>
      </div>
    )

    return (
      <div>
        <Button
           outline color="secondary"
           style={this.props.bigButton}
           onClick={() => this.toggle()}>
               {this.props.name} <FaRoute/>
         </Button>

        <Modal
           id={"GraphicalPlanModalBody"}
           isOpen={this.state.modal}
           toggle={this.toggle}
           size="lg">
          <ModalHeader toggle={this.toggle}>
            Graphical Plan
            <PopoutWindow
              title={"Graphical Plan"}
              content={graphicalPlanContent(this.props.graphicalPlan)}
              options={{
                width: "800px",
                height: "600px"
              }}/>

          </ModalHeader>

          <ModalBody
          style={{maxHeight: "calc(100vh - 200px)"}}>
          {this.props.graphicalPlan != null && this.state.modal ?
           <div style={{display:"flex", alignItems:"center", justifyContent:"center"}}>
            <PDQTree
               data={this.props.graphicalPlan}
               />
           </div>
           : null}
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
