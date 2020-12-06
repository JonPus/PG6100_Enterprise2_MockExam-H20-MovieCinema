import React from 'react';

const Movie = (props) => {
  let imageClasses = 'room-image';

  if (props.quantity) {
    props.quantity;
  } else {
    imageClasses += ' disabled';
  }

  let divClasses = 'room ' + props.roomClass;

  return (
    <div className={divClasses}>
      <p className='room-label'>Title: {props.movieName}</p>
      <p className='room-label'>Tickets available: {props.seats}</p>
      <p className='room-label'>Your Tickets: {props.quantity}</p>
      <img className={imageClasses} src={'/api/rooms/imgs/' + props.imageId} />
    </div>
  );
};

export default Movie;
