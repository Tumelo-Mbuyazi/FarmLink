# 🚜 FARMLINK - Connecting Farmers & Agriculture Students

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)
![Language](https://img.shields.io/badge/Language-Java-orange)
![Database](https://img.shields.io/badge/Database-Firebase-blue)
![Status](https://img.shields.io/badge/Status-Development-yellow)

## 📱 Overview

**FarmLink** is a dual-role Android application that bridges the gap between farmers and agriculture students. Farmers can sell their produce directly to students and create online courses, while students can shop for fresh farm products, enroll in agricultural courses, and earn certificates.

### 🎯 Problem Statement

Farmers struggle to find direct buyers for their produce, losing 30-40% profit to middlemen. Agriculture students lack platforms to buy fresh produce and access practical farming education. FarmLink solves this by creating a direct connection.



## 📱 Features

### 👨‍🌾 Farmer Features
- Add products with images, price, and quantity
- Edit or delete products anytime
- Manage customer orders
- Update order status:
  - Pending
  - Processing
  - Shipped
  - Delivered
- Respond to student questions
- View simple sales statistics

### 👨‍🎓 Student Features
- Browse farm products from different farmers
- Search and filter products
- Add products to cart
- Checkout and place orders
- Track orders in real-time
- Enroll in agriculture courses
- Watch video lessons
- Complete quizzes
- Earn PDF certificates after course completion
- Ask farmers questions
- Request farm visits for practical learning

---

# 🛠️ Tech Stack

| Technology | Usage |
|------------|------|
| Java | Main programming language |
| Android Studio | App development |
| Firebase Authentication | Login & signup |
| Firebase Firestore | NoSQL database |
| Firebase Storage | Image & file storage |
| Firebase Cloud Messaging | Notifications |
| iTextPDF | PDF certificate generation |
| XML + Material Design | User interface |

---

# 🏗️ Project Structure

app/src/main/java/com/farmlink/
│
├── activities/
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   ├── SignupActivity.java
│   │   └── ForgotPasswordActivity.java
│   │
│   ├── farmer/
│   │   ├── FarmerDashboardActivity.java
│   │   ├── FarmerProductsActivity.java
│   │   ├── AddProductActivity.java
│   │   ├── EditProductActivity.java
│   │   ├── FarmerOrdersActivity.java
│   │   └── FarmerProfileActivity.java
│   │
│   └── student/
│       ├── StudentDashboardActivity.java
│       ├── StudentMarketplaceActivity.java
│       ├── ProductDetailActivity.java
│       ├── CartActivity.java
│       ├── CheckoutActivity.java
│       ├── MyOrdersActivity.java
│       ├── OrderTrackingActivity.java
│       ├── StudentLearningActivity.java
│       ├── CourseDetailActivity.java
│       ├── StudentConnectActivity.java
│       ├── AskFarmerActivity.java
│       ├── RequestFarmVisitActivity.java
│       └── StudentProfileActivity.java
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
│   ├── CartItem.java
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

## 1. Clone the repository

```bash
git clone https://github.com/yourusername/farmlink.git
```

## 2. Open in Android Studio

Open the project folder in Android Studio.

## 3. Setup Firebase

Create a Firebase project and:

- Enable Email/Password Authentication
- Create Firestore Database
- Enable Firebase Storage
- Download `google-services.json`
- Place it inside the `app/` folder

## 4. Run the app

Use:
- Android Emulator  
or
- Physical Android device

---

# 📚 What I Learned

Building this project taught me a lot about Android development and Firebase.

Some important things I learned:

- Firebase becomes slow without proper indexing
- Pagination is necessary for large datasets
- Managing cart data across activities is challenging
- Planning the database structure early saves time later
- PDF generation takes more work than expected

I also learned how to structure a bigger Android project with multiple activities, adapters, models, and Firebase integration.

---

# 🚧 Future Improvements

Features I would like to add later:

- Online payments
- Live course streaming
- AI-based crop price suggestions
- WhatsApp notifications
- Multi-language support
- Dark mode
- Better analytics for farmers

---

# 📸 Screenshots

Screenshots will be added soon.

---

# 👨‍💻 Developer

Built as a student project for learning purposes.

**Name:** Tumelo Mbuyazi
**Course:** Diploma in ICT    
**Email:** tumelombuyazi96@gmail.com

---

# ⚠️ Note

This is a student project and not production-ready yet. Some parts of the code can still be improved, but the project was built to practice Android development, Firebase integration, and real-world problem solving.

If you find the project useful, feel free to fork it or give it a ⭐ on GitHub.

---

## ☕ Built with coffee, debugging, and late-night coding sessions.
