# KIOTY — Shopping Cart Optimizer

KIOTY is an intelligent shopping cart optimization system designed to help users maximize the value and importance of items within their specified budget using a 0/1 Knapsack Dynamic Programming algorithm.

The backend is built in **Pure Java 21** (replacing the legacy Python Flask implementation) with zero external runtime dependencies, providing blazing-fast execution, virtual thread concurrency, and reliable persistence.

---

## 🚀 Key Features

- **Java 21 Backend**: Pure Java implementation using JDK's built-in `HttpServer` and Java 21 Virtual Threads.
- **Zero External Dependencies**: Compiles and runs out-of-the-box on any standard JDK 21 installation without needing pip, virtual environments, or external jar downloads.
- **0/1 Knapsack Dynamic Programming**: Optimal item selection maximizing importance value under a strict budget constraint.
- **Persistence & Logging**:
  - Daily session archives: `logs/YYYY-MM-DD/session_<id>.json`
  - User history log: `logs/history.json`
  - Rolling application log: `logs/app.log`
- **Full CORS & REST Support**: Compatible with web browsers and frontends across domains.
- **Maven & Build Scripts Included**: Includes `pom.xml`, Windows batch files (`run.bat`, `build.bat`), and Unix shell scripts (`run.sh`, `build.sh`).

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
│   │   │   │   ├── Item.java                 # Item Data Model
│   │   │   │   └── OptimizeResult.java       # Session Result Model
│   │   │   ├── service/
│   │   │   │   └── HistoryService.java       # Session & History File Manager
│   │   │   ├── util/
│   │   │   │   ├── AppLogger.java            # Console & File Logger
│   │   │   │   ├── JsonUtil.java             # Zero-dependency JSON parser & serializer
│   │   │   │   └── Validator.java            # Request validation rules
│   │   │   └── App.java                      # Main HTTP Server & REST API Handlers
│   │   └── test/java/com/kioty/
│   │       └── AppTest.java                  # Test suite for algorithms and utilities
│   ├── bin/                                  # Compiled .class binaries
│   ├── logs/                                 # Runtime logs and history JSON
│   ├── build.bat / build.sh                  # One-click build scripts
│   ├── run.bat / run.sh                      # One-click run scripts
│   ├── kioty-optimizer.jar                   # Standalone runnable JAR
│   ├── pom.xml                               # Maven project configuration
│   └── Procfile                              # Cloud deployment configuration
├── frontend/
│   ├── index.html                            # Landing page
│   ├── app.html                              # Interactive Cart Optimizer UI
│   ├── history.html                          # Past sessions viewer
│   ├── script.js                             # Frontend logic & API interaction
│   └── style.css                             # Responsive styling & themes
├── run.bat                                   # Quick root launcher
└── README.md
```

---

## 🛠️ How to Build and Run

### Prerequisites
- **Java JDK 21** or later (`java -version`, `javac -version`)

### Quick Start (Windows)
Double-click `run.bat` in the root folder or execute in terminal:
```cmd
.\run.bat
```
The server will start at: `http://localhost:5050`

### Build & Package Manually
To re-compile and generate `kioty-optimizer.jar`:
```cmd
cd backend
build.bat
```

To run the packaged JAR:
```cmd
java -jar backend/kioty-optimizer.jar
```

### Running with Maven (Optional)
If you have Apache Maven installed:
```cmd
cd backend
mvn clean package
java -jar target/kioty-optimizer-1.0.0.jar
```

### Running Tests
To run the automated verification test suite:
```cmd
javac -encoding UTF-8 -d backend/bin -cp backend/bin backend/src/test/java/com/kioty/AppTest.java
java -cp backend/bin com.kioty.AppTest
```

---

## 📡 API Endpoints

### 1. Health Check
- **Endpoint**: `GET /`
- **Response**: `200 OK`
```
🟢 Shopping Cart Optimizer API is running.
```

### 2. Optimize Cart
- **Endpoint**: `POST /optimize`
- **Headers**: `Content-Type: application/json`
- **Request Body**:
```json
{
  "budget": 100,
  "items": [
    { "name": "Milk", "price": 40, "value": 8 },
    { "name": "Bread", "price": 30, "value": 5 },
    { "name": "Eggs", "price": 50, "value": 7 }
  ]
}
```
- **Response**: `200 OK`
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

### 3. Session History
- **Endpoint**: `GET /history`
- **Response**: `200 OK`
```json
{
  "history": [
    {
      "sessionId": "0bdf9ee6",
      "timestamp": "2026-10-27T17:09:04.276589",
      "budget": 100,
      "selectedItems": [ ... ],
      "totalPrice": 90,
      "totalValue": 15,
      "count": 2
    }
  ]
}
```

---

## 🌐 Frontend Usage

1. Open `frontend/index.html` or `frontend/app.html` in your web browser.
2. Enter your total budget (₹).
3. Add items with Name, Price, and Importance (1–10).
4. Click **Optimize Cart** to calculate the best combination of items.
5. Click **View History** to view past optimization sessions saved by the Java backend.