// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import PopoutWindow from '../Popout';
import { FaClipboardList } from 'react-icons/fa';
import { Modal,
         ModalHeader,
         ModalBody,
         ModalFooter
} from 'reactstrap';
import { Tree } from 'antd';

import Button from 'react-bootstrap/Button'

export default class PlanModal extends React.Component{
  constructor(props){
    super(props);
    this.state = {
      modalOpen: false,
      formattedTree: this.grow([this.props.plan.jsonPlan])
    };
    this.toggle = this.toggle.bind(this);
  }

  toggle(){
    this.setState({
      modalOpen: !this.state.modalOpen
    })
  }

  grow(jsonPlan) {
    console.log(jsonPlan)
    /*
    Converts JSONPlan object into the required shape for Tree
    */
    const toReturn = [];
    for (const command in jsonPlan) {
      console.log(jsonPlan[command])
      const node = {
        title: jsonPlan[command].command,
        key: `${jsonPlan[command].command}-${jsonPlan[command].inputAttributes}-${jsonPlan[command].outputAttributes}`,
        children: this.grow(jsonPlan[command].subexpression)
      }
      toReturn.push(node);
    }
    return toReturn;
  }

  render(){
    const planInfoContent = (name, plan, planStringStyle) =>(
      <div>
        <header>{name}</header>
          {plan ?
          (<div>
            <i>Found an optimal {plan.runnable ? "runnable":null} plan
              in {plan.planTime} seconds.</i>

            <span>
              {plan.jsonPlan}
            </span>
           </div>)
          :
          (null)}
      </div>
    );
    
    console.log(this.stateformattedTree)
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
            {this.props.plan ?
            (
              // <span style={{ display: "flex", flexDirection: "column",
              //   overflowY: "scroll", whiteSpace: "pre-wrap", height: "calc(100vh - 300px)",
              //   overflowWrap: 'break-word'}}>
              //     Check the console!
              // </span>
              <Tree treeData={this.state.formattedTree} height={233}/>
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
