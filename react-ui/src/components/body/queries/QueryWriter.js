import React from 'react';
import { FaEdit } from 'react-icons/fa';
import { connect } from 'react-redux';
import { verifyQuery } from '../../../actions/verifyQuery';
import Button from 'react-bootstrap/Button';
import { Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
         Form,
         FormGroup,
         Input,
         Alert
} from 'reactstrap';


class QueryWriter extends React.Component{
  constructor(props){
    super(props);
    this.toggleQueryModal = this.toggleQueryModal.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
    this.submitEditedQuery = this.submitEditedQuery.bind(this);
      this.state = {
        modalQueryOpen: false,
        tooltipOpen: false,
        editedQuery: this.props.queryFromList.SQL,
        validQuery: true,
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
    this.props.verifyQuery(this.props.schemaID, this.props.numQueries, this.state.editedQuery).then(()=>{
      if(this.props.queryValidation.validQuery){
        let queryToAdd = {
          id: this.props.numQueries,
          SQL: this.state.editedQuery,
          name: "query"+this.props.numQueries
        }
        this.mounted && this.props.updateSchemalistQueries(queryToAdd, this.props.schemaID);

        this.toggleQueryModal();

        this.props.setQuery(this.props.numQueries - 1);

      }else{
        this.mounted && this.setState({validQuery: false});
      }
    })
  }
  render(){
    
    if (!this.props.schemaList.isFetching){
      return(
        <div>
          <Button
            id={"QueryButton"+ this.props.schemaList.selectedSID+"_"+this.props.id}
            variant="link"
            onClick={()=> this.toggleQueryModal()}
          >
            <FaEdit/>
          </Button>

          <Tooltip
            trigger="hover"
            placement="top"
            isOpen={this.state.tooltipOpen}
            target={"QueryButton"+ this.props.schemaList.selectedSID+"_"+this.props.id}
            toggle={this.toggleTooltip}>
            Add new SQL query
          </Tooltip>

          <Modal
            size="lg"
            isOpen={this.state.modalQueryOpen}
            toggle={this.toggleQueryModal}>
            <ModalHeader toggle={this.toggleQueryModal}>Add Query</ModalHeader>

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
                variant="primary"
                onClick={this.submitEditedQuery}>Submit</Button>{' '}
              <Button
                variant="secondary"
                onClick={this.toggleQueryModal}>Cancel</Button>
            </ModalFooter>
          </Modal>
        </div>
      )
    }
    else{
      return null;
    }
  }
}

const mapStatesToProps = (state) =>({
  dispatch: state.dispatch,
  queryValidation: state.queryValidation,
  schemaList: state.schemaList
});

const mapDispatchToProps = (dispatch) =>({
  updateSchemalistQueries: (query, sID) => dispatch({
    type: 'UPDATE_SCHEMALIST_QUERY',
    query: query,
    id: sID }),
  verifyQuery: (schemaID, queryID, SQL) => dispatch(verifyQuery(schemaID, queryID, SQL)),
  setQuery: (id) => dispatch({ type: 'SET_Q_ID', id: id})
});

export default connect(mapStatesToProps, mapDispatchToProps)(QueryWriter);
