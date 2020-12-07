import React from 'react';
import { Link, withRouter } from 'react-router-dom';

export class HeaderBar extends React.Component {
  constructor(props) {
    super(props);
  }

  doLogout = async () => {
    const url = '/api/auth/logout';

    let response;

    try {
      response = await fetch(url, { method: 'post' });
    } catch (err) {
      alert('Failed to connect to server: ' + err);
      return;
    }

    if (response.status !== 204) {
      alert('Error when connecting to server: status code ' + response.status);
      return;
    }

    this.props.updateLoggedInUser(null);
    this.props.history.push('/');
  };

  renderLoggedIn(userId) {
    return (
      <React.Fragment>
        <p className='header-text'>Welcome {userId}!</p>
        <Link className='header-link' to='/collection' tabIndex='0'>
          Your Tickets
        </Link>
        <Link className='header-link' to='/ticket' tabIndex='0'>
          Buy Tickets
        </Link>
        <Link className='header-link' to='/movieboard' tabIndex='0'>
          Upcoming Movies
        </Link>
        <button
          className='header-link header-link-logout'
          onClick={this.doLogout}
          id='logoutBtnId'>
          Logout
        </button>
      </React.Fragment>
    );
  }

  renderNotLoggedIn() {
    return (
      <React.Fragment>
        <p className='header-text'>You are not logged in</p>
        <div className='action-buttons'>
          <Link className='header-link' to='/login' tabIndex='0'>
            Log in
          </Link>
          <Link className='header-link' to='/signup' tabIndex='0'>
            Sign up
          </Link>
        </div>
      </React.Fragment>
    );
  }

  render() {
    const userId = this.props.userId;

    let content;
    if (!userId) {
      content = this.renderNotLoggedIn();
    } else {
      content = this.renderLoggedIn(userId);
    }

    return (
      <div className='header'>
        <Link className='header-logo' to={'/'} tabIndex='0'>
          <h1>SF Cinema</h1>
          <span>We are following all Covid-19 guidelines!</span>
        </Link>
        {content}
      </div>
    );
  }
}

export default withRouter(HeaderBar);
