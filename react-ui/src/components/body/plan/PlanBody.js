//react
import React from 'react';
//components
import GraphicalPlanModal from './GraphicalPlanModal';
import PlanPropertiesModal from './PlanPropertiesModal';
//reactstrap
import { Button } from 'reactstrap';
//redux
import { connect } from 'react-redux';
//actions
import { plan } from "../../../actions/plan.js";
import { runPlan } from "../../../actions/runPlan.js";
//css
import './planbody.css';
//icons
import { FaRegMap, FaPlay } from 'react-icons/fa';
//img
import moreDots from '../../../img/threeDots.png';

/**
 * Renders the plan/button, graphical plan/button, and plan properties/button
 *
 * @author Camilo Ortiz
 */

const PlanBody = ({selectedSchema, plan, getPlan, runPlan, planRun}) => {
  return(
    <div style={{border:"1px solid #E0E0E0", borderRadius:"25px",
                boxShadow: "0 0 5px 2px #E0E0E0", width: "43rem"}}>

      <header className='body-name-plan'>
        Plan
        <div style={{float:"right"}}>
          <PlanPropertiesModal/>
        </div>
      </header>


      {selectedSchema.selectedSchema != null ?

        <div className='plan'>
          <div style={{flexDirection:"row"}}>

            <Button
                disabled={plan.isFetchingPlan}
                outline color="secondary"
                style={{float: "left", width: "4rem", height:"4rem", margin:"1rem 1rem 1rem 1rem"}}
                onClick={(e) => getPlan(selectedSchema.selectedSchema.id)}>

                Plan <FaRegMap/>

            </Button>
            <div style={{whiteSpace:"normal",
                          overflowX:"scroll", margin:"1rem 1rem 1rem 1rem",
                          height:"4rem", border:"1px solid #E0E0E0",
                          width:"80%", padding: "1rem 1rem 1rem 1rem"}}>

                {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
                  <div>
                    {plan.plan.bestPlan}
                  </div>
                  :
                  null}
            </div>
          </div>
          {plan.plan!==null && plan.id === selectedSchema.selectedSchema.id ?
            <div>
              <GraphicalPlanModal
                  graphicalPlan={plan.plan.graphicalPlan}
                  selectedSchema={selectedSchema.selectedSchema}/>

              <Button
                disabled={!plan.plan.runnable}
                outline color="secondary"
                style={{float: "left", height:"4rem", margin:"1rem 1rem 1rem 1rem",
                        width: "11rem"}}
                onClick={(e)=>runPlan(selectedSchema.selectedSchema.id)}>
                Run best plan <FaPlay/>
              </Button>

              <Button
                disabled={planRun.planRun === null}
                outline color="secondary"
                style={{float: "left", height:"4rem", margin:"1rem 1rem 1rem 1rem",
                        width: "11rem"}}
                >
                Download run as .csv  <FaPlay/>
              </Button>
            </div>
            :
            null}

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
