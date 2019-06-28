import React from 'react';
import { FaPencilAlt } from 'react-icons/fa';
import { Button,
         Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
         Form,
         FormGroup,
         Input
} from 'reactstrap';

export default class EditQueryModal extends React.Component{
  constructor(props){
    super(props);
    this.toggleQueryModal = this.toggleQueryModal.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
      this.state = {
        modalQueryOpen: false,
        tooltipOpen: false
      };
  }

  toggleQueryModal(){
    this.setState({
      modalQueryOpen: !this.state.modalQueryOpen
    });
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }


  render(){
    return(
      <div>
        <Button
          id={"QueryButton"+ this.props.selectedSchema.id}
          color={"link"}
          onClick={()=> this.toggleQueryModal()}
        >
          <FaPencilAlt/>
        </Button>

        <Tooltip
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"QueryButton"+ this.props.selectedSchema.id}
          toggle={this.toggleTooltip}>
          Edit Query as SQL
        </Tooltip>

        <Modal
          size={"lg"}
          isOpen={this.state.modalQueryOpen}
          toggle={this.toggleQueryModal}>
          <ModalHeader toggle={this.toggleQueryModal}>Edit Query</ModalHeader>

          <ModalBody style={{height:"calc(100vh - 200px)"}}>
            <Form>
              <FormGroup>
                <Input
                  type="textarea"
                  name="text"
                  id="queryText"
                  placeholder={this.props.queryList.queryList.SQL}
                  style={{height:"calc(100vh - 200px - 2rem)"}} />
              </FormGroup>
            </Form>
          </ModalBody>

          <ModalFooter>
            <Button
              color="primary"
              onClick={this.toggleQueryModal}>Submit</Button>{' '}
            <Button
              color="secondary"
              onClick={this.toggleQueryModal}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}
