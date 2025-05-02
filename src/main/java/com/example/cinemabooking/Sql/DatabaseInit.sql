CREATE DATABASE cinemabooking;
USE cinemabooking;

CREATE TABLE movies (
    movieId INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    durationMinutes INT,
    genre VARCHAR(50)
);

CREATE TABLE rooms (
    roomId INT AUTO_INCREMENT PRIMARY KEY,
    roomName VARCHAR(50) NOT NULL UNIQUE,
    totalSeats INT
);

CREATE TABLE seats (
    seatId INT AUTO_INCREMENT PRIMARY KEY,
    seatName VARCHAR(10),
    roomId INT,
    seatType VARCHAR(20),
    `row` CHAR(1),
    `column` INT,
    FOREIGN KEY (roomId) REFERENCES rooms(roomId)
);

CREATE TABLE showtimes (
    showTimeId INT AUTO_INCREMENT PRIMARY KEY,
    movieId INT,
    roomId INT,
    startTime DATETIME,
    ticketPrice DECIMAL(10, 2),
    FOREIGN KEY (movieId) REFERENCES movies(movieId),
    FOREIGN KEY (roomId) REFERENCES rooms(roomId)
);

CREATE TABLE tickets (
    ticketId INT AUTO_INCREMENT PRIMARY KEY,
    seatId INT,
    customerPhone VARCHAR(15),
    showTimeId INT,
    isBooked BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (seatId) REFERENCES seats(seatId),
    FOREIGN KEY (showTimeId) REFERENCES showtimes(showTimeId),
    UNIQUE (seatId, showTimeId)
);

