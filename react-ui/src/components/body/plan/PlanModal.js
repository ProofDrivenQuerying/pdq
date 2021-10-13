// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import PopoutWindow from '../Popout';
import {FaClipboardList} from 'react-icons/fa';
import {Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap';
import {Tree} from 'antd';
import PlanTreeNode from './PlanTreeNode'
import Button from 'react-bootstrap/Button'

export default class PlanModal extends React.Component{
  
  constructor(props){
    super(props);
    console.log("[JsonPlan]", [this.props.plan.jsonPlan]);
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
    /*
    Converts JSONPlan object into the required shape for Tree
    */
    const toReturn = [];
    for (const command in jsonPlan) {
      let relationalTerm = jsonPlan[command];
      //skip printing rename in treeNode instead passing onto the next node
      if(relationalTerm.command === "Rename"){
        toReturn.push(...this.grow(relationalTerm.subexpression));
      }else{
        const node = {
          title: <PlanTreeNode relationalTerm={relationalTerm}/>,
          key: `${relationalTerm.command}-${command}`,
          children: this.grow(relationalTerm.subexpression)
        }
        toReturn.push(node);
      }

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
              <Tree treeData={this.state.formattedTree} height={233} defaultExpandAll/>
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
