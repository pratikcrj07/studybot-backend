# Studybot Backend

A simple, secure, and modular backend for **Studybot**, built with **Spring Boot** and **MySQL (Aiven)**. This backend handles authentication, API integration, email notifications, and database operations for the Studybot platform.

---

## Features

* JWT-based authentication and authorization
* Secure handling of sensitive credentials via `.env`
* Integration with Groq AI API for chatbot responses
* SMTP email notifications (Gmail SMTP)
* MySQL database support with JPA/Hibernate
* Environment-based configuration for easy deployment
* Docker-ready (optional, can be added later)

---

## Tech Stack

* **Backend:** Java 17, Spring Boot 3.5
* **Database:** MySQL (via Aiven Cloud)
* **Authentication:** JWT
* **Email:** Spring Boot Mail
* **Environment Management:** `dotenv-java`

---

## Setup

### Prerequisites

* Java 17+
* Maven 3.8+
* Git
* Aiven MySQL or local MySQL database

### Steps

1. **Clone the repository**

```bash
git clone https://github.com/pratikcrj07/studybot-backend.git
cd studybot-backend
```

2. **Create environment file**

Copy `bot.env.example` to `bot.env` and update your credentials:

```bash
cp bot.env.example bot.env
```

Edit `bot.env` with your API keys, database credentials, and SMTP settings.

3. **Run the application**

```bash
mvn clean install
mvn spring-boot:run
```

The backend will start on port `8080` (or your configured port).

---

## Environment Variables

All sensitive credentials and configuration should be stored in `bot.env`:

* `SERVER_PORT` — Backend server port
* `SPRING_APPLICATION_NAME` — Application name
* `JWT_SECRET` — Secret for JWT tokens
* `GROQ_API_URL` / `GROQ_API_KEY` — Groq AI integration
* `SPRING_MAIL_*` — Gmail SMTP configuration
* `SPRING_DATASOURCE_*` — MySQL database configuration

**Note:** Never commit `bot.env` to public repositories. Use `bot.env.example` as reference.

---

## Database

* Using MySQL via Aiven Cloud
* JPA/Hibernate handles schema updates automatically (`spring.jpa.hibernate.ddl-auto=update`)
* Tables will be created automatically on startup

---

## Contributing

* Fork the repository
* Create feature branches for new functionality
* Commit with clear, concise messages
* Push and open pull requests

---

## License

This project is open-source. Feel free to use and modify it under [MIT License] or your chosen license.

---

### Optional Notes

* Add Docker support for easier deployment
* Add unit/integration tests for endpoints
* Add logging and monitoring for production readiness
