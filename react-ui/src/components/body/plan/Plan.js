// This file is part of PDQ (https://github.com/ProofDrivenQuerying/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React from 'react';
import GraphicalPlanModal from './GraphicalPlanModal';
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
import Container from 'react-bootstrap/Container';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';

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
    <div style={{overflowY:"scroll"}}>
      <h4 className='my-2 pb-1'>
        <Container style={{padding:'0', margin:'0'}}>
          <Row>
            <Col xs={9}>Plan</Col>
          </Row>
        </Container>
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
            <div className='half'>
              <h4 className='my-3'>
                <Container style={{padding:'0', margin:'0'}}>
                  <Row>
                    <Col>
                    Run Your Plan
                    </Col>
                  </Row>
                </Container>
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
    <div>
      <div className='my-3'>
        <Button
            block
            disabled={plan.isFetchingPlan || (plan.schemaID === schemaList.selectedSID && plan.queryID === schemaList.selectedQID && plan.isErrorPlan)}
            variant={plan.schemaID === schemaList.selectedSID && plan.queryID === schemaList.selectedQID ? plan.isErrorPlan ? 'danger' :'outline-primary' : 'primary'}
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

                  <div>
                  {plan.schemaID === schemaList.selectedSID && plan.queryID === schemaList.selectedQID ?
                    <div>
                      { plan.isErrorPlan ?
                        <div>
                        PDQ was unable to plan with this query
                        </div>
                        :
                      <div>
                        Plan Again <FaRegMap/>
                      </div>
                      }
                    </div>
                    :
                    <div>
                      Plan <FaRegMap/>
                    </div>
                  }
                  </div>

                </div>
              }
            </div>
        </Button>
      </div>
      {
        plan.plan!==null &&
        plan.schemaID === schemaList.selectedSID &&
        plan.queryID === schemaList.selectedQID ?

        <div className='my-4'>
          <h4 className='my-2'>
            <Container style={{padding:'0', margin:'0'}}>
              <Row>
                <Col xs={9}>Plan Results</Col>
                <Col>
                  <DownloadPlanButton
                    schemaID={schemaList.selectedSID}
                    queryID = {schemaList.selectedQID}
                    SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
                    plan={plan}
                    margins={true}
                    id={1}/>
                </Col>
              </Row>
              <Row>
               <Col className="my-2">
                  <h6 style={{whiteSpace: 'normal'}}><i>Found an optimal {plan.plan.runnable ? "runnable":null} plan
                    in {plan.plan.planTime} seconds.</i></h6>
                </Col>
              </Row>
            </Container>
          </h4>

          <div>
            <PlanInfoModal
              id={schemaList.selectedSID}
              plan={plan.plan}/>

            <GraphicalPlanModal
                graphicalPlan={plan.plan.graphicalPlan}/>

          </div>
        </div>

        :
        null
      }

    </div>

  );
}

const RunGroup = ({plan, planRun, schemaList, userID, run}) => {

  return (
    <div>
      {plan.plan!==null
        && plan.schemaID === schemaList.selectedSID
        && plan.queryID === schemaList.selectedQID ?

        <div className='my-2 mx-0'>
          <Button
            block
            disabled={planRun.isFetchingPlanRun || !plan.plan.runnable || (planRun.schemaID === schemaList.selectedSID && planRun.queryID === schemaList.selectedQID && planRun.isErrorPlanRun)}
            variant={planRun.schemaID === schemaList.selectedSID && planRun.queryID === schemaList.selectedQID ? planRun.isErrorPlanRun ? 'danger' :'outline-primary' : 'primary'}
            onClick={() => run(
               schemaList.selectedSID,
               schemaList.selectedQID,
               schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL
             )}>
             <div >
               {planRun.isFetchingPlanRun ?
                 <div className="my-2">
                   <Spinner animation="border"/>
                 </div>
                 :
                 <div className="my-3">
                   {plan.plan.runnable ?

                   <div>

                   {planRun.schemaID === schemaList.selectedSID && planRun.queryID === schemaList.selectedQID ?
                     <div>
                       { planRun.isErrorPlanRun ?
                         <div>
                         PDQ was unable to run this plan
                         </div>
                         :
                         <div>
                           Run Again <FaPlay/>
                         </div>
                       }
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
            <div className='my-4'>
              <h4 className='my-2'>
                <Container style={{padding:'0', margin:'0'}}>
                  <Row>
                    <Col xs={9}>Run Results</Col>
                    <Col>
                    <DownloadRunButton
                      SQL={schemaList.schemas[schemaList.selectedSID].queries[schemaList.selectedQID].SQL}
                      queryID={schemaList.selectedQID}
                      schemaID={schemaList.selectedSID}
                      plan={plan.plan}
                      planRun={planRun}
                      margins={true}
                      id={1}
                      />
                     </Col>
                  </Row>
                  <Row>
                    <Col className="my-2">
                      <h6><i>{planRun.planRun.tupleCount} {" "}
                        {planRun.planRun.tupleCount > 1 ? "tuples" : "tuple" } found
                        in {planRun.planRun.runTime} seconds.</i></h6>
                    </Col>
                  </Row>
                </Container>

              </h4>

              <div>
                <RunTable planRun={planRun.planRun}/>
              </div>
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
    <div className='my-2'>
      <Table responsive>
        <thead>
          <tr>
           <th>#</th>
           {planRun.table.columns.map((name, index) => {
             return[ <th key={"runHead"+index}>{name}</th> ]
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
