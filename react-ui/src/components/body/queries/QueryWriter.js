// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import { FaEdit } from 'react-icons/fa';
import { connect } from 'react-redux';
import { verifyQuery } from '../../../actions/verifyQuery';
import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import { Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
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
      tooltipOpen: false
    });
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }

  submitEditedQuery(){
    this.props.verifyQuery(this.props.schemaID, this.props.numQueries, this.state.editedQuery).then(()=>{
      if(this.props.queryValidation.validQuery && !this.props.queryValidation.isErrorValidation){
        let queryToAdd = {
          id: this.props.numQueries,
          SQL: this.state.editedQuery,
          name: "query"+this.props.numQueries
        }
        this.mounted && this.props.updateSchemalistQueries(queryToAdd, this.props.schemaID);

        this.toggleQueryModal();

        this.props.setQuery(this.props.numQueries - 1);

      }else{
        this.mounted && this.setState({validQuery: this.props.queryValidation.validQuery});
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
            className="pull-right"
            onClick={()=> this.toggleQueryModal()}
          >
            <FaEdit/>
          </Button>

          <Tooltip
            trigger="hover"
            placement="top"
            isOpen={this.state.tooltipOpen && !this.state.modalQueryOpen}
            target={"QueryButton"+ this.props.schemaList.selectedSID+"_"+this.props.id}
            toggle={this.toggleTooltip}>
            Write a new SQL query
          </Tooltip>

          <Modal
            size="lg"
            isOpen={this.state.modalQueryOpen}
            toggle={this.toggleQueryModal}>
            <ModalHeader toggle={this.toggleQueryModal}>Add a Query</ModalHeader>

            <ModalBody style={{height:"calc(100vh - 200px)"}}>
              <Form>
                <Form.Group controlId={"QueryText"+this.props.schemaList.selectedSID+"_"+this.props.id}>
                  <Form.Label>Please write your query in SQL</Form.Label>
                  <Form.Control
                    as="textarea"
                    rows="20"
                    defaultValue={this.props.queryFromList.SQL}
                    placeholder={this.props.queryFromList.SQL}
                    onChange={e => this.setState({editedQuery: e.target.value})}/>
                </Form.Group>
              </Form>

              {this.state.validQuery ? null :
                <Alert
                  color="danger"
                  style={{margin:".5rem"}}
                >
                This query is not valid.
                </Alert>}
              {this.props.queryValidation.isErrorValidation ?
                <Alert
                  color="danger"
                  style={{margin:".5rem"}}
                >
                There was an error submitting your query.
                </Alert>
                :
                null}

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
