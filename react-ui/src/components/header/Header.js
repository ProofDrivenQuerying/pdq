import React from 'react';
import { Breadcrumb, BreadcrumbItem } from 'reactstrap';
import './header.css';
import logo from '../../img/logo.png';

/**
 * Renders the app's header. Called from App.js.
 *
 * @author: Camilo Ortiz
 */

const Header = () =>{
  return(
    <div className = "Header-wrapper">
      <a
        style={{display:"flex"}}
        href="http://www.cs.ox.ac.uk/projects/pdq/home.html"
        target='_blank'
        rel="noopener noreferrer">
        <img src={logo} className="PDQ-Logo" alt="logo" />
      </a>

      <div className = 'breadlist'>
        <Breadcrumb>
          <BreadcrumbItem>Schema</BreadcrumbItem>
          <BreadcrumbItem>Query</BreadcrumbItem>
          <BreadcrumbItem>Plan</BreadcrumbItem>
        </Breadcrumb>
      </div>

      <span className='about'>
        <a
          href="http://www.cs.ox.ac.uk/projects/pdq/home.html"
          target='_blank'
          rel="noopener noreferrer">
          about
        </a>
      </span>
    </div>
  )

}


export default Header;
