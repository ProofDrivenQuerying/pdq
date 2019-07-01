import React from 'react';
import { FaPencilAlt } from 'react-icons/fa';
import { connect } from 'react-redux';
import { verifyQuery } from '../../../actions/verifyQuery';
import { Button,
         Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
         Form,
         FormGroup,
         Input,
         Alert
} from 'reactstrap';




class AddQueryModal extends React.Component{
  constructor(props){
    super(props);
    this.toggleQueryModal = this.toggleQueryModal.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
    this.submitEditedQuery = this.submitEditedQuery.bind(this);
      this.state = {
        modalQueryOpen: false,
        tooltipOpen: false,
        editedQuery: this.props.queryFromList.SQL,
        validQuery: true
      };
    this.mounted = false;
  }

  componentDidMount(){
    this.mounted = true;
  }

  componentWillUnmount(){
    this.mounted = false;
  }

  toggleQueryModal(){
    this.setState({
      modalQueryOpen: !this.state.modalQueryOpen,
    });

  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }



  submitEditedQuery(){
    this.props.verifyQuery(this.props.schemaID, this.state.editedQuery).then(()=>{
      if(this.props.queryValidation.validQuery){
        let queryToAdd = {
          id: this.props.numQueries,
          SQL: this.state.editedQuery,
          name: "query"+this.props.numQueries
        }
        this.mounted && this.props.updateSchemalistQueries(queryToAdd, this.props.schemaID);

        this.toggleQueryModal();
      }else{
        this.mounted && this.setState({validQuery: false});
      }
    })
  }

  render(){
    console.log(this.props);
    return(
      <div>
        <Button
          id={"QueryButton"+ this.props.schemaID+"_"+this.props.id}
          color={"link"}
          onClick={()=> this.toggleQueryModal()}
        >
          <FaPencilAlt/>
        </Button>

        <Tooltip
          trigger="hover"
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"QueryButton"+ this.props.schemaID+"_"+this.props.id}
          toggle={this.toggleTooltip}>
          Edit query as SQL
        </Tooltip>

        <Modal
          size="lg"
          isOpen={this.state.modalQueryOpen}
          toggle={this.toggleQueryModal}>
          <ModalHeader toggle={this.toggleQueryModal}>Edit Query</ModalHeader>

          <ModalBody style={{height:"calc(100vh - 200px)"}}>
            <Form>
              <FormGroup>
                <Input
                  type="textarea"
                  name="queryText"
                  id="queryText"
                  placeholder={this.props.queryFromList.SQL}
                  style={{height:"calc(100vh - 200px - 6rem)"}}
                  value={this.state.editedQuery}
                  onChange={e => this.setState({editedQuery: e.target.value})}/>
              </FormGroup>
            </Form>

            {this.state.validQuery ? null :
              <Alert
                color="danger"
                style={{margin:".5rem"}}
              >
              This query is not valid.
              </Alert>}

          </ModalBody>

          <ModalFooter>
            <Button
              color="primary"
              onClick={this.submitEditedQuery}>Submit</Button>{' '}
            <Button
              color="secondary"
              onClick={this.toggleQueryModal}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}

const mapStatesToProps = (state) =>({
  dispatch: state.dispatch,
  queryValidation: state.queryValidation,
});

const mapDispatchToProps = (dispatch) =>({
  updateSchemalistQueries: (query, sID) => dispatch({
    type: 'UPDATE_SCHEMALIST_QUERY',
    query: query,
    id: sID }),
  verifyQuery: (id, SQL) => dispatch(verifyQuery(id, SQL))
});

export default connect(mapStatesToProps, mapDispatchToProps)(AddQueryModal);
