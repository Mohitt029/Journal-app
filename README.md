# Journal App

A journal application built with a React frontend and Spring Boot backend. This app allows users to manage personal journal entries securely with features like user authentication, journal management, and sentiment analysis, backed by a MongoDB database and Redis caching for performance.

## Features




- **User Authentication**: Secure login and registration using **JWT** (JSON Web Tokens) for session management.



- **Journal Management**: Create, edit, and delete journal entries with a user-friendly interface.



- **Sentiment Analysis**: Analyze the sentiment of journal entries to understand emotional tone (e.g., happy, sad) using the `Sentiment`: enum.



- **Weather Integration**: Fetch weather data for journal entries via the for `WeatherService`: to contextualize entries (e.g., "It was sunny when I wrote this").



- **Text-to-Speech**: Convert journal entries to speech using the `TextToSpeechService` for accessibility.



- **Scheduled Tasks:**





  - **Blacklist Cleanup:** Automatically clean up expired JWT tokens using `BlacklistCleanupScheduler`.



  **User Notifications:** Send scheduled email reminders or updates via `UserScheduler` and `EmailService`.



- **Caching**: Improve performance with Redis caching for frequently accessed data, managed by `RedisService`.


- **MongoDB Database**: Store journal entries and user data in a scalable **MongoDB** database.

## Tech Stack

### Frontend





- **React**: For building the interactive user interface.



- **Axios**: For making HTTP requests to the backend.



- **Tailwind CSS**: For styling (via inline styles in `styles.css`).

### Backend





- **Spring Boot**: For building the RESTful API and handling business logic.



- **MongoDB**: NoSQL database for storing user and journal data.



- **Redis**: In-memory caching for faster data retrieval.



- **JWT**: For secure authentication and authorization.

### Other Tools





- **Maven**: Dependency management for the backend (configured in pom.xml).



- **http-server**: Lightweight server to serve the frontend during development.
