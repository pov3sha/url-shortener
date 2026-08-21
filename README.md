# URL Shortener

A simple, complete, and deployable URL Shortener web application built with **Java 21**, **Spring Boot 3**, **Spring Data JPA**, and **PostgreSQL**.

---

## Live Demo

> **Live Demo:** `https://github.com/pov3sha/url-shortener`

---

## Features

* **URL Shortening**: Converts long HTTP/HTTPS URLs into compact 6-character short URLs.
* **HTTP Redirects**: Instant 302 redirection from short link to destination URL.
* **Click Tracking & Analytics**: Tracks total click counts, creation timestamps, and last access timestamps.
* **Base62 Generation**: Secure, collision-checked 6-character random short code generator.
* **URL Validation**: Rejects invalid protocols (e.g. `ftp://`, `javascript:`, malformed URLs).
* **In-Memory Rate Limiting**: Restricts requests to 10 short URL creations per minute per IP address.
* **PostgreSQL Persistence**: Automatically managed database schema using Hibernate JPA.
* **Docker & Docker Compose**: Single-command containerized local environment.

---

## Tech Stack

| Technology | Purpose |
| --- | --- |
| **Java 21** | Core Programming Language |
| **Spring Boot 3.3** | Web Application & REST Framework |
| **Spring Data JPA** | Object-Relational Mapping & Database Layer |
| **PostgreSQL** | Relational Database Persistence |
| **HTML / CSS / JavaScript** | Lightweight Frontend Interface |
| **Docker & Docker Compose** | Containerization & Local Multi-Container Setup |
| **Maven** | Dependency Management & Build Automation |


