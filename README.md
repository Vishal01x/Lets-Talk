# Chat App with Priority Messaging and Scheduled Delivery

This is a feature-rich, user-centric messaging app that enables seamless communication between users, groups, and communities. With unique features like message prioritization, scheduled message delivery, real-time status updates, and robust message management, the app offers a comprehensive solution for modern messaging needs. Built using Kotlin, Jetpack Compose, and Firebase, the app ensures smooth, real-time communication and data synchronization, while also integrating advanced security measures for user privacy.

## Features

### **Message Prioritization**
- **Prioritize Messages**: Users can classify messages into categories such as **Urgent**, **Important**, and **Normal** to help ensure that the most important communications are highlighted.
- **Prioritize People, Groups, and Communities**: Users can assign priority to specific users, groups, and communities. This guarantees that messages and notifications from high-priority sources always appear first, even when the app is active in the background or closed.

### **Scheduled Messaging**
- **Pre-set Message Delivery**: Users can schedule messages to be sent at a specific time, allowing for advanced communication planning. For instance, you can set birthday wishes or reminders and the app will send them at the pre-scheduled time, eliminating the need to wait and manually send them.
  
### **Communication Modes**
- **Text, Audio, and Video**: The app supports a wide range of communication formats, including text, audio, video, images, GIFs, and reactions to messages, offering versatile ways to connect with others.
- **Voice and Video Calls**: In addition to messaging, users can initiate voice or video calls for more direct communication.
  
### **Message Management**
- **Edit, Delete, and Favorite Messages**: Users have full control over their messages. They can edit or delete any sent messages, mark messages as favorites for easy access later, and keep their chat history organized.
- **Reply and Forward Messages**: The app supports message replies and forwarding, making it easy to share messages and maintain conversation flow in both individual and group chats.

### **Groups and Community Management**
- **Create Groups and Communities**: Users can create custom groups and communities, allowing them to interact with multiple users at once.
- **Restrict Actions within Groups**: Administrators can restrict certain actions within groups, such as who can send messages or join the group, helping manage communication in large communities.

### **Profile Customization**
- **User Profiles**: Users can set up and manage their profiles, including personal information, profile pictures, and status updates. Profile customization enhances the user experience and adds a personal touch to interactions.

## Some Reference of the app 
![Main App](https://github.com/user-attachments/assets/fc2fcdc5-5975-4b73-93ab-1bc103b81305)
![App Functionality](https://github.com/user-attachments/assets/5253ddb5-7a17-4d9d-8da1-e7e868f90524)
![Profile and Status Screens](https://github.com/user-attachments/assets/8aa927e0-1673-4afa-9e2a-883fccf85536)

## Technical Overview

The app is built using modern Android development practices and technologies, focusing on performance, security, and user experience.

### **Programming Language and UI Framework**
- **Kotlin**: The app is developed in Kotlin, ensuring conciseness, safety, and ease of maintenance.
- **Jetpack Compose**: The UI is built using **Jetpack Compose**, Android's modern toolkit for creating native UI. Jetpack Compose allows for highly flexible and dynamic UI components that update automatically in response to data changes.

### **Architecture**
- **MVVM (Model-View-ViewModel)**: The app follows the MVVM architecture, ensuring clear separation of concerns. This architecture promotes clean, maintainable, and testable code.
  - **Model**: Handles the business logic, data, and network operations.
  - **ViewModel**: Manages UI-related data and interacts with the model. It acts as a mediator between the view and the model, ensuring that data is presented appropriately to the UI layer.
  - **View**: The UI layer built using Jetpack Compose, which reacts to state changes and updates the user interface accordingly.

### **Real-time Data and Notifications**
- **Firebase Realtime Database**: The app uses Firebase Realtime Database for real-time data updates. It tracks user statuses (online/offline) and sends notifications instantly when there are new messages or updates.
  - **Presence Tracking**: Using Firebase Realtime Database’s **Presence** feature, the app updates user status to **online** when the app is in use, and **offline** when the app is closed or the network is lost. The **onDisconnect()** method ensures that the user status is updated to offline when the network is disconnected.
  - **Callback Flow**: To efficiently handle data fetching and minimize rendering times, the app uses **CallbackFlow**. This asynchronous flow allows the app to automatically fetch data from Firebase listeners without manual triggering, ensuring that the UI always reflects the latest data.
  
### **End-to-End Encryption**
- **Single Protocol Encryption**: The app uses **Single Protocol** for end-to-end encryption of messages. When a user sends a message:
  - A **public/private key pair** is used for encryption and decryption. The public key is stored in the Firebase database, while the private key remains securely stored on the device.
  - Messages are encrypted on the sender's side before being stored in the database. On the recipient's side, the message is decrypted using the corresponding private key. This ensures that only the sender and recipient can read the message, providing a high level of security and privacy. Not even the app’s administrators have access to the message content.

### **Database and Offline Support**
- **Firestore**: **Firebase Firestore** is used for managing users' data, messages, and group information. Firestore is a NoSQL database that supports real-time syncing and offline capabilities, making it ideal for chat applications.
  - **Persistence**: Firestore provides offline persistence, which ensures that users can continue interacting with the app even when they are not connected to the internet. The local storage feature syncs data as soon as the device regains network access.
  
### **Dependency Injection (DI)**
- **Dagger-Hilt**: To manage dependencies across the app, **Dagger-Hilt** is used to implement **dependency injection (DI)**. This ensures that the app uses singleton instances for common dependencies, reducing the need for repetitive code and improving testability.
  - **Constructor and Field Injection**: Dagger-Hilt uses both constructor and field injection to manage dependencies across the app, making it easier to manage object creation and sharing.

### **Navigation**
- **Jetpack Navigation Component**: The app uses the **Navigation Component** to handle screen transitions. It creates a navigation graph, allowing users to seamlessly move between different screens with minimal effort.
  - Each screen has a defined route in the navigation graph, which simplifies the management of screen navigation and makes the app structure more intuitive.

## How It Works

### **Authentication Phase**
- When users first open the app, they are presented with a login screen for email-based authentication through **Firebase Authentication**. The app uses a robust and user-friendly UI to ensure a smooth sign-in experience.
- After successful authentication, users are taken to the main chat interface, where they can interact with messages, view group chats, and adjust their settings.

### **Main Application Phase**
- **Firestore** is used to store user and message data. The app listens for changes in the database and updates the UI accordingly. This ensures that users always see the most up-to-date messages and notifications in real time.
- **Cloudinary** is integrated for media storage, allowing users to send images, videos, and other media types within the chat.

### **Message Management**
- Users can prioritize messages based on urgency and category. For example, **Urgent** messages are highlighted first, ensuring that critical messages are never missed.
- Users can also set future timestamps for sending messages, allowing them to pre-schedule messages like birthday wishes or reminders.

### **Real-Time Updates**
- The app listens to changes in real-time via Firebase listeners, updating the UI immediately as new messages, status changes, or other updates occur.
- **CallbackFlow** and **LaunchedEffect** are used to ensure that the app fetches data asynchronously and reacts to UI changes efficiently.

## Installation

To run this project locally, follow the steps below:

1. **Clone the repository**:
    ```bash
    git clone [https://github.com/Vishal01x/LoopIt]
    ```

2. **Open the project** in Android Studio.

3. **Configure Firebase**:
    - Follow the official Firebase setup guide for Android: [Firebase Android Setup](https://firebase.google.com/docs/android/setup).
    - Download the `google-services.json` file and place it in your app's `app/` directory.

4. **Build and run the app**:
    - After setting up Firebase, you can build and run the app directly on your emulator or physical device.

## Contributing

Contributions are welcome! If you have suggestions for new features, improvements, or bug fixes, feel free to fork the repository and submit a pull request.

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/your-feature`).
3. Commit your changes (`git commit -am 'Add new feature'`).
4. Push to your branch (`git push origin feature/your-feature`).
5. Submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
