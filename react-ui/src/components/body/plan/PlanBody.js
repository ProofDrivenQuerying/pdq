import React from 'react';
import GraphicalPlanModal from './GraphicalPlanModal';
import RunModal from './RunModal';
import DownloadRunButton from './DownloadRunButton';
import DownloadPlanButton from './DownloadPlanButton';
import PlanInfoModal from './PlanInfoModal';
import { Button, Spinner } from 'reactstrap';
import { connect } from 'react-redux';
import { plan } from "../../../actions/getPlan.js";
import { runPlan } from "../../../actions/runPlan.js";
import './planbody.css';
import { FaRegMap,
         FaPlay
} from 'react-icons/fa';

/**
 * Renders the plan/button, graphical plan/button, and plan properties/button
 *
 * @author Camilo Ortiz
 */


const PlanBody = ({selectedSchema, plan, getPlan, runPlan, planRun, schemaList}) => {

  let smallButton = {
    float: "left", width: "4rem", height:"4rem", margin:"1rem 1rem 1rem 1rem"
  }
  let bigButton = {
    float: "left", height:"4rem", margin:"1rem 1rem 1rem 1rem", width: "11rem"
  }
  let graphicalPlanName = "Plan Exploration Graph";
  let planViewName = "View Plan";
  let runViewName = "Run Results";

  return(
    <div>

      <header className='body-name-plan'>
        Plan
      </header>

      {schemaList.schemas != null ?

        <div className='plan'>
          <div style={{flexDirection:"row"}}>

            <Button
                disabled={plan.id === schemaList.selectedSID || plan.isFetchingPlan}
                outline color={plan.id === schemaList.selectedSID ? "primary" : "secondary"}
                style={smallButton}
                onClick={(e) => getPlan(schemaList.selectedSID)}>
                {plan.isFetchingPlan ?
                  <Spinner color="secondary"/>
                  :
                  <div>
                  Plan <FaRegMap/>
                  </div>
                }
            </Button>

            {plan.plan!==null && plan.id === schemaList.selectedSID ?
              <div>
                <PlanInfoModal
                  bigButton={bigButton}
                  id={schemaList.selectedSID}
                  plan={plan.plan}
                  name = {planViewName}/>

                <GraphicalPlanModal
                    graphicalPlan={plan.plan.graphicalPlan}
                    bigButton={bigButton}
                    name = {graphicalPlanName}/>

                <DownloadPlanButton
                    plan={plan}
                    margins={true}
                    id={1}
                    schemaID={schemaList.selectedSID}/>

              </div>
              :
              null
             }
          </div>

          <div style={{width:"100%"}}>
            <hr style={{color: "#C8C8C8", backgroundColor: "#C8C8C8", height: 0.2}}/>
          </div>

          {plan.plan!==null && plan.id === schemaList.selectedSID ?
            <div style={{flexDirection:"row"}}>

            <Button
               outline color={!plan.plan.runnable ?
                 "danger"
                 :
                 planRun.id === schemaList.selectedSID ?
                 "primary" : "secondary"
                 }
               disabled={!plan.plan.runnable || planRun.isFetchingPlanRun ||
                        (planRun.planRun !== null &&
                          planRun.id === schemaList.selectedSID)}
               style={smallButton}
               onClick={() => runPlan(schemaList.selectedSID)}>
               {planRun.isFetchingPlanRun ?
               <Spinner color="secondary"/>
                :
                <div>
                  Run <FaPlay/>
                </div>
                }
             </Button>

             {plan.plan.runnable ?
              <div>
                <RunModal
                  schemaID={schemaList.selectedSID}
                  planRun={planRun}
                  plan ={plan.plan}
                  bigButton={bigButton}
                  name={runViewName}/>

                <DownloadRunButton
                  schemaID={schemaList.selectedSID}
                  plan={plan.plan}
                  planRun={planRun}
                  margins={true}
                  id={1}/>
              </div>
              :
              null}

            </div>
            :
            <div>
              <Button
                 outline color="secondary"
                 disabled={true}
                 style={smallButton}>
                     Run <FaPlay/>
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
    plan: state.plan,
    graphicalPlan: state.graphicalPlan,
    planRun: state.planRun,
    schemaList: state.schemaList
  })
}


const mapDispatchToProps = (dispatch) =>({
  getPlan: (id) => dispatch(plan(id)),

  runPlan: (id) => dispatch(runPlan(id))
});

export default connect(mapStatesToProps, mapDispatchToProps)(PlanBody);
