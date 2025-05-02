# CINEMA BOOKING SYSTEM
Cinema Booking application manages the entire movie ticket booking process in a cinema, including information about movies, showtimes, rooms, seats and tickets. ADMIN users can freely create new rooms, movies and showtimes. CUSTOMER users can book movie tickets and manage movie tickets using their phone numbers.

Currently, the application is only implemented at a rudimentary level and focuses on multi-thread management to synchronize the database when multiple users book tickets at the same time.

* **Author**: Huynh Le Dan Linh - 22520759
* **Instrutor**: PhD. Nguyen Duy Khanh
* **Subject**: SE334 - Programming Paradigms.
* **Exercise**: TH01 - Deadline: 05.05.2025
* **Technologies**:

  * Java Spring Boot
  * MySQL
  * Thymeleaf

* **Demo**: [Demo video](https://youtu.be/pKi9V0QmNOU)
* **Acknowledgement**: The project utilized ChatGPT to support UI arrangement and CSS, code cleaning and formatting, as well as documentation.

## TABLE OF CONTENTS
1. [Features](#features)
    - [Admin Limitations](#admin-limitations)
    - [User Authentication](#user-authentication)
2. [Usage Guidelines](#usage-guidelines)
3. [Database Design](#database-design)
4. [Testing](#testing)
    - [TicketDAO Testing](#1-ticketdao-testing)
    - [ShowTimeDAO Testing](#2-showtimedao-testing)
5. [Application Screen Captures](#application-screen-captures)
    - [Admin Mode](#1-admin-mode)
    - [Customer Mode](#2-customer-mode)


## FEATURES
| **Feature**          | **Mode**   | **Description**                                                                                         | **Note**                                                                                      |
|----------------------|------------|---------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------|
| Add a new Movie      | `ADMIN`    | The admin can add a new movie to the movie repository                                                  |                                                                                               |
| Add a new Room       | `ADMIN`    | The admin can add a new room along with its seat map                                                   |                                                                                               |
| Add a new Showtime   | `ADMIN`    | The admin can add a new showtime for a movie. Tickets are auto-generated based on the showtime room   | Showtimes must not overlap (same room, same start time). **No end time check for now.**        |
| Book Ticket          | `CUSTOMER` | The customer can browse available showtimes and book movie tickets                                     | Prevents double-booking. No two users can successfully book the same ticket simultaneously. Each ticket will be treated individually.    |
| Cancel Ticket        | `CUSTOMER` | The customer can cancel their ticket and return it to the pool of available (unbooked) tickets         |                                                                                               |

### Admin Limitations
* No **Update** or **Delete** operations for existing data
* Only **Add** functionality

### User Authentication
* No login system
* Just toggle between `ADMIN` and `CUSTOMER` modes manually

## USAGE GUIDELINES
> **&#9432;** **NOTES**
>* The application is designed to run only one instance at a time. This is enforced through **in-app thread locks**, which ensure that only one operation can be executed simultaneously. Running multiple instances of the application may cause conflicts and unexpected behavior, so please ensure that you are running **only one instance** of the app.
>* Do not manually change the URL in your browser's address bar to navigate between pages within the application. Always use the **navigation buttons** provided within the app to move between pages. This ensures that the app's internal state remains intact and prevents errors.

1. Create the database schema from the provided [SQL Init file](src/main/java/com/example/cinemabooking/Sql/DatabaseInit.sql).
2. Replace the URL, USER and PASSWORD in [DatabaseConnection](src/main/java/com/example/cinemabooking/Model/DatabaseConnection.java) class with your own data.
```java
private static final String URL = "jdbc:mysql://localhost:your/database/link";
private static final String USER = "your_username";
private static final String PASSWORD = "your_password";
```
3. Run the application and access to `localhost:8080/landing/`
4. Choose your mode and enjoy!

---
## DATABASE DESIGN
![ERD - CinemaBooing](https://github.com/user-attachments/assets/da9ea558-efd5-4751-90d6-889c025c7041)

#### 1. movies (Movie)

Contains information about the movies being shown:

* `movieId`: Primary key
* `title`: Movie name
* `durationMinutes`: Movie duration (minutes)
* `genre`: Movie genre

#### 2. rooms (Room)

Contains a list of movie screening rooms:

* `roomId`: Primary key
* `roomName`: Room name
* `totalSeats`: Total number of seats in the room

#### 3. showtimes (Showtime)

Link between movies and screening rooms by specific time:

* `showTimeId`: Primary key
* `movieId`: Foreign key linking to the `movies` table
* `roomId`: Foreign key link to table `rooms`
* `startTime`: Showtime start time
* `ticketPrice`: Ticket price

#### 4. seats (Seat)

Manage each seat in the screening room:

* `seatId`: Primary key
* `seatName`: Seat name (eg: A1, B2)
* `roomId`: Foreign key to table `rooms`
* `seatType`: Seat type (regular, VIP,...)
* `row`: Position in row
* `column`: Position in column

#### 5. tickets (Ticket)

Save customer ticket booking information:

* `ticketId`: Primary key
* `seatId`: Seat booked (foreign key to table `seats`)
* `customerPhone`: Customer phone number
* `showTimeId`: Corresponding showtime
* `isBooked`: Booking status (1 = booked, 0 = not booked)

## TESTING
### 1. TicketDAO Testing
To validate the **thread-safety and correctness** of the `TicketDAO` class by simulating concurrent ticket booking and cancellation operations across multiple threads and users.
* Simulates **8 concurrent users**, each performing:

  * Randomized order booking of 56 tickets.
  * Randomized cancellation of previously booked tickets.
* Uses in-memory **H2 database** for isolated and repeatable tests.

#### Test Scenario

* 56 tickets are created for a single showtime.
* Each thread performs operations with small randomized delays to mimic real-world race conditions.
* Final verification checks:

  * Tickets are only booked once.
  * Only the user who booked the ticket can cancel it.
  * Database state matches in-memory records after all threads finish.

#### Results
* No overbooking occurs.
* All ticket cancellations are valid (booked and owned by the cancelling user).
* Application-level locks (ReentrantLock) **effectively prevent race conditions** even without database-level locking.

### 2. ShowTimeDAO Testing

To verify that the `ShowTimeDAO` correctly handles **concurrent insertion** attempts and **prevents duplicate showtimes**, particularly for the same room and time combinations.
* Spawns **10 concurrent threads**, each attempting to insert **9 showtimes**.
* All showtimes are scheduled at the same day but at different hours (e.g., 20:00, 21:00, ...).
* All insertions target the same **movie (id=1)** and **room (id=1)**.
* Uses in-memory **H2 database** for isolated and repeatable tests.
* The method uses `Executors` and `Future` to track concurrent insertion outcomes.

#### Test Scenario
* Creates and shuffles 9 showtime objects to ensure insertion order varies.
* Each thread attempts to insert these showtimes.
* Final Verification Checks:

  * Total number of successful insertions should be 9 (one for each distinct time).
  * No duplicate showtimes exist in the database (i.e., no two entries have the same startTime for the same roomId).

#### Results
* Despite 10 threads running in parallel, only one unique set of 9 showtimes should be successfully inserted (one per start time).
* The **expected successful insertions**: `9`.
* All other attempts fail due to duplication logic.


## APPLICATION SCREEN CAPTURES
<p align="center">
  <em>Landing page of Cinema Booking System. The user can choose between 2 modes.</em>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/2ef69216-6ecc-4245-9edc-339a947b5349" alt="Landing Page" width="800"/>
</p>


### 1. Admin Mode
#### *Room page*: The admin can add a new room and see the room map
<p align="center">
  <img src="https://github.com/user-attachments/assets/a042dd4a-05a9-4265-a98a-0b45fc67a8c7" alt="Room Page" width="800"/>
</p>

<p align="center">
  <img src="https://github.com/user-attachments/assets/1e021a61-fe1b-4d80-8f22-234325ba2554" alt="Room Page" width="800"/>
</p>

#### *Movie page*: The admin can add a new movie and see a movie's showtimes
<p align="center">
  <img src="https://github.com/user-attachments/assets/c895a901-0231-4b98-b930-57adbdeaaee9" alt="Movie Page" width="800"/>
</p>

#### *Showtime page*: The admin can add a new showtime, as well as view the booking status of a showtime
<p align="center">
  <img src="https://github.com/user-attachments/assets/56137544-d177-406b-9535-1d1f0b0898c5" alt="Showtime Page" width="800"/>
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/b5dfb1a1-3f1e-4ddd-b4d5-1993152dc3b9" alt="Showtime Page" width="800"/>
</p>

### 2. Customer Mode
#### *Movie page*: The customer can see a movie's showtimes and book tickets
<p align="center">
  <img src="https://github.com/user-attachments/assets/7b059348-f4a0-4472-8629-f3b79b881a02" alt="Movie Page" width="800"/>
</p>

#### *Booking page*: The customer can book multiple tickets for a showtime
<p align="center">
  <img src="https://github.com/user-attachments/assets/7f6471f6-4ff7-4564-8161-53e949d796a6" alt="Booking Page" width="800"/>
</p>

#### *Ticket page*: The customer can cancel multiple tickets
<p align="center">
  <img src="https://github.com/user-attachments/assets/2a8efd75-648d-4b7c-b6e7-20ff26a88d62" alt="Booking Page" width="800"/>
</p>

