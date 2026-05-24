<div align="center">

# 🚜 FARMLINK  
### Connecting Farmers & Agriculture Students

<img src="https://img.shields.io/badge/Platform-Android-brightgreen?style=for-the-badge&logo=android" />
<img src="https://img.shields.io/badge/Language-Java-orange?style=for-the-badge&logo=java" />
<img src="https://img.shields.io/badge/Database-Firebase-blue?style=for-the-badge&logo=firebase" />
<img src="https://img.shields.io/badge/Status-Development-yellow?style=for-the-badge" />

<br/>
<br/>

> 🌱 Bridging the gap between farmers and agriculture students through technology.

</div>

---

# 📱 Overview

**FarmLink** is a dual-role Android application that connects farmers directly with agriculture students.

Farmers can:
- Sell fresh produce
- Manage customer orders
- Create learning content

Students can:
- Buy farm products
- Track orders
- Learn agriculture online
- Earn certificates

The platform removes middlemen and creates a direct farming ecosystem.

---

# 🎯 Problem Statement

Many farmers struggle to sell produce directly and lose profits to middlemen.

At the same time, agriculture students often lack:
- Access to affordable fresh produce
- Practical farming knowledge
- Direct interaction with experienced farmers

FarmLink solves this problem by creating a single digital platform for both groups.

---

# ✨ Features

<div align="center">

| 👨‍🌾 Farmer Features | 👨‍🎓 Student Features |
|---------------------|----------------------|
| Add/Edit/Delete Products | Browse Marketplace |
| Manage Orders | Add To Cart |
| Upload Product Images | Checkout & Payments |
| Track Deliveries | Track Orders |
| Answer Questions | Watch Courses |
| View Sales Stats | Earn Certificates |
| Manage Inventory | Request Farm Visits |

</div>

---

# 🛠️ Tech Stack

<div align="center">

| Technology | Usage |
|------------|------|
| ☕ Java | Main programming language |
| 📱 Android Studio | App development |
| 🔥 Firebase Authentication | Login & signup |
| 🗄️ Firebase Firestore | NoSQL database |
| ☁️ Firebase Storage | Image & file storage |
| 🔔 Firebase Cloud Messaging | Notifications |
| 📄 iTextPDF | PDF generation |
| 🎨 XML + Material Design | User Interface |

</div>

---

# 🏗️ Project Structure

```text
app/src/main/java/com/farmlink/
│
├── activities/
│   ├── auth/
│   ├── farmer/
│   └── student/
│
├── adapters/
│   ├── CartAdapter.java
│   ├── ProductAdapter.java
│   └── FarmerOrdersAdapter.java
│
├── models/
│   ├── User.java
│   ├── Product.java
│   ├── Order.java
│   ├── Cart.java
│   ├── Course.java
│   └── Certificate.java
│
└── utils/
    ├── CartManager.java
    ├── FirestoreHelper.java
    └── FastDraggableFAB.java
```

---

# 🧠 App Architecture

```text
┌─────────────────────────────────────────────┐
│ PRESENTATION LAYER                         │
│ Activities + XML Layouts                   │
├─────────────────────────────────────────────┤
│ ADAPTER LAYER                              │
│ RecyclerView Adapters                      │
├─────────────────────────────────────────────┤
│ LOGIC LAYER                                │
│ CartManager + FirestoreHelper              │
├─────────────────────────────────────────────┤
│ MODEL LAYER                                │
│ User, Product, Order, Course, Certificate  │
├─────────────────────────────────────────────┤
│ DATA LAYER                                 │
│ Firebase Firestore + Firebase Auth         │
└─────────────────────────────────────────────┘
```

---

# 🔥 Firebase Collections

```text
users/{userId}
├── name
├── email
├── role
├── learningPoints
└── location

products/{productId}
├── productName
├── description
├── price
├── quantity
├── farmerId
└── imageUrl

orders/{orderId}
├── customerId
├── items
├── totalPrice
├── orderStatus
└── timestamp

courses/{courseId}
├── title
├── description
├── videoUrl
└── quiz
```

---

# ▶️ How To Run The Project

## 1️⃣ Clone The Repository

```bash
git clone https://github.com/yourusername/farmlink.git
```

---

## 2️⃣ Open In Android Studio

Open the project folder in Android Studio.

---

## 3️⃣ Setup Firebase

Create a Firebase project and:

- Enable Email/Password Authentication
- Create Firestore Database
- Enable Firebase Storage
- Download `google-services.json`
- Place it inside the `app/` folder

---

## 4️⃣ Run The App

Run using:
- 📱 Android Emulator
or
- 📲 Physical Android Device

---

# 📚 What I Learned

Building this project taught me a lot about:

- Firebase architecture
- Android activity lifecycle
- RecyclerViews & adapters
- Real-time database syncing
- State management
- PDF generation
- Scalable app structure

I also learned the importance of:
- Database indexing
- Pagination
- Proper project planning

---

# 🚧 Future Improvements

- 💳 Online payments
- 🎥 Live course streaming
- 🤖 AI-based crop price suggestions
- 📲 WhatsApp notifications
- 🌍 Multi-language support
- 🌙 Dark mode
- 📊 Better analytics dashboard

---

# 📸 Screenshots

<div align="center">

🚧 Screenshots coming soon...

</div>

---

# 👨‍💻 Developer

<div align="center">

## Tumelo Mbuyazi

🎓 Diploma in ICT Student  
📧 tumelombuyazi96@gmail.com

</div>

---

# ⚠️ Disclaimer

This is a student project created for learning purposes.

The application is still under development and not production-ready yet.

---

<div align="center">

# ☕ Built with coffee, debugging, and late-night coding sessions.

⭐ If you like the project, consider giving it a star on GitHub!

</div>
