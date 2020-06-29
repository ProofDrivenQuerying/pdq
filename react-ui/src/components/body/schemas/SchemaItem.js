// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import { connect } from 'react-redux';
import { getRelations } from '../../../actions/getRelations';
import { getDependencies } from '../../../actions/getDependencies';
import Relations from './Relations';
import Dependencies from './Dependencies';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import ListGroup from 'react-bootstrap/ListGroup';

/**
 * SchemaName returns a button for given schema.
 * This file also contains the nested modal components of the selected schema's
 * relation list.
 *
 * @author Camilo Ortiz
 */

 class SchemaItem extends React.Component{

   render(){
     return(
       <div key={this.props.schemaFromList.id}>
       {
         /*
          First check whether we have loaded the schemas from the backend
          before rendering. If we haven't, return null.

          Next, there is a conditional. If the SchemaItem's id is
          the user selected schema, display the buttons for the relations modal
          and the dependencies modal.
         */
       }
       {this.props.schemaList.selectedSID != null ?
            <div className='my-2'>
              {this.props.schemaFromList.id === this.props.schemaList.selectedSID ?
                  <ListGroup.Item
                  id = {this.props.schemaFromList.id}
                  variant='primary'
                  >
                    <Container>
                      <Row>
                        <Col className="my-2" sm={12} md={12} lg={4}>
                          {this.props.schemaFromList.name}
                        </Col>
                        <Col xs={6} sm={6} md={6} lg={4}>
                          <Relations
                            getRelations={this.props.getRelations}
                            relationList = {this.props.relationList}
                            schemaFromList = {this.props.schemaFromList}
                            />
                        </Col>
                        <Col xs={6} sm={6} md={6} lg={4}>
                          <Dependencies
                            schemaFromList = {this.props.schemaFromList}
                            getDependencies = {this.props.getDependencies}
                            dependencyLists = {this.props.dependencyLists}
                            />
                        </Col>
                    </Row>
                  </Container>
                </ListGroup.Item>
                :
                <ListGroup.Item
                id = {this.props.schemaFromList.id}
                action
                onClick={(e) => {
                  this.props.setSchema(this.props.schemaFromList.id);
                  this.props.setQuery(0);
                  }}
                >
                  <Container>
                    <Row>
                      <Col className="my-2">
                      {this.props.schemaFromList.name}
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

//redux
const mapStatesToProps = (state) =>({
  relationList: state.relationList,
  dependencyLists: state.dependencyLists,
  schemaList: state.schemaList
});

const mapDispatchToProps = (dispatch) =>({
  setSchema: (id) => dispatch({ type: 'SET_S_ID', id: id}),
  setQuery: (id) => dispatch({ type: 'SET_Q_ID', id: id}),
  getRelations: (id) => dispatch(getRelations(id)),
  getDependencies: (id) => dispatch(getDependencies(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(SchemaItem);
