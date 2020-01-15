import React from 'react';
import { FaShareAlt } from 'react-icons/fa';
// import PopoutWindow from '../Popout';
import { IconContext } from "react-icons";
import classnames from 'classnames';
import Button from 'react-bootstrap/Button';
import ListGroup from 'react-bootstrap/ListGroup';
import { Modal,
         ModalHeader,
         ModalBody,
         ModalFooter,
         Tooltip,
         Nav,
         NavItem,
         NavLink,
         TabContent,
         TabPane,
} from 'reactstrap';

export default class Dependencies extends React.Component{
  constructor(props){
    super(props);
    this.toggleDependenciesModal = this.toggleDependenciesModal.bind(this);
    this.toggleTooltip = this.toggleTooltip.bind(this);
    this.toggleTab = this.toggleTab.bind(this);
      this.state = {
        modalDependenciesOpen: false,
        tooltipOpen: false,
        activeTab: "TGD"
      };
  }

  toggleDependenciesModal(){
    this.setState({
      modalDependenciesOpen: !this.state.modalDependenciesOpen,
      tooltipOpen: false
    });
  }

  toggleTooltip(){
    this.setState({
      tooltipOpen: !this.state.tooltipOpen
    })
  }

  toggleTab(tab){
    if (this.state.activeTab !== tab) {
      this.setState({
        activeTab: tab
      });
    }
  }

  openDependencies(id){
    this.props.getDependencies(id);
    this.toggleDependenciesModal();
  }

  render(){
    return(
      <div>
        <Button
          id={"DependenciesButton"+ this.props.schemaFromList.id}
          variant='link'
          onClick={()=> this.openDependencies(this.props.schemaFromList.id)}
        >
          <IconContext.Provider value={{ style:{ margin: '0', padding: '0'} }}>
            <FaShareAlt/>
          </IconContext.Provider>
        </Button>

        <Tooltip
          trigger="hover"
          placement="top"
          isOpen={this.state.tooltipOpen && !this.state.modalDependenciesOpen}
          target={"DependenciesButton"+ this.props.schemaFromList.id}
          toggle={this.toggleTooltip}>
          View Dependencies
        </Tooltip>

        <Modal
          size="lg"
          isOpen={this.state.modalDependenciesOpen}
          toggle={this.toggleDependenciesModal}>
          <ModalHeader toggle={this.toggleDependenciesModal}>Dependencies</ModalHeader>
          <ModalBody>
            <Nav tabs>
            <NavItem>
              <NavLink
                className={classnames({ active: this.state.activeTab === 'TGD' })}
                onClick={() => { this.toggleTab('TGD'); }}
              >
                TGD Dependencies
              </NavLink>
            </NavItem>
            <NavItem>
              <NavLink
                className={classnames({ active: this.state.activeTab === 'EGD' })}
                onClick={() => { this.toggleTab('EGD'); }}
              >
                EGD Dependencies
              </NavLink>
            </NavItem>
          </Nav>

          <TabContent activeTab={this.state.activeTab}>
            <TabPane tabId="TGD">
              <div>
                {this.props.dependencyLists.dependencyLists.TGDDependencies != null
                  && this.props.dependencyLists.dependencyLists.TGDDependencies.length > 0 ?
                  <ListGroup variant='flush'>
                  {this.props.dependencyLists.dependencyLists.TGDDependencies.map((dependency, index)=>{
                    return(
                      <ListGroup.Item style={{overflowWrap: 'break-word'}} key={"EGDDependency"+index}>
                        {dependency.definition}
                      </ListGroup.Item>
                    )
                  })}
                  </ListGroup>
                  :
                  <div className='my-2'>
                    {this.props.schemaFromList.name} contains no TGD Dependencies.
                  </div>
                }
              </div>
            </TabPane>

            <TabPane tabId="EGD">
              <div>
                {this.props.dependencyLists.dependencyLists.EGDDependencies != null
                  && this.props.dependencyLists.dependencyLists.EGDDependencies.length > 0 ?
                  <ListGroup variant='flush'>
                  {this.props.dependencyLists.dependencyLists.EGDDependencies.map((dependency, index)=>{
                    return(
                      <ListGroup.Item style={{overflowWrap: 'break-word'}} key={"EGDDependency"+index}>
                        {dependency.definition}
                      </ListGroup.Item>
                    )
                  })}
                  </ListGroup>
                  :
                  <div className='my-2'>
                    {this.props.schemaFromList.name} contains no EGD Dependencies.
                  </div>
                }
              </div>
            </TabPane>
          </TabContent>
          </ModalBody>

          <ModalFooter>
            <Button
              variant="secondary"
              onClick={this.toggleDependenciesModal}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}
