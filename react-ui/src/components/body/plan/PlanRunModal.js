import React from "react";
import { FaTable, FaDownload } from 'react-icons/fa';
import { Button,
         Modal,
         ModalHeader,
         ModalBody,
         Table,
         ModalFooter,
         Tooltip
} from 'reactstrap';

/**
 * Contains the run's table modal and table components. Handles opening and
 * displaying the run's modal.
 *
 * @author Camilo Ortiz
 */


 const RunTable = ({planRun}) => {
   return(
     <div>
       <Table responsive>
         <thead>
           <tr>
            <th>#</th>
            {planRun.table.header.map((head, index) => {
              return[
                <th key={"runHead"+index}>{head.name}</th>
              ]
            })}
           </tr>
         </thead>

         <tbody>
           {planRun.table.data.map((dataPoint, index) => {
             return[
               <tr key={"runRow"+index}>
                 <th scope="row">{index+1}</th>
                 {dataPoint.values.map((value, index)=>{
                   return[
                     <td key={"runRowValue"+index}>{value}</td>
                   ]
                 })}
               </tr>
             ]
           })}

         </tbody>

       </Table>
     </div>
   )
 }

export default class GraphicalPlanModal extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      modal: false,
      tooltipOpen: false
    };

    this.toggle = this.toggle.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
  }

  toggle() {
    this.setState(prevState => ({
      modal: !prevState.modal
    }));
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }

  render() {
    return (
      <div>
        <Button
           outline color="secondary"
           disabled={!this.props.plan.runnable ||
                      this.props.planRun.planRun === null ||
                      this.props.planRun.id !== this.props.selectedSchema.id}
           style={this.props.bigButton}
           onClick={() => this.toggle()}>
               Run results <FaTable/>
         </Button>

        <Modal
           isOpen={this.state.modal}
           toggle={this.toggle}
           size="lg">
          <ModalHeader toggle={this.toggle}>
            Plan{this.props.selectedSchema.id} run information
          </ModalHeader>

          <ModalBody>
          {this.props.planRun.planRun != null ?
            <div>
              <div style={{display:"flex", flexDirection:"row",
                          justifyContent:"space-between"}}>

                <i>{this.props.planRun.planRun.tupleCount} {" "}
                {this.props.planRun.planRun.tupleCount > 1 ? "tuples" : "tuple" } found
                in {this.props.planRun.planRun.runTime} sec.

                {this.props.planRun.planRun.table.data.length < this.props.planRun.planRun.table.dataSize ?
                  <span>{" "}
                  Displaying {this.props.planRun.planRun.table.data.length} of
                  {" "}{this.props.planRun.planRun.table.dataSize}
                  {" "} total tuples.
                  </span>
                  :
                  null
                }

                </i>


                  <Button
                    id={"downloadFromModal-"+this.props.selectedSchema.id}
                    style={{float:"right", alignSelf:"center"}}
                    color="link"
                    onClick={(e) => this.props.downloadCSV(this.props.selectedSchema.id)}>
                    <FaDownload/>
                  </Button>

                  <Tooltip
                    placement="top"
                    isOpen={this.state.tooltipOpen}
                    target={"downloadFromModal-"+this.props.selectedSchema.id}
                    toggle={this.toggleTooltip}>
                    Download full run as .csv
                  </Tooltip>

              </div>

              <RunTable planRun={this.props.planRun.planRun}/>

            </div>
           :
           null}
          </ModalBody>

          <ModalFooter>
            <Button
               color="secondary"
               onClick={this.toggle}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    );
  }
}
