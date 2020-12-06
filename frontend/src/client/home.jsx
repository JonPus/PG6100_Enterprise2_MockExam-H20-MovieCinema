import React from 'react';
import { withRouter } from 'react-router-dom';

export class Home extends React.Component {
  constructor(props) {
    super(props);
  }

  render() {
    return (
      <>
        <p className='home-welcome'>Welcome to SF Cinema!</p>
        <p className='home-text'>
          Buy Ticket, See Movies and Checkout the Upcoming Movies!
        </p>
      </>
    );
  }
}

export default withRouter(Home);
