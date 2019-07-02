import React from 'react';
import { FaShareAlt } from 'react-icons/fa';
import PopoutWindow from '../Popout';
import classnames from 'classnames';
import { Button,
         Modal,
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

export default class RelationModal extends React.Component{
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
          color={this.props.color}
          onClick={()=> this.openDependencies(this.props.schemaFromList.id)}
        >
          <FaShareAlt/>
        </Button>

        <Tooltip
          trigger="hover"
          placement="top"
          isOpen={this.state.tooltipOpen}
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
              <div style={{margin: "1rem"}}>
                {this.props.dependencyLists.dependencyLists.TGDDependencies != null
                  && this.props.dependencyLists.dependencyLists.TGDDependencies.length > 0 ?
                  <ul>
                  {this.props.dependencyLists.dependencyLists.TGDDependencies.map((dependency, index)=>{
                    return(
                      <li key={"TGDDependency"+index}>
                        <NestedDependenciesModal
                          name={"TGD Dependency " + index}
                          dependency={dependency}
                        />
                      </li>
                    )
                  })}
                  </ul>
                  :
                  <div>
                    {this.props.schemaFromList.name} contains no TGD Dependencies.
                  </div>
                }
              </div>
            </TabPane>

            <TabPane tabId="EGD">
              <div style={{margin: "1rem"}}>
                {this.props.dependencyLists.dependencyLists.EGDDependencies != null
                  && this.props.dependencyLists.dependencyLists.EGDDependencies.length > 0 ?
                  <ul>
                  {this.props.dependencyLists.dependencyLists.EGDDependencies.map((dependency, index)=>{
                    return(
                      <li key={"EGDDependency"+index}>
                        <NestedDependenciesModal
                          name={"EGD Dependency " + index}
                          dependency={dependency}
                        />
                      </li>
                    )
                  })}
                  </ul>
                  :
                  <div>
                    {this.props.schemaFromList.name} contains no EGD Dependencies.
                  </div>
                }
              </div>
            </TabPane>
          </TabContent>
          </ModalBody>

          <ModalFooter>
            <Button
              color="secondary"
              onClick={this.toggleDependenciesModal}>Cancel</Button>
          </ModalFooter>
        </Modal>
      </div>
    )
  }
}

//nested modal class that displays each relation's information
 class NestedDependenciesModal extends React.Component {
   constructor(props) {
     super(props);
     this.state = {
       modal: false
     };

     this.toggle = this.toggle.bind(this);
   }

   toggle() {
     this.setState(prevState => ({
       modal: !prevState.modal
     }));
   }

   render() {
     let definitionStringStyle = {
       position: "relative",
       marginRight: "2rem",
       marginLeft: "2rem",
       display: "flex",
       flexDirection: "column",
       overflowY: "scroll",
       whiteSpace: "pre-wrap",
       fontSize: "1rem",
       height: "calc(100vh - 300px)",
       wordWrap:"break"
     }
     const dependencyContent = (definition, name) => (
       <div>
         <header>{name}</header>

         <div style={{
           position: "relative",
           marginRight: "2rem",
           marginLeft: "2rem",
           display: "flex",
           flexDirection: "column",
           overflowY: "scroll",
           whiteSpace: "pre-wrap",
           fontSize: "1rem",
           height: "calc(100vh - 300px)",
           wordWrap:"break"}}>
           {this.props.dependency.definition}
         </div>
       </div>
     )
     return (
       <div>
         <Button
            color="link"
            onClick={this.toggle}>{this.props.name}</Button>

         <Modal
          isOpen={this.state.modal}
          toggle={this.toggle}
          size="lg">

           <ModalHeader toggle={this.toggle}>
              {this.props.name}
              <PopoutWindow
                title={"Dependency"}
                content={dependencyContent(this.props.dependency.definition, this.props.name)}/>
            </ModalHeader>

           <ModalBody>
            <div style={definitionStringStyle}>
              {this.props.dependency.definition}
            </div>
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
