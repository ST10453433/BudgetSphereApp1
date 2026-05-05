# BudgetSphere1



A personal budget tracking Android app built with Kotlin and RoomDB.

**Team Members**

- Hlulani Hope Mashaba - ST10202512
- Zwivhuya Prudence Maphalaphatwa - ST10453433
- Kitso Zwane - ST10442335
- Mankane Makinta - ST10435250

**Demo Video**

[Watch the demo on YouTube](https://w)

**Features**

| **Feature**         | **Description**                                                                      |
| ------------------- | ------------------------------------------------------------------------------------ |
| 🔐 Login / Register | Secure username + password login with RoomDB storage                                 |
| 📁 Categories       | Create custom spending categories with colour codes                                  |
| ➕ Add Expense      | Record expenses with date, start/end time, description, category, and optional photo |
| 📷 Photo Capture    | Attach a photo to any expense using the device camera                                |
| 🎯 Budget Goals     | Set minimum and maximum monthly spending goals using a SeekBar                       |
| 📋 Expense History  | View all expenses filtered by a user-selectable date range                           |
| 📊 Category Totals  | See total spending per category for any selected period                              |
| 🗄️ Local Database   | All data persisted offline using RoomDB (SQLite)                                     |

**Tech Stack**

- **Language:** Kotlin
- **UI:** XML Layouts, Material Design, ViewBinding
- **Database:** Room (SQLite)
- **Architecture:** Fragment-based navigation with BottomNavigationView
- **Image loading:** Glide
- **Async:** Kotlin Coroutines + LiveData
- **CI/CD:** GitHub Actions (automated build on every push)

**Project Structure**

app/src/main/java/com/example/budgetsphere/

**├── data/**

│ ├── AppDatabase.kt # Room database singleton

│ ├── Daos.kt # All DAO interfaces

│ └── Entities.kt # User, Category, Expense, BudgetGoal

**├── ui/**

│ ├── DashboardFragment.kt

│ ├── AddExpenseFragment.kt

│ ├── ExpenseListFragment.kt

│ ├── CategoryTotalsFragment.kt

│ └── BudgetGoalsFragment.kt

**├── adapters/**

│ ├── ExpenseAdapter.kt

│ └── CategoryTotalsAdapter.kt

├── LoginActivity.kt

├── RegisterActivity.kt

└── MainActivity.kt






**References**

- Canva (2026). _Creating app design mockups and prototypes_. Available at: <https://www.canva.com>
- Android Developers (2026). _Room persistence library_. Available at: <https://developer.android.com/training/data-storage/room>
- Android Developers (2026). _FileProvider_. Available at: <https://developer.android.com/reference/androidx/core/content/FileProvider>
- Zymr (2025). _How to develop a mobile banking app_. Available at: <https://www.zymr.com/blog/how-to-develop-a-mobile-banking-app>
