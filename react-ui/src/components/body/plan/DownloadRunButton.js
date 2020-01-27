import React from "react";
import { FaDownload } from 'react-icons/fa';
import Helpers from "../../../actions/helpers.js";
import {
        Tooltip
} from 'reactstrap';

import Button from 'react-bootstrap/Button';
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';


export default class DownloadRunButton extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      tooltipOpen: false
    };
    this.toggleTooltip = this.toggleTooltip.bind(this);
  }

  downloadRun(schemaID, queryID, SQL, userID){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");
    Helpers.httpRequest(
      `/downloadRun/`+schemaID+`/`+queryID+`/`+simpleSQL,
      "get"
    )// 1. Convert the data into 'blob'
     .then((response) => response.blob())
     .then((blob) => {
       // 2. Create blob link to download
       const url = window.URL.createObjectURL(new Blob([blob]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', `PDQ_run_schema`+schemaID+`_query`+queryID+`.csv`);
       // 3. Append to html page
       document.body.appendChild(link);
       // 4. Force download
       link.click();
       // 5. Clean up and remove the link
       link.parentNode.removeChild(link);
     })
      .catch((error) => {
           console.log(error);
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
      <Container>
        <Row>
          <Col xs lg="2">
            <Button
              id={"downloadRun"+this.props.schemaID+this.props.id}
              variant="link"
              disabled={!this.props.plan.runnable ||
                         this.props.planRun.planRun === null ||
                         this.props.planRun.schemaID !== this.props.schemaID ||
                         this.props.planRun.queryID !== this.props.queryID}
              onClick={(e) => this.downloadRun(
                this.props.schemaID,
                this.props.queryID,
                this.props.SQL
              )}>
              <FaDownload/>
              </Button>
            </Col>
          </Row>
        </Container>

        <Tooltip
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"downloadRun"+this.props.schemaID+this.props.id}
          toggle={this.toggleTooltip}>
          Download full run as .csv
        </Tooltip>
      </div>
    )
  }
}
