import React from 'react';
import GraphicalPlanModal from './GraphicalPlanModal';
import PlanRunModal from './PlanRunModal';
import DownloadCSVButton from './DownloadCSVButton';
import { Button } from 'reactstrap';
import { connect } from 'react-redux';
import { plan } from "../../../actions/getPlan.js";
import { runPlan } from "../../../actions/runPlan.js";
import Helpers from "../../../actions/helpers.js";
import './planbody.css';
import { FaRegMap,
         FaPlay,
         FaDownload,
         FaRoute,
         FaTable
} from 'react-icons/fa';

/**
 * Renders the plan/button, graphical plan/button, and plan properties/button
 *
 * @author Camilo Ortiz
 */

 function downloadCSV(id){
   Helpers.httpRequest(
     `/downloadRun/`+id,
     "get"
   )// 1. Convert the data into 'blob'
    .then((response) => response.blob())
    .then((blob) => {
      // 2. Create blob link to download
      const url = window.URL.createObjectURL(new Blob([blob]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `PDQrun`+id+`results.csv`);
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

 const Line = ({ color }) => (
     <hr
         style={{
             color: color,
             backgroundColor: color,
             height: 0.2
         }}
     />
 );


const PlanBody = ({selectedSchema, plan, getPlan, runPlan, planRun}) => {

  let smallButton = {
    float: "left", width: "4rem", height:"4rem",
             margin:"1rem 1rem 1rem 1rem"
  }
  let bigButton = {
    float: "left", height:"4rem",
            margin:"1rem 1rem 1rem 1rem", width: "11rem"
  }
  return(
    <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
                boxShadow: "0 0 5px 2px #E0E0E0", width: "43rem"}}>

      <header className='body-name-plan'>
        Plan
      </header>

      {selectedSchema.selectedSchema != null ?

        <div className='plan'>
          <div style={{flexDirection:"row"}}>

            <Button
                disabled={plan.id === selectedSchema.selectedSchema.id || plan.isFetchingPlan}
                outline color={plan.id === selectedSchema.selectedSchema.id ? "primary" : "secondary"}
                style={smallButton}
                onClick={(e) => getPlan(selectedSchema.selectedSchema.id)}>

                Plan <FaRegMap/>
            </Button>

            {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
              <GraphicalPlanModal
                  graphicalPlan={plan.plan.graphicalPlan}
                  selectedSchema={selectedSchema.selectedSchema}
                  bigButton={bigButton}/>
              :
              <Button
                 outline color="secondary"
                 disabled={true}
                 style={bigButton}>
                     Plan exploration graph <FaRoute/>
               </Button>}

          </div>
          <div style={{width:"100%"}}>
            <Line color="#C8C8C8"/>
          </div>

          {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
            <div style={{flexDirection:"row"}}>

            <Button
               outline color={planRun.id === selectedSchema.selectedSchema.id ? "primary" : "secondary"}
               disabled={!plan.plan.runnable || planRun.isFetchingPlanRun ||
                        (planRun.planRun !== null &&
                          planRun.id === selectedSchema.selectedSchema.id)}
               style={smallButton}
               onClick={() => runPlan(selectedSchema.selectedSchema.id)}>
                   Run <FaPlay/>
             </Button>

              <PlanRunModal
                selectedSchema={selectedSchema.selectedSchema}
                planRun={planRun}
                plan ={plan.plan}
                downloadCSV = {downloadCSV}
                bigButton={bigButton}/>

              <DownloadCSVButton
                plan={plan}
                selectedSchema={selectedSchema.selectedSchema}
                downloadCSV={downloadCSV}
                planRun={planRun}
                smallButton={smallButton}/>
            </div>
            :
            <div>
              <Button
                 outline color="secondary"
                 disabled={true}
                 style={smallButton}>
                     Run <FaPlay/>
               </Button>

               <Button
                  outline color="secondary"
                  disabled={true}
                  style={bigButton}>
                      Run results <FaTable/>
                </Button>

                <Button
                  outline color="secondary"
                  disabled={true}
                  style={smallButton}>
                  <FaDownload/>
                </Button>
              </div>
           }
        </div>
        :
        null
      }
    </div>
  );
}


const mapStatesToProps = (state) =>{
  return({
    selectedSchema: state.selectedSchema,
    plan: state.plan,
    graphicalPlan: state.graphicalPlan,
    planRun: state.planRun
  })
}


const mapDispatchToProps = (dispatch) =>({
  getPlan: (id) => dispatch(plan(id)),

  runPlan: (id) => dispatch(runPlan(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(PlanBody);
