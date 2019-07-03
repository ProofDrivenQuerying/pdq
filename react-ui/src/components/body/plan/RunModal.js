import React from "react";
import { FaTable } from 'react-icons/fa';
import PopoutWindow from '../Popout';
import DownloadRunButton from './DownloadRunButton';
import { Button,
         Modal,
         ModalHeader,
         ModalBody,
         Table,
         ModalFooter,
} from 'reactstrap';

/**
 * Contains the run's table modal and table components. Handles opening and
 * displaying the run's modal.
 *
 * @author Camilo Ortiz
 */


 export default class RunModal extends React.Component {
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

     const popoutContent = (planRun) =>(
       <div>
         {planRun != null ?
           <div>
             <div style={{display:"flex", flexDirection:"row",
                         justifyContent:"space-between"}}>

               <i>{planRun.tupleCount} {" "}
               {planRun.tupleCount > 1 ? "tuples" : "tuple" } found
               in {planRun.runTime} seconds.

               {planRun.table.data.length < planRun.table.dataSize ?
                 <span>{" "}
                 Displaying {planRun.table.data.length} of
                 {" "}{planRun.table.dataSize}
                 {" "} total tuples.
                 </span>
                 :
                 null
               }
               </i>
             </div>

             <RunTable planRun={planRun}/>

           </div>
          :
          null}
        </div>
     );

     return (
       <div>
         <Button
          outline color="secondary"
          disabled={!this.props.plan.runnable ||
                     this.props.planRun.planRun === null ||
                     this.props.planRun.schemaID !== this.props.schemaID ||
                     this.props.planRun.queryID !== this.props.queryID
                   }
          style={this.props.bigButton}
          onClick={() => this.toggle()}>

          {this.props.name} <FaTable/>

          </Button>

         <Modal
          isOpen={this.state.modal}
          toggle={this.toggle}
          size="lg">
           <ModalHeader toggle={this.toggle}>
             Plan{this.props.schemaID} run information
           </ModalHeader>

           <ModalBody>
           {this.props.planRun.planRun != null ?
             <div>
               <div style={{display:"flex", flexDirection:"row",
                           justifyContent:"space-between"}}>

                 <i>{this.props.planRun.planRun.tupleCount} {" "}
                 {this.props.planRun.planRun.tupleCount > 1 ? "tuples" : "tuple" } found
                 in {this.props.planRun.planRun.runTime} seconds.

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
                  <div>
                   <DownloadRunButton
                     SQL={this.props.SQL}
                     queryID={this.props.queryID}
                     schemaID={this.props.schemaID}
                     plan={this.props.plan}
                     planRun={this.props.planRun}
                     margins={false}
                     id={2}
                    />

                    <PopoutWindow
                      title="Run Information"
                      content={popoutContent(this.props.planRun.planRun)}
                    />
                  </div>
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

 const RunTable = ({planRun}) => {
   return(
     <div>
       <Table responsive>
         <thead>
           <tr>
            <th>#</th>
            {planRun.table.header.map((head, index) => {
              return[ <th key={"runHead"+index}>{head.name}</th> ]
            })}
           </tr>
         </thead>

         <tbody>
           {planRun.table.data.map((dataPoint, index) => {
             return[
               <tr key={"runRow"+index}>
                 <th scope="row">{index+1}</th>
                 {dataPoint.values.map((value, index)=>{
                   return[ <td key={"runRowValue"+index}>{value}</td> ]
                 })}
               </tr>
             ]
           })}

         </tbody>

       </Table>
     </div>
   )
 }
