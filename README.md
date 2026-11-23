# KIOTY — Shopping Cart Optimizer

KIOTY is a smart shopping cart optimization tool that helps you get the most value out of your budget. Add items with their price and importance, set a budget, and KIOTY uses a **0/1 Knapsack Dynamic Programming** algorithm to recommend the mathematically optimal combination of items.

---

## 🚀 Key Features

- **Java 21 Backend** — Built using JDK's built-in `HttpServer` with Java 21 Virtual Threads for high-performance concurrency.
- **Zero External Dependencies** — No frameworks, no package managers. Runs on any standard JDK 21 installation.
- **0/1 Knapsack DP Algorithm** — Guarantees the globally optimal item selection within your budget.
- **Persistent Session Logging**:
  - Daily session archives: `logs/YYYY-MM-DD/session_<id>.json`
  - User history log: `logs/history.json`
  - Rolling application log: `logs/app.log`
- **Full CORS & REST API** — Compatible with browsers and frontends across domains.
- **Polished Frontend** — Responsive UI with dark mode, animated item management, real-time budget progress bar, and history viewer.

---

## 📂 Project Structure

```
KIOTY/
├── backend/
│   ├── src/
│   │   ├── main/java/com/kioty/
│   │   │   ├── algorithm/
│   │   │   │   └── KnapsackOptimizer.java    # 0/1 Knapsack DP Algorithm
│   │   │   ├── model/
│   │   │   │   ├── Item.java                 # Item data model
│   │   │   │   └── OptimizeResult.java       # Session result model
│   │   │   ├── service/
│   │   │   │   └── HistoryService.java       # Session & history persistence
│   │   │   ├── util/
│   │   │   │   ├── AppLogger.java            # Thread-safe console & file logger
│   │   │   │   ├── JsonUtil.java             # Zero-dependency JSON parser/serializer
│   │   │   │   └── Validator.java            # Request validation
│   │   │   └── App.java                      # HTTP server & REST API handlers
│   │   └── test/java/com/kioty/
│   │       └── AppTest.java                  # Verification test suite
│   ├── build.bat / build.sh                  # One-click build scripts
│   ├── run.bat / run.sh                      # One-click run scripts
│   ├── kioty-optimizer.jar                   # Standalone runnable JAR
│   ├── pom.xml                               # Maven project file (optional)
│   └── Procfile                              # Cloud deployment config
├── frontend/
│   ├── index.html                            # Landing page
│   ├── app.html                              # Cart Optimizer UI
│   ├── history.html                          # Past sessions viewer
│   ├── script.js                             # Frontend logic & API interaction
│   └── style.css                             # Responsive styling & themes
├── run.bat                                   # Root launcher (starts backend)
└── README.md
```

---

## 🛠️ How to Build and Run

### Prerequisites
- **Java JDK 21** or later — verify with `java -version` and `javac -version`

### Quick Start (Windows)
Double-click `run.bat` in the root folder, or run in terminal:
```cmd
.\run.bat
```
The API server starts at: **`http://localhost:5050`**

Open `frontend/index.html` in your browser to launch the UI.

### Manual Build & Package
Compile and package the runnable JAR:
```cmd
cd backend
build.bat
```

Run the packaged JAR directly:
```cmd
java -jar backend/kioty-optimizer.jar
```

### Maven (Optional)
```cmd
cd backend
mvn clean package
java -jar target/kioty-optimizer-1.0.0.jar
```

### Run Tests
```cmd
javac -encoding UTF-8 -d backend/bin -cp backend/bin backend/src/test/java/com/kioty/AppTest.java
java -cp backend/bin com.kioty.AppTest
```

---

## 📡 API Endpoints

### `GET /`
Health check — confirms the API is running.
```
🟢 Shopping Cart Optimizer API is running.
```

### `POST /optimize`
Runs the 0/1 Knapsack DP optimizer and returns the best item combination.

**Request:**
```json
{
  "budget": 100,
  "items": [
    { "name": "Milk",  "price": 40, "value": 8 },
    { "name": "Bread", "price": 30, "value": 5 },
    { "name": "Eggs",  "price": 50, "value": 7 }
  ]
}
```

**Response:**
```json
{
  "sessionId": "0bdf9ee6",
  "timestamp": "2026-10-27T17:09:04.276589",
  "budget": 100,
  "selectedItems": [
    { "name": "Milk", "price": 40, "value": 8 },
    { "name": "Eggs", "price": 50, "value": 7 }
  ],
  "totalPrice": 90,
  "totalValue": 15,
  "count": 2
}
```

### `GET /history`
Returns all past optimization sessions for the current user.
```json
{
  "history": [
    {
      "sessionId": "0bdf9ee6",
      "timestamp": "2026-10-27T17:09:04.276589",
      "budget": 100,
      "totalPrice": 90,
      "totalValue": 15,
      "count": 2,
      "selectedItems": [ ... ]
    }
  ]
}
```

---

## 🌐 Using the Frontend

1. Open `frontend/index.html` in your browser.
2. Click **Launch Optimizer** to open the cart page.
3. Enter your total budget (₹).
4. Add items with a **Name**, **Price (₹)**, and **Importance score (1–10)**.
5. Click **⚡ Optimize Cart** — KIOTY instantly picks the best combination.
6. View your past sessions on the **📜 History** page.
