import React from 'react';
import { FaClipboardList } from 'react-icons/fa';
import { Modal,
         Button,
         ModalHeader,
         ModalBody,
         ModalFooter
} from 'reactstrap';

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
    let planStringStyle = {
      position: "relative",
      marginRight: "2rem",
      marginLeft: "2rem",
      display: "flex",
      flexDirection: "column",
      overflowY: "scroll",
      whiteSpace: "pre-wrap",
      fontSize: "1rem",
      height: "calc(100vh - 300px)"
    }
    return(
      <div>
        <Button
          outline color = "secondary"
          onClick={(e) => this.toggle()}
          style={this.props.bigButton}>

          {this.props.name} <FaClipboardList/>

        </Button>

        <Modal
          isOpen={this.state.modalOpen}
          toggle={this.toggle}
          size="lg">

          <ModalHeader toggle={this.toggle}>
            Plan{this.props.id}
          </ModalHeader>

          <ModalBody style={{maxHeight: "calc(100vh - 200px)"}}>
            {this.props.plan != null ?
            (<div>
              <i>Found an optimal {this.props.plan.runnable ? "runnable":null} plan
                in {this.props.plan.planTime} seconds.</i>

              <div style={{width:"100%"}}>
                <hr style={{color: "rgb(250,250,250)", backgroundColor: "rgb(250,250,250)", height: 0.1}}/>
              </div>

              <span style={planStringStyle}>{this.props.plan.bestPlan}</span>
             </div>)
            :
            (null)}
          </ModalBody>

          <ModalFooter>
            <Button
               color="secondary"
               onClick={this.toggle}>Cancel</Button>
          </ModalFooter>

        </Modal>

      </div>
    )
  }
}
