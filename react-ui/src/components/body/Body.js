import React from 'react';
import Schemas from './schemas/Schemas';
import Queries from './queries/Queries';
import Plan from './plan/Plan';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import { connect } from 'react-redux';

/**
 * Renders the app's body. Displays Schemas, Queries, and Plan
 * components.
 * Called from App.js.
 *
 * @author Camilo Ortiz
 */

const Body = ({selectedSchema, body, queryList}) => {
  return (
    <div>
      <style type="text/css">
        {`
        .half {
          height: calc((100vh - 11.5rem) / 2);
          max-height: calc((100vh - 11.5rem) / 2)
          margin-bottom: 10px;
          overflow:scroll;
          -webkit-overflow-scrolling: touch;
          white-space: pre;
          x-overflow:scroll;
        }
        `}
      </style>
      <Container fluid = {true}>
        <Row>
          <Col className='border' md>
            <Schemas/>
          </Col>
          <Col className='border' md>
            <Queries/>
          </Col>
          <Col className='border' md>
            <Plan/>
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
