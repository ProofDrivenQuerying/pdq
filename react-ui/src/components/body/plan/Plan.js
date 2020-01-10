import React from 'react';
import GraphicalPlanModal from './GraphicalPlanModal';
import RunModal from './RunModal';
import DownloadRunButton from './DownloadRunButton';
import DownloadPlanButton from './DownloadPlanButton';
import PlanInfoModal from './PlanInfoModal';

import Button from 'react-bootstrap/Button';
import Spinner from 'react-bootstrap/Spinner';
import ButtonToolbar from 'react-bootstrap/ButtonToolbar';

import { connect } from 'react-redux';
import { plan } from "../../../actions/getPlan.js";
import { run } from "../../../actions/getRun.js";
import './planbody.css';
import { FaRegMap,
         FaPlay
} from 'react-icons/fa';

/**
 * Renders the plan/button, graphical plan/button, and plan properties/button
 *
 * @author Camilo Ortiz
 */


const Plan = ({plan, getPlan, run, planRun, schemaList, userID}) => {

  let bigButton = {
    float: "left", height:"4rem", margin:"1rem 1rem 1rem 1rem", width: "11rem"
  }
  let runViewName = "Run Results";
  return(
    <div>
      <h4 className='my-2'>
        Planning
      </h4>

      {schemaList !== null && schemaList.schemas.length > 0 ?

        <div>
            <PlanGroup
              plan={plan}
              getPlan={getPlan}
              schemaList={schemaList}
              userID={userID}
            />


          <div style={{width:"100%"}}>
            <hr style={{color: "#C8C8C8", backgroundColor: "#C8C8C8", height: 0.2}}/>
          </div>

          {plan.plan!==null
            && plan.schemaID === schemaList.selectedSID
            && plan.queryID === schemaList.selectedQID ?
            <div>

            <Button
               variant={!plan.plan.runnable ?
                 "danger"
                 :
                 planRun.queryID === schemaList.selectedQID &&
                 planRun.schemaID === schemaList.selectedSID?
                 "primary" : "secondary"
                 }
               disabled={!plan.plan.runnable || planRun.isFetchingPlanRun ||
                        (planRun.planRun !== null &&
                          planRun.schemaID === schemaList.selectedSID &&
                          planRun.queryID === schemaList.selectedQID
                        )}
               onClick={() => run(
                 schemaList.selectedSID,
                 schemaList.selectedQID,
                 schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL
               )}>
               {planRun.isFetchingPlanRun ?
               <Spinner color="secondary"/>
                :
                <div>
                  {plan.plan.runnable ?
                    <div>
                      Run <FaPlay/>
                    </div>
                    :
                    <div>
                      PDQ does not have access to the services required to run this plan
                    </div>
                  }

                </div>
                }
             </Button>

             {plan.plan.runnable ?
              <div>
                <RunModal
                  SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
                  queryID={schemaList.selectedQID}
                  schemaID={schemaList.selectedSID}
                  planRun={planRun}
                  plan ={plan.plan}
                  bigButton={bigButton}
                  name={runViewName}
                  userID={userID}
                  />

                <DownloadRunButton
                  SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
                  queryID={schemaList.selectedQID}
                  schemaID={schemaList.selectedSID}
                  plan={plan.plan}
                  planRun={planRun}
                  margins={true}
                  id={1}
                  userID={userID}
                  />
              </div>
              :
              null}

            </div>
            :
            <div>
              <Button
                 outline color="secondary"
                 disabled={true}>
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

const PlanGroup = ({plan, getPlan, schemaList, userID}) => {
  return (
    <div className='half'>

      <div className='my-2'>
        <Button
            block
            disabled={plan.isFetchingPlan}
            variant={plan.schemaID === schemaList.selectedSID && plan.queryID === schemaList.selectedQID ? 'outline-primary' : 'primary'}
            onClick={(e) => getPlan(
              schemaList.selectedSID,
              schemaList.selectedQID,
              schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL
            )}>
            <div className="my-3">
              {plan.isFetchingPlan ?
                <Spinner animation="border"/>
                :
                <div>
                {plan.schemaID === schemaList.selectedSID && plan.queryID === schemaList.selectedQID ?
                  <div>
                    Plan Again <FaRegMap/>
                  </div>
                  :
                  <div>
                    Plan <FaRegMap/>
                  </div>
                }
                </div>
              }
            </div>
        </Button>
      </div>
      {
        plan.plan!==null &&
        plan.schemaID === schemaList.selectedSID &&
        plan.queryID === schemaList.selectedQID ?
        <div>
          <PlanInfoModal
            id={schemaList.selectedSID}
            plan={plan.plan}/>

          <GraphicalPlanModal
              graphicalPlan={plan.plan.graphicalPlan}/>

          <DownloadPlanButton
              schemaID={schemaList.selectedSID}
              queryID = {schemaList.selectedQID}
              SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
              plan={plan}
              margins={true}
              id={1}
              userID={userID}/>
        </div>
        :
        null
      }


    </div>

  );
}


const mapStatesToProps = (state) => {
  return({
    plan: state.plan,
    graphicalPlan: state.graphicalPlan,
    planRun: state.planRun,
    schemaList: state.schemaList,
    userID: state.schemaList.userID
  })
}

const mapDispatchToProps = (dispatch) => ({
  getPlan: (schemaID, queryID, SQL) => dispatch(plan(schemaID, queryID, SQL)),

  run: (schemaID, queryID, SQL) => dispatch(run(schemaID, queryID, SQL))
});

export default connect(mapStatesToProps, mapDispatchToProps)(Plan);
