import React from 'react';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';

/**
 * Renders the app's header. Called from App.js.
 *
 * @author: Camilo Ortiz
 */

const Header = () =>{
  return(
    <Navbar bg='light' expand='lg' sticky='top' className='border border-top-0'>
      <Navbar.Brand
        href='/'>
        <img
        src={'https://www.cs.ox.ac.uk/projects/pdq/images/pdq-logo.svg'}
        className='d-inline-block align-top'
        alt="Logo"
        height='45'/>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className='ml-auto'>
          <Nav.Link
          href="http://www.cs.ox.ac.uk/projects/pdq/home.html">
            About
          </Nav.Link>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  )

}


export default Header;
