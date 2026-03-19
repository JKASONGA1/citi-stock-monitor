# 📈 Citi DJIA Real-Time Stock Monitor

A Java desktop application built as part of the **Citi Technology Analyst Program** simulation. The app queries the Dow Jones Industrial Average (^DJI) from Yahoo Finance every 5 seconds, stores readings in a bounded queue, and displays a live-updating line chart using JavaFX.

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![Gradle](https://img.shields.io/badge/Gradle-9.4.1-02303A?logo=gradle)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange)
![License](https://img.shields.io/badge/license-MIT-green)

---

## 🖥️ Demo

> Live DJIA price queried from Yahoo Finance every 5 seconds, plotted in real time.

```
======================================================
  Citi DJIA Real-Time Monitor
  Ticker  : ^DJI
  Polling : every 5s  |  Queue cap: 100
  Press Ctrl+C or close the window to stop.
======================================================

#      Time          DJIA Price       Queue Size
------------------------------------------------
1      15:57:23      $    46,021.43            1
2      15:57:28      $    46,021.43            2
3      15:57:34      $    46,021.43            3
```

---

## 🗂️ Project Structure

```
citi-stock-monitor/
├── app/
│   ├── build.gradle                        # Gradle build config + JavaFX plugin
│   └── src/
│       ├── main/java/org/example/
│       │   └── App.java                    # Main application
│       └── test/java/org/example/
│           └── AppTest.java                # Unit tests
├── gradle.properties                       # Disables configuration cache for JavaFX
└── README.md
```

---

## ⚙️ How It Works

1. **HTTP fetch** — Java's built-in `HttpClient` sends a request to Yahoo Finance's `/v8/finance/chart` endpoint with a browser `User-Agent` header to avoid rate limiting (HTTP 429).
2. **Price parsing** — A `Pattern` regex extracts `regularMarketPrice` from the JSON response.
3. **Bounded queue** — Each reading is wrapped in a `StockRecord` (tick, timestamp, price) and pushed into a `LinkedList`-backed `Queue` capped at 100 entries. When full, the oldest entry is dropped via `poll()`.
4. **Live chart** — JavaFX renders a `LineChart` with query number on the X axis and price on the Y axis. The polling runs on a background daemon thread; chart updates are dispatched back to the JavaFX thread via `Platform.runLater()`.

---

## 🚀 Getting Started

### Prerequisites

- Java 21 ([Eclipse Adoptium](https://adoptium.net/) recommended)
- Gradle 9.x ([gradle.org](https://gradle.org/releases/))

### Run the app

```bash
# Clone the repo
git clone https://github.com/YOUR_USERNAME/citi-stock-monitor.git
cd citi-stock-monitor

# Build
gradle build

# Run
gradle run
```

A desktop window will open with the live chart. Stock prices update every 5 seconds. Press `Ctrl+C` or close the window to stop.

---

## 🧠 Key Concepts Demonstrated

| Concept | Implementation |
|---|---|
| REST API integration | `HttpClient` querying Yahoo Finance |
| Rate limit handling | Custom `User-Agent` header to avoid HTTP 429 |
| Data structures | Bounded `Queue` via `LinkedList` with `maxlen` logic |
| Multithreading | Background daemon thread + `Platform.runLater()` |
| Desktop GUI | JavaFX `LineChart` with live data binding |
| Build tooling | Gradle with JavaFX plugin and JUnit Jupiter |

---

## 📋 Background — Citi Technology Analyst Program

This project was built as part of a four-task simulation of Citi's Technology Analyst internship program:

| Task | Deliverable |
|---|---|
| 1 | UML state diagram of the loan management lifecycle |
| 2 | Machine learning credit risk modeling proposal |
| 3 | Java app querying and storing live DJIA prices in a queue |
| 4 | JavaFX live line chart added to the stock monitor (this repo) |

---

## 👤 Author

**John Smith**
Cybersecurity Graduate — SMU Bootcamp
[LinkedIn](https://linkedin.com/in/YOUR_PROFILE) · [GitHub](https://github.com/YOUR_USERNAME)
