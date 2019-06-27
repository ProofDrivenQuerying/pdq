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

  downloadPlan(id){
    Helpers.httpRequest(
      `/downloadPlan/`+id,
      "get"
    )// 1. Convert the data into 'blob'
     .then((response) => response.blob())
     .then((blob) => {
       // 2. Create blob link to download
       const url = window.URL.createObjectURL(new Blob([blob]));
       const link = document.createElement('a');
       link.href = url;
       link.setAttribute('download', `PDQplan`+id+`.xml`);
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
          id={"downloadPlan"+this.props.selectedSchema.id+this.props.id}
          color="link"
          disabled={this.props.plan === null ||
                     this.props.plan.id !== this.props.selectedSchema.id}
          style={this.props.margins ? smallButton : noStyle}
          onClick={(e) => this.downloadPlan(this.props.selectedSchema.id)}>
          <FaDownload/>
        </Button>

        <Tooltip
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"downloadPlan"+this.props.selectedSchema.id+this.props.id}
          toggle={this.toggleTooltip}>
          Download plan as .xml
        </Tooltip>
      </div>
    )
  }
}
