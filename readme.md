# TicTacToe Game in Java

A versatile Java implementation of the classic TicTacToe game, featuring both single-player and multiplayer modes. The single-player version allows users to play against an intelligent computer opponent with a graphical user interface (GUI). The multiplayer version extends the gameplay by enabling two players to compete against each other over a network using basic socket programming.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
  - [Single-Player Version](#single-player-version)
  - [Multiplayer Version](#multiplayer-version)
- [Usage](#usage)
  - [Single-Player Mode](#single-player-mode)
  - [Multiplayer Mode](#multiplayer-mode)
- [Directory Structure](#directory-structure)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)

## Features

### Single-Player Version

- **Play Against Computer:** Challenge an intelligent computer opponent.
- **Graphical User Interface (GUI):** Interactive and user-friendly interface built with Java Swing.
- **Real-Time Gameplay:** Instantaneous updates and move validations.
- **Win/Draw Detection:** Automatically detects and announces game outcomes.

### Multiplayer Version

- **Two-Player Gameplay:** Compete against another human player.
- **Client-Server Architecture:** Robust networking using socket programming.
- **Real-Time Communication:** Seamless synchronization between client and server.
- **Turn Management:** Ensures fair alternating turns between players.
- **Game State Persistence:** Maintains the current state of the game across the network.

## Prerequisites

- **Java Development Kit (JDK):** Version 8 or higher. [Download JDK](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
- **Operating System:** Platform-independent (Windows, macOS, Linux).
- **Internet Connection:** Required for multiplayer mode to allow network communication.

## Installation

### Single-Player Version

1. **Clone the Repository**

   ```bash
   git clone https://github.com/aaqwesas/TicTacToe.git
   ```

2. **Navigate to the Project Directory**

   ```bash
   cd TicTacToeJava
   ```

3. **Compile the Java File**

   ```bash
   javac TicTacToeGame.java
   ```

4. **Run the Application**

   ```bash
   java TicTacToeGame
   ```

### Multiplayer Version

1. **Navigate to the Multiplayer Directory**

   ```bash
   cd Multiplayer
   ```

2. **Compile the Server and Client Java Files**

   ```bash
   javac TicTacToeServer.java
   javac TicTacToeClient.java
   ```

## Usage

### Single-Player Mode

1. **Run the Application**

   After compiling, execute the following command:

   ```bash
   java TicTacToeGame
   ```

2. **Gameplay Instructions**

   - **Making a Move:** Click on an empty cell in the TicTacToe grid to place your mark (X or O).
   - **Computer's Move:** The computer will automatically make its move after yours.
   - **Winning the Game:** The game detects and announces when a player has won or if the game ends in a draw.
   - **Restarting the Game:** After a game concludes, you can choose to play again without restarting the application.

### Multiplayer Mode

The multiplayer version requires running both the server and client applications, potentially on separate machines connected over a network.

#### **Step 1: Start the Server**

1. **Navigate to the Multiplayer Directory**

   ```bash
   cd Multiplayer
   ```

2. **Run the Server Application**

   ```bash
   java TicTacToeServer
   ```

   - The server will start and listen for incoming client connections on a specified port (default: 7000).

#### **Step 2: Start the Client**

1. **On the Client Machine or Another Terminal**

2. **Run the Client Application**

   ```bash
   java TicTacToeClient
   ```

3. **Server Details**

   - **Host IP:** By default it is 127.0.0.1
   - **Port Number:** By default it is PORT 7000

4. **Gameplay Instructions**

   - **Making a Move:** Click on an empty cell in the TicTacToe grid to place your mark (X or O).
   - **Opponent's Move:** The connected player will see your move reflected on their grid, and vice versa.
   - **Winning the Game:** The game detects and announces when a player has won or if the game ends in a draw.
   - **Exiting the Game:** Close the client application to disconnect from the server.

## Directory Structure

```
TicTacToeJava/
├── TicTacToeGame.java
├── Multiplayer/
│   ├── TicTacToeClient.java
│   └── TicTacToeServer.java
└── README.md
```

- **TicTacToeGame.java:** Single-player version of the game.
- **Multiplayer/**: Directory containing the multiplayer version files.
  - **TicTacToeClient.java:** Client-side application for multiplayer mode.
  - **TicTacToeServer.java:** Server-side application managing multiplayer connections.

## Contributing

Contributions are welcome! If you'd like to enhance the game or fix any issues, please follow these steps:

1. **Fork the Repository**

2. **Create a New Branch**

   ```bash
   git checkout -b feature/YourFeatureName
   ```

3. **Make Your Changes**

4. **Commit Your Changes**

   ```bash
   git commit -m "Add feature: YourFeatureName"
   ```

5. **Push to the Branch**

   ```bash
   git push origin feature/YourFeatureName
   ```

6. **Open a Pull Request**

   - Describe your changes and submit for review.

## License

This project is licensed under the [MIT License](LICENSE). You are free to use, modify, and distribute this software as per the terms of the license.

## Acknowledgments

- **Java Swing:** For providing the framework to build the graphical user interface.
- **Socket Programming:** Leveraged for establishing network communication in the multiplayer mode.
- **Open-Source Community:** For inspiring and guiding through various resources and tutorials.
