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

  downloadRun(schemaID, queryID, SQL, userID){
    let simpleSQL = SQL.replace(/\n|\r|\t/g, " ");
    Helpers.httpRequest(
      `/downloadRun/`+schemaID+`/`+queryID+`/`+simpleSQL+`/`+userID,
      "get"
    )// 1. Convert the data into 'blob'
     .then((response) => response.blob())
     .then((blob) => {
       // 2. Create blob link to download
       const url = window.URL.createObjectURL(new Blob([blob]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', `PDQrun`+schemaID+`-`+queryID+`results.csv`);
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
          id={"downloadRun"+this.props.schemaID+this.props.id}
          color="link"
          disabled={!this.props.plan.runnable ||
                     this.props.planRun.planRun === null ||
                     this.props.planRun.schemaID !== this.props.schemaID ||
                     this.props.planRun.queryID !== this.props.queryID}
          style={this.props.margins ? smallButton : noStyle}
          onClick={(e) => this.downloadRun(
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
          target={"downloadRun"+this.props.schemaID+this.props.id}
          toggle={this.toggleTooltip}>
          Download full run as .csv
        </Tooltip>
      </div>
    )
  }
}
