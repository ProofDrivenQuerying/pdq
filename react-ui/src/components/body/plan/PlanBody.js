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


const PlanBody = ({selectedSchema, plan, getPlan, runPlan, planRun}) => {

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
                {plan.isFetchingPlan ?
                  <Spinner color="secondary"/>
                  :
                  <div>
                  Plan <FaRegMap/>
                  </div>
                }
            </Button>

            {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
              <div>
                <PlanInfoModal
                  bigButton={bigButton}
                  selectedSchema={selectedSchema.selectedSchema}
                  plan={plan.plan}
                  name = {planViewName}/>

                <GraphicalPlanModal
                    graphicalPlan={plan.plan.graphicalPlan}
                    selectedSchema={selectedSchema.selectedSchema}
                    bigButton={bigButton}
                    name = {graphicalPlanName}/>

                <DownloadPlanButton
                    plan={plan}
                    margins={true}
                    selectedSchema={selectedSchema.selectedSchema}/>

              </div>
              :
              null
             }
          </div>

          <div style={{width:"100%"}}>
            <hr style={{color: "#C8C8C8", backgroundColor: "#C8C8C8", height: 0.2}}/>
          </div>

          {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
            <div style={{flexDirection:"row"}}>

            <Button
               outline color={!plan.plan.runnable ?
                 "danger"
                 :
                 planRun.id === selectedSchema.selectedSchema.id ?
                 "primary" : "secondary"
                 }
               disabled={!plan.plan.runnable || planRun.isFetchingPlanRun ||
                        (planRun.planRun !== null &&
                          planRun.id === selectedSchema.selectedSchema.id)}
               style={smallButton}
               onClick={() => runPlan(selectedSchema.selectedSchema.id)}>
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
                  selectedSchema={selectedSchema.selectedSchema}
                  planRun={planRun}
                  plan ={plan.plan}
                  bigButton={bigButton}
                  name={runViewName}/>

                <DownloadRunButton
                  plan={plan.plan}
                  selectedSchema={selectedSchema.selectedSchema}
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
