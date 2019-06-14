import React from 'react';
import './header.css';
import logo from '../../img/logo.png';
import { Breadcrumb, BreadcrumbItem } from 'reactstrap';

/**
 * Renders the app's header. Called from App.js.
 *
 * @author: Camilo Ortiz
 */

const Header = () =>{
  return(
    <div className = "Header-wrapper">
      <img src={logo} className="PDQ-Logo" alt="logo" />
      <div className = 'breadlist'>
        <Breadcrumb>
          <BreadcrumbItem>Schemas</BreadcrumbItem>
          <BreadcrumbItem>Queries</BreadcrumbItem>
          <BreadcrumbItem>Searches</BreadcrumbItem>
        </Breadcrumb>
      </div>

      <span className='about'>
        <a href="http://www.cs.ox.ac.uk/projects/pdq/home.html"
          target='_blank'
          rel="noopener noreferrer">
          about
        </a>
      </span>
    </div>
  )

}


export default Header;
