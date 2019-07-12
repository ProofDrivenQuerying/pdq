import React from "react";
import { FaDownload } from 'react-icons/fa';
import Helpers from "../../../actions/helpers.js";
import { Button,
        Tooltip
} from 'reactstrap';


export default class DownloadRunButton extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      tooltipOpen: false
    };
    this.toggleTooltip = this.toggleTooltip.bind(this);
  }

  downloadPlan(schemaID, queryID, SQL, userID){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");
    Helpers.httpRequest(
      `/downloadPlan/`+schemaID+`/`+queryID+`/`+simpleSQL+`/`+userID,
      "get"
    )// 1. Convert the data into 'blob'
     .then((response) => response.blob())
     .then((blob) => {
       // 2. Create blob link to download
       const url = window.URL.createObjectURL(new Blob([blob]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', `PDQplan`+schemaID+`-`+queryID+`.xml`);
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

    let smallButton = {
      float: "left", width: "4rem", height:"4rem",
               margin:"1rem 1rem 1rem 1rem"
    }

    let noStyle = {
      float: "left"
    }

    return(
      <div>
        <Button
          id={"downloadPlan"+this.props.schemaID+this.props.id}
          color="link"
          disabled={
            this.props.plan === null ||
            this.props.plan.schemaID !== this.props.schemaID ||
            this.props.plan.queryID !== this.props.queryID
          }
          style={this.props.margins ? smallButton : noStyle}
          onClick={(e) => this.downloadPlan(
            this.props.schemaID,
            this.props.queryID,
            this.props.SQL,
            this.props.userID
          )}>
          <FaDownload/>
        </Button>

        <Tooltip
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"downloadPlan"+this.props.schemaID+this.props.id}
          toggle={this.toggleTooltip}>
          Download plan as .xml
        </Tooltip>
      </div>
    )
  }
}
