# DSA Tracker — A2Z Sheet

A Spring Boot web app to track DSA preparation using the **A2Z DSA Sheet** curriculum.

## Features

- **474 problems** across 18 A2Z sheet topics
- **Sorted by difficulty** — Easy → Medium → Hard within each topic
- **Progress tracking** — check off problems to update solved count
- **Revision notes** — write and save your own notes per problem
- **Search** — topics, patterns, problem titles, and your notes

## Run

```bash
cd dsa-tracker
mvn spring-boot:run
```

Open http://localhost:8080

## Tech Stack

- Java 21
- Spring Boot 3.4 (Web, JPA, Thymeleaf, Validation)
- H2 Database
- Vanilla CSS + JavaScript

## Prerequisites

- Java 21+
- Maven 3.9+

## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Dashboard |
| GET | `/api/topics` | All topics with problems |
| GET | `/api/topics/stats` | Dashboard stats |
| PUT | `/api/problems/{id}/toggle` | Toggle solved status |
| PUT | `/api/problems/{id}/revision-notes` | Save revision notes |

## Data

Problem data is loaded from `src/main/resources/data/a2z-sheet.json` (A2Z DSA Sheet topics and LeetCode/GFG links).
