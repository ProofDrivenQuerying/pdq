import React from 'react';
import SchemaBody from './schemas/SchemaBody';
import QueryBody from './queries/QueryBody';
import PlanBody from './plan/PlanBody';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { connect } from 'react-redux';

/**
 * Renders the app's body. Displays SchemaBody, QueryBody, and PlanBody
 * components.
 * Called from App.js.
 *
 * @author Camilo Ortiz
 */

const Body = ({selectedSchema, body, queryList}) => {
  return (
    <div>
      <Container fluid = {true}>
        <Row>
          <Col className='border'>
            <SchemaBody/>
          </Col>
          <Col className='border'>
            <QueryBody/>
          </Col>
          <Col className='border'>
            <PlanBody/>
          </Col>
        </Row>
      </Container>
    </div>
  )
}

//map states to props
const mapStatesToProps = (state) =>({
  selectedSchema: state.selectedSchema,
  queryList: state.queryList
});



export default connect(mapStatesToProps, null)(Body);
