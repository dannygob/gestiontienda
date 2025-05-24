# Gestión Tienda - Store Management App

A comprehensive Kotlin Multiplatform Mobile (KMM) store management application built with modern
development practices and Clean Architecture principles.

## Features

### 1. Product Management

- Complete CRUD operations for products
- Manual barcode entry with product lookup
- Barcode scanning (coming soon)
- Stock level tracking
- Product categories and tags
- Price history tracking
- Integration with Open Food Facts API for product information

### 2. Sales System

- Quick sale interface
- Multiple payment methods support
- Receipt generation
- Sales history and returns handling
- Discount management
- Tax calculation

### 3. Customer Management

- Customer profiles with contact information
- Purchase history tracking
- Customer categories
- Automated notifications

### 4. Stock Management

- Real-time inventory tracking
- Low stock alerts
- Stock movement history
- Multi-location support

### 5. Sales Statistics & Reports

- Daily/weekly/monthly sales reports
- Product performance analytics
- Export functionality (PDF, Excel)
- Custom report generation

### 6. User Management & Security

- Role-based access control
    - Administrator
    - Manager
    - Cashier
    - Inventory Manager
- Secure authentication with Firebase
- Activity logging
- Session management

### 7. Settings & Configuration

- Store information management
- Tax rates configuration
- Backup management
    - Cloud backup (Firebase)
    - Local backup
- Language support
    - English (default)
    - Spanish
    - Arabic (Morocco)
    - Chinese (Simplified)
- Currency settings

## Technical Stack

### Architecture

- Kotlin Multiplatform Mobile (KMM)
- Clean Architecture with MVVM pattern
- Multi-module project structure
- Repository pattern for data management
- Use case pattern for business logic

### Android Components

- Jetpack Compose for UI
- Material 3 Design
- Navigation Component
- ViewModel
- SQLDelight for local database
- WorkManager for background tasks
- DataStore for preferences

### Dependencies

- Kotlin Coroutines & Flow
- Firebase (Auth, Firestore, Storage)
- ML Kit for barcode scanning (planned)
- Open Food Facts API integration
- SQLDelight for cross-platform persistence
- Ktor for networking

## Project Structure

```
├── androidApp/               # Android application module
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── kotlin/com/gestiontienda/android/
│           │   ├── data/
│           │   │   ├── local/
│           │   │   ├── remote/
│           │   │   └── repository/
│           │   ├── di/
│           │   ├── ui/
│           │   │   ├── common/
│           │   │   ├── products/
│           │   │   ├── sales/
│           │   │   ├── customers/
│           │   │   ├── statistics/
│           │   │   └── settings/
│           │   └── utils/
│           └── res/
│
├── shared/                   # Shared Kotlin module
│   ├── build.gradle.kts
│   └── src/
│       ├── commonMain/      # Common Kotlin code
│       │   └── kotlin/com/gestiontienda/shared/
│       │       ├── domain/
│       │       ├── data/
│       │       └── utils/
│       ├── androidMain/     # Android-specific code
│       └── iosMain/         # iOS-specific code
│
├── iosApp/                  # iOS application module
├── build.gradle.kts         # Root build file
└── settings.gradle.kts      # Settings and module definitions
```

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 24 or later
- Firebase project setup
- Git

### Installation

1. Clone the repository:

```bash
git clone https://github.com/yourusername/gestiontienda.git
```

2. Open the project in Android Studio

3. Configure Firebase:
    - Create a Firebase project
    - Add Android app to Firebase project
    - Download `google-services.json` and place it in `androidApp/`
    - Enable Authentication and Firestore

4. Build and run the project

### Configuration

1. Firebase Setup:
    - Enable Authentication
    - Set up Firestore
    - Configure Storage rules

2. API Keys:
    - Create a `local.properties` file
    - Add your API keys and sensitive data

## Current Implementation Status

### Completed Features

- ✅ Project structure and architecture
- ✅ Firebase integration
- ✅ Product management (basic CRUD)
- ✅ Manual barcode entry
- ✅ Open Food Facts API integration
- ✅ Basic UI components with Material 3

### In Progress

- 🔄 User authentication
- 🔄 Product catalog
- 🔄 Sales management
- 🔄 Stock tracking

### Planned Features

- 📅 Barcode scanning
- 📅 Reports and analytics
- 📅 Customer management
- 📅 Multi-language support

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Contact

Your Name - [@yourusername](https://twitter.com/yourusername)
Project
Link: [https://github.com/yourusername/gestiontienda](https://github.com/yourusername/gestiontienda)

## Future Improvements

1. Integration with e-commerce platforms
2. Advanced analytics and ML-based predictions
3. Multi-store support
4. Offline-first architecture
5. Integration with accounting software
6. Enhanced security features
7. Mobile payment integration
8. Advanced inventory management features

// Example usage in a ViewModel
class ProductViewModel(private val repository: ProductRepository) {
// Add a product by scanning its barcode
suspend fun addScannedProduct(barcode: String, purchasePrice: Double, salePrice: Double) {
repository.addProductByBarcode(
barcode = barcode,
purchasePrice = purchasePrice,
salePrice = salePrice
)
}

    // Search products in Open Food Facts database
    suspend fun searchProducts(query: String) {
        repository.searchProducts(query)
    }

} 