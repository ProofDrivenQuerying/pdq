import React from 'react';
import Popout from 'react-popout';
import '../../bootstrap/bootstrap.css'
import { FaShareSquare } from 'react-icons/fa';
import { Button,
         Tooltip
} from 'reactstrap';

export default class PopoutWindow extends React.Component{
  constructor(props) {
    super(props);
    this.popout = this.popout.bind(this);
    this.popoutClosed = this.popoutClosed.bind(this);
    this.state = { isPoppedOut: false };
  }

  popout() {
    this.setState({isPoppedOut: true});
  }

  popoutClosed() {
    this.setState({isPoppedOut: false});
  }

  render() {
    if (this.state.isPoppedOut) {
      return (
        <div>
          <Popout
            title={this.props.title}
            options={this.props.options}
            >
            <div>{this.props.content}</div>
          </Popout>

          <Button
            onClick={this.popout}
            color="link"
            style={{float:'right'}}>
            <FaShareSquare/>
          </Button>
        </div>
      );
    } else {
      return (
        <Button
          onClick={this.popout}
          color="link"
          style={{float:'right'}}
          >
          <FaShareSquare/>
        </Button>
      );
    }
  }
}
