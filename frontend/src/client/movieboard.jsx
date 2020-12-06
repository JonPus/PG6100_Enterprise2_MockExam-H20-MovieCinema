import React from 'react';
import { withRouter } from 'react-router-dom';

export class Movieboard extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      movies: [],
      next: null,
      error: null,
    };
  }

  componentDidMount() {
    this.fetchMovies();
  }

  fetchMovies = async () => {
    let url = '/api/movies';

    if (this.state.next !== null) {
      url = this.state.next;
    }

    let response;
    let payload;

    try {
      response = await fetch(url);
      payload = await response.json();
    } catch (err) {
      this.setState({
        error: 'Error when retrieving movies: ' + err,
        movies: [],
        next: null,
      });
      return;
    }

    if (response.status == 200) {
      this.setState((prev) => {
        return {
          error: null,
          movies: [...prev.movies, ...payload.data.list],
          next: payload.data.next,
        };
      });
    } else {
      this.setState({
        error:
          'Issue with HTTP connectiion: status code ' +
          response.status +
          ', ' +
          payload.error,
        movies: [],
        next: null,
      });
    }
  };

  render() {
    let table;

    if (this.state.error !== null) {
      table = <p>{this.state.error}</p>;
    } else if (!this.state.movies || this.state.movies.length === 0) {
      table = <p>There is no movieboard info</p>;
    } else {
      table = (
        <div>
          <table className='allScores'>
            <thead>
              <tr className='leaderboard-headers'>
                <th>Movie</th>
                <th>Director</th>
                <th>Release Date</th>
              </tr>
            </thead>
            <tbody className='leaderboard-rows'>
              {this.state.movies.map((m) => (
                <tr key={'key_' + m.id} className='leaderboard-row'>
                  <td>{m.movieTitle}</td>
                  <td>{m.directors}</td>
                  <td>{m.year}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    }

    return (
      <div className='center'>
        <h2>Upcoming Movies</h2>
        {table}
        {this.state.next && (
          <button className='button' onClick={this.fetchMovies}>
            Next
          </button>
        )}
      </div>
    );
  }
}

export default withRouter(Movieboard);
