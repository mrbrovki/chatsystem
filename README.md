# 💬 TooChatty - Real-Time Chat System

A modern, feature-rich chat application built with **Spring Boot** and **React**, enabling real-time communication with support for direct messaging, group chats, and AI-powered bot integration.

🔗 **Live Demo:** [https://toochatty.ngrok.app](https://toochatty.ngrok.app)

---

## ✨ Features

- 🔐 **Secure Authentication** - JWT-based authentication with role-based access control
- 💬 **Real-Time Messaging** - WebSocket support via STOMP for instant message delivery
- 👥 **Group Chats** - Create and manage group conversations
- 🤖 **AI Bot Integration** - Built-in bot service with DeepSeek AI integration
- 📸 **Avatar Management** - Upload and manage user avatars with AWS S3
- 🌙 **Dark/Light Themes** - Customizable UI themes
- 📱 **Responsive Design** - Works seamlessly on desktop and mobile devices
- 💾 **MongoDB Database** - Persistent data storage with MongoDB Atlas
- ⚡ **Fast & Modern Stack** - Vite + React for optimal frontend performance

---

## 🎥 Demo & Screenshots

### Authentication & Bot Integration

![Login and Bots Demo](demos/Login%20and%20Bots.gif)

### Real-Time Private Messaging

![Private Messaging Demo](demos/Private%20msg.gif)

---

## �🏗️ Architecture

### Tech Stack

**Backend:**

- Java 21
- Spring Boot 3.3.5
- Spring WebSocket & STOMP
- Spring Security with JWT
- MongoDB
- Lombok 1.18.30
- JWT (JJWT) 0.11.5
- AWS SDK S3 1.12.767

**Frontend:**

- React 18.3.1
- TypeScript 5.5.3
- Vite 5.4.0
- Styled Components 6.1.11
- STOMP Client 7.0.0

**Infrastructure:**

- Docker & Docker Compose
- Maven 3.9.9
- Nginx (Reverse Proxy)
- AWS S3 (File Storage)
- ngrok (Public Tunnel)

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 21 (for local development)
- Node.js 18+ (for frontend development)
- MongoDB Atlas account (optional, for cloud database)

### Running with Docker

1. **Clone the repository**

   ```bash
   git clone <repo-url>
   cd chatsystem
   ```

2. **Start the application**

   ```bash
   docker-compose up --build
   ```

   The application will be available at:
   - Frontend: `http://localhost:12345`
   - Backend API: `http://localhost:8970`

### Local Development

#### Backend

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

#### Frontend

```bash
cd chatsystem-fr

# Install dependencies
npm install

# Development server
npm run dev

# Build for production
npm run build
```

---

## 📁 Project Structure

```
chatsystem/
├── src/
│   └── main/
│       ├── java/com/example/chatsystem/
│       │   ├── controller/          # REST & WebSocket endpoints
│       │   ├── service/             # Business logic
│       │   ├── repository/          # Data access layer
│       │   ├── model/               # Entity models
│       │   ├── dto/                 # Data transfer objects
│       │   ├── security/            # JWT & authentication
│       │   ├── config/              # Configuration classes
│       │   ├── bot/                 # AI bot integration
│       │   └── utils/               # Utility classes
│       └── resources/
│           └── application.properties
├── chatsystem-fr/                   # React frontend
│   └── src/
│       ├── auth/                    # Authentication pages
│       ├── chat/                    # Chat components
│       ├── components/              # Reusable UI components
│       ├── context/                 # React context state
│       ├── hooks/                   # Custom React hooks
│       ├── panel/                   # Panel/sidebar components
│       └── utils/                   # Frontend utilities
├── docker-compose.yml
├── Dockerfile
└── nginx.conf
```

---

## 🔑 Key Endpoints

### Authentication

- `POST /api/auth/login` - User login
- `POST /api/auth/signup` - User registration

### Chat

- `GET /api/chats` - Get all chats
- `POST /api/chats` - Create new chat
- `GET /api/chats/{id}` - Get chat details

### Messages

- `GET /api/messages/{chatId}` - Get messages for a chat
- `POST /api/messages` - Send a message
- `WebSocket /ws/chat` - Real-time messaging via STOMP

### Users

- `GET /api/users/{id}` - Get user profile
- `PUT /api/users/{id}` - Update user profile
- `POST /api/users/{id}/avatar` - Upload avatar

---

## 🔐 Environment Variables

Configure these in your `.env` or `docker-compose.yml`:

```env
# Database
SPRING_DATA_MONGODB_URI=mongodb+srv://username:password@cluster.mongodb.net/chat
SPRING_DATA_MONGODB_DATABASE=chat

# Security
JWT_SECRET=your-secret-key-here

# AWS S3
AWS_ACCESS_KEY_ID=your-access-key
AWS_SECRET_ACCESS_KEY=your-secret-key
AWS_BUCKET_AVATARS=your-bucket-name
AWS_AVATARS_URL=https://your-bucket.s3.region.amazonaws.com/

# Server
SERVER_PORT=8970

# CORS
CORS_ALLOWED_ORIGINS=https://toochatty.ngrok.app

# AI/Bot
OPENAI_API_SERVER=http://your-ai-server:port
OPENAI_API_KEY=your-api-key
OPENAI_API_MODEL=deepseek/deepseek-r1
```

---

## 📝 API Documentation

### WebSocket Connection

```javascript
// Connect to WebSocket
const client = new Client({
  brokerURL: "ws://localhost:8970/ws",
  debug: function (str) {
    console.log(str);
  },
  reconnectDelay: 5000,
});

// Subscribe to messages
client.subscribe("/user/queue/messages", (message) => {
  console.log(message.body);
});

// Send a message
client.publish({
  destination: "/app/chat.sendMessage",
  body: JSON.stringify({
    content: "Hello",
    chatId: "chat-id",
  }),
});
```

---

## 🐛 Troubleshooting

### MongoDB Connection Issues

- Verify MongoDB URI in environment variables
- Check IP whitelist in MongoDB Atlas
- Ensure VPN/network connectivity

### WebSocket Connection Failed

- Check CORS_ALLOWED_ORIGINS matches your domain
- Verify Nginx proxy configuration
- Check firewall rules

### Avatar Upload Issues

- Verify AWS credentials and S3 bucket permissions
- Check bucket CORS configuration
- Ensure file size limits are appropriate

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

## 📄 License

This project is licensed under the MIT License.

---

## 📧 Contact & Support

For questions or support, please reach out through the repository's issue tracker.

---

**Made with ❤️ by me :)**
