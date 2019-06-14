import React, { Component } from 'react';
import Header from './components//header/Header.js';
import Body from './components/body/Body.js';
import { connect } from 'react-redux';
import { getInitialData } from './actions/initialJsonCall.js';
import setBody from './actions/setBody.js';



/**
 * Entry of the App's UI. Renders Header and Body components from
 * the components folder.
 *
 * @author Camilo Ortiz
 */

class App extends Component{

  componentWillMount(){
    //get initial schemaList from server and set state variable schemaList accordingly
    this.props.dispatch(getInitialData());

    this.props.dispatch(setBody('schema'));
  }

  render(){
    console.log(this.props);
    return(
      <div style={{height:"100%"}}>
        <Header/>
        <Body/>
      </div>
    )
  }
}
//map states to props
const mapStatesToProps = (state) =>({
  ...state
});

//connect component to store
export default connect(mapStatesToProps)(App);
