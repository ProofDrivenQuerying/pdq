import React from "react";
import { FaDownload } from 'react-icons/fa';
import { Button,
        Tooltip
} from 'reactstrap';


export default class DownloadCSVButton extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      tooltipOpen: false
    };
    this.toggleTooltip = this.toggleTooltip.bind(this);
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }

  render(){
    return(
      <div>
        <Button
          id={"downloadCSV-"+this.props.selectedSchema.id}
          outline color="secondary"
          disabled={!this.props.plan.plan.runnable ||
                     this.props.planRun.planRun === null ||
                     this.props.planRun.id !== this.props.selectedSchema.id}
          style={this.props.smallButton}
          onClick={(e) => this.props.downloadCSV(this.props.selectedSchema.id)}>
          <FaDownload/>
        </Button>

        <Tooltip
          placement="top"
          isOpen={this.state.tooltipOpen}
          target={"downloadCSV-"+this.props.selectedSchema.id}
          toggle={this.toggleTooltip}>
          Download full run as .csv
        </Tooltip>
      </div>
    )
  }
}
