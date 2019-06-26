import React, { Component } from 'react';
import Header from './components//header/Header.js';
import Body from './components/body/Body.js';
import { connect } from 'react-redux';
import { getInitialData } from './actions/getSimpleSchemaList.js';

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
  }

  render(){
    console.log(this.props);
    return(
      <div style={{height:"100vh"}}>
        <Header/>
        <Body/>
      </div>
    )
  }
}

//redux
const mapStatesToProps = (state) =>({
  ...state
});

export default connect(mapStatesToProps, null)(App);
