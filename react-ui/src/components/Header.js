import React from 'react';
import logo from '../img/logo.png';
import Navbar from 'react-bootstrap/Navbar';
import Nav from 'react-bootstrap/Nav';
import Button from 'react-bootstrap/Button';

/**
 * Renders the app's header. Called from App.js.
 *
 * @author: Camilo Ortiz
 */

const Header = () =>{
  return(
    <Navbar bg='light' expand='lg' sticky='top'>
      <Navbar.Brand
        href='/'>
        <img
        src={logo}
        className='d-inline-block align-top'
        alt="Logo"
        height='45'/>
      </Navbar.Brand>
      <Navbar.Toggle aria-controls="basic-navbar-nav" />
      <Navbar.Collapse id="basic-navbar-nav">
        <Nav className='ml-auto'>
          <Nav.Link
          href="https://www.cs.ox.ac.uk/projects/pdq/home.html">
            About
          </Nav.Link>
        </Nav>
      </Navbar.Collapse>
    </Navbar>
  )

}


export default Header;
