import React from 'react';
import { withRouter } from 'react-router-dom';
import Movie from './movie';

export class Collection extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      userStats: null,
      collection: null,
      openedPack: null,
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

  openPack = async () => {
    const userId = this.props.userId;
    const url = '/api/user-collections/' + userId;

    let response;

    const data = {
      command: 'OPEN_PACK',
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

    this.setState({ openedPack: payload.data, errorMsg: null });
    this.fetchUserStats();
  };

  closePackView = () => {
    this.setState({ openedPack: null });
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

    if (this.state.openedPack) {
      const rooms = this.state.openedPack.roomIdsInOpenedPack.map((id) => {
        return this.state.collection.rooms.find((r) => r.roomId === id);
      });

      return (
        <div className='room-pack'>
          <button onClick={this.closePackView}>Close</button>
          <h1>Pack Content</h1>
          <div className='room-container'>
            {rooms.map((r) => {
              r.quantity = 1;
              return <Movie key={r.roomId} {...r} />;
            })}
          </div>
        </div>
      );
    }

    const packs = this.state.userStats.roomPacks;

    return (
      <div>
        <div className='players-stuff'>
          <p>Bonus Points: {this.state.userStats.coins} &#128176;</p>
          <p>Free Ticket Packs: {packs} &#127752;</p>
          <button
            className='button'
            disabled={packs <= 0}
            onClick={this.openPack}>
            Open Pack
          </button>
        </div>
        <div className='room-container'>
          {this.state.collection.rooms.map((r) => {
            const info = this.state.userStats.ownedRooms.find(
              (z) => z.roomId === r.roomId
            );
            const quantity = info ? info.numberOfCopies : 0;
            return <Movie key={r.roomId} {...r} quantity={quantity} />;
          })}
        </div>
      </div>
    );
  }
}

export default withRouter(Collection);
