import React from 'react';
import { connect } from 'react-redux';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import ListGroup from 'react-bootstrap/ListGroup';

// import Button from 'react-bootstrap/Button';
// import { FaTrashAlt } from 'react-icons/fa';


/**
 * SchemaName returns a button for given schema.
 * This file also contains the nested modal components of the selected schema's
 * relation list.
 *
 * @author Camilo Ortiz
 */

 class QueryItem extends React.Component{
   render(){
     return(
       <div key={this.props.queryFromList.id}>
       {
         /*
          First check whether there is a selected Query.
          If there isn't, return null.

          Next, there is a conditional. If the query's id is
          the user selected query, display it in blue.
         */
       }
       {this.props.schemaList.selectedQID != null ?
            <div className='my-2'>
              {this.props.schemaList.selectedQID === this.props.queryFromList.id ?
                  <ListGroup.Item
                  id = {this.props.queryFromList.id}
                  variant={ 'primary'}
                  >
                    <Container>
                      <Row>
                        <Col xs={7} className="my-2">
                          {this.props.queryFromList.name}
                        </Col>

                        <Col>
                        { //Delete button not implemented in backend yet:

                          // this.props.queryFromList.id === 0 ?
                          // null
                          // :
                          // <Button
                          //   variant="link"
                          //   onClick={(e)=> {
                          //     this.props.removeQuery(this.props.schemaList.selectedSID, this.props.queryFromList.id)
                          //     this.props.setQuery(0)
                          //   }}
                          //   >
                          //   <FaTrashAlt/>
                          // </Button>
                        }
                        </Col>
                    </Row>
                  </Container>
                </ListGroup.Item>
                :
                <ListGroup.Item
                id = {this.props.queryFromList.id}
                variant={'outline-secondary'}
                action
                onClick={(e) => this.props.setQuery(this.props.queryFromList.id)}
                >
                  <Container>
                    <Row>
                      <Col xs={7} className="my-2">
                        {this.props.queryFromList.name}
                      </Col>

                      <Col>
                      { //Delete button not implemented in backend yet:

                        // this.props.queryFromList.id === 0 ?
                        // null
                        // :
                        // <Button
                        //   variant="link"
                        //   onClick={(e)=> {
                        //     this.props.removeQuery(this.props.schemaList.selectedSID, this.props.queryFromList.id)
                        //     this.props.setQuery(0)
                        //   }}
                        //   >
                        //   <FaTrashAlt/>
                        // </Button>
                      }
                      </Col>
                  </Row>
                </Container>
              </ListGroup.Item>
              }
          </div>
          :
          null
        }
      </div>
     )
   }
 }

 //map states to props
 const mapStatesToProps = (state) =>({
   schemaList: state.schemaList
 });

 const mapDispatchToProps = (dispatch) =>({
   setQuery: (id) => dispatch({ type: 'SET_Q_ID', id: id}),

   removeQuery: (schemaID, queryID) => dispatch({
     type: 'REMOVE_SCHEMALISTQUERY',
     schemaID: schemaID,
     queryID: queryID
   })
 });

export default connect(mapStatesToProps, mapDispatchToProps)(QueryItem);
