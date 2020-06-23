// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import PopoutWindow from '../Popout';
import { FaClipboardList } from 'react-icons/fa';
import { Modal,
         ModalHeader,
         ModalBody,
         ModalFooter
} from 'reactstrap';

import Button from 'react-bootstrap/Button'

export default class PlanInfoModal extends React.Component{
  constructor(props){
    super(props);

    this.state = {
      modalOpen: false,
    };
    this.toggle = this.toggle.bind(this);
  }

  toggle(){
    this.setState({
      modalOpen: !this.state.modalOpen
    })
  }

  render(){
    const planInfoContent = (name, plan, planStringStyle) =>(
      <div>
        <header>{name}</header>
          {plan != null ?
          (<div>
            <i>Found an optimal {plan.runnable ? "runnable":null} plan
              in {plan.planTime} seconds.</i>

            <span>
              {plan.bestPlan}
            </span>
           </div>)
          :
          (null)}
      </div>
    );

    return(
      <div>
        <div className="my-2">
          <Button
            block
            variant="primary"
            onClick={(e) => this.toggle()}>
              <div className="my-2">
                View Plan <FaClipboardList/>
              </div>
          </Button>
        </div>

        <Modal
          isOpen={this.state.modalOpen}
          toggle={this.toggle}
          size="lg">

          <ModalHeader toggle={this.toggle}>
            Plan{this.props.id}

            <PopoutWindow
              title={"Plan Information"}
              content={planInfoContent("Plan"+this.props.id, this.props.plan)}
              options={{
                width: "800px",
                height: "600px"
              }}
            />

          </ModalHeader>

          <ModalBody style={{maxHeight: "calc(100vh - 200px)"}}>
            {this.props.plan != null ?
            (
              <span style={{ display: "flex", flexDirection: "column",
                overflowY: "scroll", whiteSpace: "pre-wrap", height: "calc(100vh - 300px)",
                overflowWrap: 'break-word'}}>
                  {this.props.plan.bestPlan}
              </span>
             )
            :
            (null)}
          </ModalBody>

          <ModalFooter>
            <Button
               variant="secondary"
               onClick={this.toggle}>
                Cancel
            </Button>
          </ModalFooter>

        </Modal>

      </div>
    )
  }
}
