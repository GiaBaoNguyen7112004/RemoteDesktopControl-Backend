# Remote Desktop Control 

This application enables an **admin** to manage and monitor multiple **staff** computers. It provides functionalities for real-time screen viewing, remote control, and error logging when staff access prohibited applications or domains. The app is secured using **Spring Security** and **JWT** for authentication and authorization.

## Features

- **Admin Management**: Admin can monitor and control multiple staff machines.
- **Screen Viewing**: View staff desktops in real-time.
- **Remote Control**: Remotely control the staff machine's mouse and keyboard.
- **Error Logging**: Detect and log access to restricted applications or domains.
- **JWT Authentication**: Secure access using token-based authentication with Spring Security.

## Technologies Used

- **Java Spring Boot**: Backend framework.
- **Spring Security**: API security.
- **JWT (JSON Web Tokens)**: User authentication.
- **WebSocket**: Real-time communication.
- **MySQL**: Database storage.

## Frontend repo: 
- https://github.com/Dangtruong-DUT/Employee-Monitoring-Desktop-App-Electron.git

## Getting Started

### Prerequisites

- JDK 11 or higher
- MySQL (configured in `application.properties`)
- Docker (optional for containerization)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/GiaBaoNguyen7112004/RemoteDeskTopControlServer.git
2. Build the application
   ```bash
   mvn clean install
3. Run the application
   ```bash
   mvn spring-boot:run

  After running this command, the application will start, and you should be able to access it at:
  ```bash
  http://localhost:8088/rdp



   
