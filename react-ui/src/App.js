// This file is part of PDQ (https://github.com/michaelbenedikt/pdq) which is released under the MIT license.
// See accompanying LICENSE for copyright notice and full details.

import React, { Component } from 'react';
import Header from './components/Header';
import Body from './components/body/Body';
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
    this.props.dispatch(getInitialData());
  }

  render(){
    return(
      <div>
        <Header/>

        {this.props.schemaList.schemas.length === 0 ?
          null
          :
          <Body/>}

      </div>
    )
  }
}

//redux
const mapStatesToProps = (state) =>({
  ...state
});

export default connect(mapStatesToProps, null)(App);
