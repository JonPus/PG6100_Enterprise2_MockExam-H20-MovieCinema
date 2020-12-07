import React from 'react';
import { withRouter } from 'react-router-dom';
import Movie from './movie';

export class Ticket extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      userStats: null,
      collection: null,
      boughtSeats: null,
    };
  }

  componentDidMount() {
    this.fetchCollection();
    this.fetchUserStats();
  }

  fetchCollection = async () => {
    const url = '/api/rooms/collection_v1_000';

    let response;

    try {
      response = await fetch(url, { method: 'get' });
    } catch (err) {
      this.setState({ errorMsg: 'Failed to connect to server: ' + err });
      return;
    }

    if (response.status !== 200) {
      this.setState({
        errorMsg: 'Failed connection to server. Status ' + response.status,
      });
      return;
    }

    const payload = await response.json();
    this.setState({ collection: payload.data, errorMsg: null });
  };

  fetchUserStats = async () => {
    const userId = this.props.userId;
    const url = '/api/user-collections/' + userId;

    let response;

    try {
      response = await fetch(url, { method: 'get' });
    } catch (err) {
      this.setState({ errorMsg: 'Failed to connect to server: ' + err });
      return;
    }

    if (response.status === 401) {
      this.props.updateLoggedInUser(null);
      this.props.history.push('/');
      return;
    }

    if (response.status === 404) {
      this.setState({
        errorMsg: 'Error: user info not available for ' + userId,
      });
      return;
    }

    if (response.status !== 200) {
      this.setState({
        errorMsg: 'Failed connection to server. Status ' + response.status,
      });
      return;
    }

    const payload = await response.json();
    this.setState({ userStats: payload.data, errorMsg: null });
  };

  buyTicket = async () => {
    const userId = this.props.userId;
    const url = '/api/user-collections/' + userId;

    let response;

    const data = {
      command: 'BUY_ROOM',
    };

    try {
      response = await fetch(url, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data),
      });
    } catch (err) {
      this.setState({ errorMsg: 'Failed to connect to server: ' + err });
      return;
    }

    if (response.status === 401) {
      this.props.updateLoggedInUser(null);
      this.props.history.push('/');
      return;
    }

    if (response.status === 404) {
      this.setState({ errorMsg: 'Error: info not available for ' + userId });
      return;
    }

    const payload = await response.json();

    if (response.status !== 200) {
      this.setState({
        errorMsg:
          'Failure. Status ' + response.status + '. Msg: ' + payload.message,
      });
      return;
    }

    this.setState({ boughtSeats: payload.data, errorMsg: null });
    this.fetchUserStats();
  };

  boughtSeatsView = () => {
    this.setState({ boughtSeats: null });
  };

  render() {
    if (this.state.errorMsg) {
      return <p>{this.state.errorMsg}</p>;
    }
    if (!this.state.userStats) {
      return <p>Loading user collection...</p>;
    }
    if (!this.state.collection) {
      return <p>Loading rooms...</p>;
    }

    if (this.state.boughtSeats) {
      const rooms = this.state.boughtSeats.roomIdsInBoughtSeat.map((id) => {
        return this.state.collection.rooms.find((r) => r.roomId == id);
      });

      return (
        <div className='room-pack'>
          <button onClick={this.closePackView}>Close</button>
          <h1>Your bought tickets!</h1>
          <div className='room-container'>
            {rooms.map((r) => {
              r.seats = 1;
              return <Movie key={r.roomId} {...r} />;
            })}
          </div>
        </div>
      );
    }

    return (
      <>
        <p>Buy Ticket Site!</p>
        <button className='button' onClick={this.buyTicket}>
          Buy Tickets
        </button>
        <table>
          <thead>
            <tr>
              <th>Room Name</th>
              <th>Movie Showing</th>
              <th>Price</th>
              <th>Seats</th>
              <th>Buy Ticket</th>
            </tr>
          </thead>
          <tbody>
            {this.state.collection.rooms.map((r) => (
              <tr key={'key_' + r.roomId}>
                <td>{r.roomId}</td>
                <td>{r.movieName}</td>
                <td>{r.price}</td>
                <td>{r.seats}</td>
                <td>
                  <button onClick={() => this.buyTicket(r.roomId)}>Buy</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </>
    );
  }
}

export default withRouter(Ticket);
