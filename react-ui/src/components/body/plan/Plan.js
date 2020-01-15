import React from 'react';
import GraphicalPlanModal from './GraphicalPlanModal';
import RunModal from './RunModal';
import DownloadRunButton from './DownloadRunButton';
import DownloadPlanButton from './DownloadPlanButton';
import PlanInfoModal from './PlanInfoModal';
import Button from 'react-bootstrap/Button';
import Spinner from 'react-bootstrap/Spinner';
import { connect } from 'react-redux';
import { plan } from '../../../actions/getPlan.js';
import { run } from '../../../actions/getRun.js';
import { FaRegMap,
         FaPlay
} from 'react-icons/fa';
import Table from 'react-bootstrap/Table';

/**
 * Renders the plan button, graphical modal, plan properties modal,
 * and the plan download button.
 *
 * Renders the run button, run table modal, and the run download button.
 *
 * @author Camilo Ortiz
 */

const Plan = ({plan, getPlan, run, planRun, schemaList, userID}) => {

  return(
    <div>
      <h4 className='my-2 pb-1'>
        Plan
      </h4>

      {schemaList !== null && schemaList.schemas.length > 0 ?

        <div>
          <PlanGroup
            plan={plan}
            getPlan={getPlan}
            schemaList={schemaList}
            userID={userID}
          />
          {plan.plan!==null
            && plan.schemaID === schemaList.selectedSID
            && plan.queryID === schemaList.selectedQID ?
            <div>
              <h4 className='my-2 pb-1'>
                Run Your Plan with PDQ
              </h4>

            <RunGroup
              plan={plan}
              planRun={planRun}
              schemaList={schemaList}
              userID={userID}
              run={run}
            />
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
            <div >
              {plan.isFetchingPlan ?
                <div className="my-2">
                  <Spinner animation="border"/>
                </div>
                :
                <div className="my-3">
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

const RunGroup = ({plan, planRun, schemaList, userID, run}) => {
  return (
    <div className='half'>
      {plan.plan!==null
        && plan.schemaID === schemaList.selectedSID
        && plan.queryID === schemaList.selectedQID ?

        <div className='my-2'>
          <Button
            block
             variant={!plan.plan.runnable ?
               "danger"
               :
               (planRun.planRun !== null &&
                planRun.schemaID === schemaList.selectedSID &&
                planRun.queryID === schemaList.selectedQID ?
                "outline-primary"
                :
                "primary"
                )
               }
             disabled={!plan.plan.runnable || planRun.isFetchingPlanRun}
             onClick={() => run(
               schemaList.selectedSID,
               schemaList.selectedQID,
               schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL
             )}>
             <div className="my-3">
               {planRun.isFetchingPlanRun ?
                 <div className="my-2">
                  <Spinner animation="border"/>
                </div>
                :
                <div>
                  {plan.plan.runnable ?
                    <div>
                      {planRun.schemaID === schemaList.selectedSID && planRun.queryID === schemaList.selectedQID ?
                        <div>
                          Run Again <FaPlay/>
                        </div>
                        :
                        <div>
                          Run <FaPlay/>
                        </div>
                      }

                    </div>
                    :
                    <div className='overflow'>
                      PDQ does not have access to the services required to run this plan
                    </div>
                  }
                </div>
                }
              </div>
           </Button>

           {planRun.planRun!==null &&
           planRun.schemaID === schemaList.selectedSID &&
           planRun.queryID === schemaList.selectedQID  ?
            <div>
              {
                <RunTable planRun={planRun.planRun}/>
              // <RunModal
              //   SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
              //   queryID={schemaList.selectedQID}
              //   schemaID={schemaList.selectedSID}
              //   planRun={planRun}
              //   plan ={plan.plan}
              //   userID={userID}
              //   />
              }


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
          null
          }
        </div>
        :
        null
      }
    </div>
  );
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
