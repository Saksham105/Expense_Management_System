# Expense Management System

## Overview
Expense Splitter System is a Java desktop application that helps users manage shared expenses inside groups. It allows users to register, log in, create groups, add participants, create split-based expenses, and settle dues between members.

The project is built with Java, Swing, JDBC, and MySQL. It follows an object-oriented and layered design so that the UI, business logic, and data storage remain separated and easier to maintain.

## Main Features
- User registration and login
- Profile update and account deletion
- Group creation and participant management
- Expense creation with multiple split strategies
- Group-wise balance summary
- Settlement flow with simplified debt calculation
- Persistent storage using MySQL
- Split payment tracking using paid and unpaid status

## Supported Split Types
The system currently supports these split types:

- `EQUAL`: divides the amount equally among selected participants
- `PERCENTAGE`: divides the amount based on percentage values
- `SHARE`: divides the amount based on share counts
- `ITEM_LEVEL`: divides the amount using custom per-user values

## Technology Stack
- Java
- Java Swing
- JDBC
- MySQL 8.0
- Object-Oriented Programming
- Java Collections
- Basic multithreading support through synchronized service and manager methods

## How the Project Works
1. When the application starts, it loads users, groups, and expenses from the MySQL database.
2. Users interact with the application through the Swing GUI.
3. GUI actions call manager classes.
4. Managers delegate work to service classes.
5. Services apply validation and business rules.
6. Repositories manage in-memory collections and database upload and reload operations.
7. When the application closes, the current data is uploaded back to the database.

## High-Level Architecture
The project follows a layered flow:

`GUI -> Manager -> Service -> Repository -> Database`

### Role of Each Layer
- `GUI`: handles user interaction and screen rendering
- `Manager`: acts as the bridge between GUI and service classes
- `Service`: contains core business logic
- `Repository`: manages data access and in-memory storage
- `Model`: represents core business entities
- `Strategy`: contains split calculation algorithms
- `Exception`: defines custom exceptions for validation and error handling

## Folder Structure
Expense_Management_System/
|-- README.md
|-- main/
|   |-- document/
|   |   |-- DOCUMENTATION.md
|   |   |-- PROBLEM_STATEMENT.md
|   |
|   |-- java/
|   |   |-- com/
|   |   |   |-- expense/
|   |   |   |   |-- split/
|   |   |   |   |   |-- ExpenseSplitterApplication.java
|   |   |   |   |   |-- db/
|   |   |   |   |   |   |-- DbConnection.java
|   |   |   |   |   |-- design/
|   |   |   |   |   |   |-- Color.java
|   |   |   |   |   |   |-- Style.java
|   |   |   |   |   |-- dto/
|   |   |   |   |   |   |-- Input.java
|   |   |   |   |   |-- exception/
|   |   |   |   |   |   |-- IllegalAmountException.java
|   |   |   |   |   |   |-- IllegalNameException.java
|   |   |   |   |   |   |-- InvalidBalanceException.java
|   |   |   |   |   |   |-- InvalidSplitException.java
|   |   |   |   |   |   |-- InvalidUserException.java
|   |   |   |   |   |   |-- PercentageNot100Exception.java
|   |   |   |   |   |-- gui/
|   |   |   |   |   |   |-- ExpenseSplitterGUI.java
|   |   |   |   |   |-- manager/
|   |   |   |   |   |   |-- ExpenseManager.java
|   |   |   |   |   |   |-- GroupManager.java
|   |   |   |   |   |   |-- UserManager.java
|   |   |   |   |   |-- model/
|   |   |   |   |   |   |-- Expense.java
|   |   |   |   |   |   |-- Group.java
|   |   |   |   |   |   |-- SplitDetail.java
|   |   |   |   |   |   |-- SplitType.java
|   |   |   |   |   |   |-- User.java
|   |   |   |   |   |-- repository/
|   |   |   |   |   |   |-- ExpenseRepository.java
|   |   |   |   |   |   |-- GroupRepository.java
|   |   |   |   |   |   |-- UserRepository.java
|   |   |   |   |   |-- service/
|   |   |   |   |   |   |-- DashboardService.java
|   |   |   |   |   |   |-- DebtSimplifier.java
|   |   |   |   |   |   |-- ExpenseService.java
|   |   |   |   |   |   |-- GroupService.java
|   |   |   |   |   |   |-- RecurringExpenseManager.java
|   |   |   |   |   |   |-- UserService.java
|   |   |   |   |   |-- strategy/
|   |   |   |   |   |   |-- EqualSplitStrategy.java
|   |   |   |   |   |   |-- ItemLevelSplitStrategy.java
|   |   |   |   |   |   |-- PercentageSplitStrategy.java
|   |   |   |   |   |   |-- ShareSplitStrategy.java
|   |   |   |   |   |   |-- SplitStrategy.java
|   |   |
|   |   |-- lib/
|   |   |   |-- mysql-connector-j-9.6.0.jar

## Package Summary

### `model`
Contains the core business entities:
- `User`
- `Group`
- `Expense`
- `SplitDetail`
- `SplitType`

### `gui`
Contains the Swing-based application screen:
- `ExpenseSplitterGUI`

### `manager`
Acts as a bridge between GUI and service layer:
- `UserManager`
- `GroupManager`
- `ExpenseManager`

### `service`
Contains business logic:
- `UserService`
- `GroupService`
- `ExpenseService`
- `DashboardService`
- `DebtSimplifier`
- `RecurringExpenseManager`

### `repository`
Handles in-memory storage and database synchronization:
- `UserRepository`
- `GroupRepository`
- `ExpenseRepository`

### `strategy`
Contains expense split algorithms:
- `SplitStrategy`
- `EqualSplitStrategy`
- `PercentageSplitStrategy`
- `ShareSplitStrategy`
- `ItemLevelSplitStrategy`

### `db`
Contains database connectivity class:
- `DbConnection`

### `exception`
Contains custom exception classes used for validation and flow control.

### `dto`
Contains helper input utilities used by the service layer.

## Core Domain Model

### User
Represents a registered user of the system.
Important fields:
- `id`
- `name`
- `email`
- `password`
- `splitHistory`

### Group
Represents a collection of users who share expenses.
Important fields:
- `id`
- `name`
- `members`
- `expenses`

### Expense
Represents a single expense created inside a group.
Important fields:
- `id`
- `description`
- `totalAmount`
- `paidTo`
- `paidBy`
- `type`
- `createdAt`
- `groupId`
- `currency`
- `conversionRate`
- `isRecurring`

### SplitDetail
Represents how much a participant owes and whether the payment is settled.
Important fields:
- `user`
- `amountOwed`
- `status`

### SplitType
Enum used to identify the selected split strategy.

## Database Schema
Database name used by the project: `expense_splitter`

### 1. `users`
Stores registered user details.

| Column    | Description          |
|-----------|----------------------|
| `user_id` | Primary key for user |
| `name`    | User name            |
| `email`   | User email           |
| `password`| User password        |

### 2. `groups_table`
Stores group information.

| Column      | Description           |
|-------------|-----------------------|
| `group_id`  | Primary key for group |
| `group_name`| Name of the group     |

### 3. `group_members`
Mapping table between groups and users.

| Column     | Description                        |
|------------|------------------------------------|
| `group_id` | References `groups_table.group_id` |
| `user_id`  | References `users.user_id`         |

### 4. `expenses`
Stores every expense created in the system.

| Column            | Description                                |
|-------------------|--------------------------------------------|
| `expense_id`      | Primary key for expense                    |
| `description`     | Expense description                        |
| `total_amount`    | Total expense amount                       |
| `paid_to`         | User who paid or created the expense       |
| `split_type`      | Type of split used                         |
| `created_at`      | Expense creation timestamp                 |
| `group_id`        | References the group where expense belongs |
| `currency`        | Currency code                              |
| `conversion_rate` | Conversion rate used for the expense       |
| `is_recurring`    | Recurring flag, stored as 0 or 1           |

### 5. `split_details`
Stores participant-level split information for each expense.

| Column           | Description                                          |
|------------------|------------------------------------------------------|
| `expense_id`     | References `expenses.expense_id`                     |
| `user_id`        | References `users.user_id`                           |
| `amount_owed`    | Amount owed by that participant                      |
| `payment_status` | Settlement status, where `0 = unpaid` and `1 = paid` |

## Database Relationships
users (1) --------< expenses.paid_to
users (M) >------< group_members >------< (M) groups_table
groups_table (1) --< expenses
expenses (1) -----< split_details >----- (M) users

## Example Database Interpretation
From the attached database screenshot:
- the `users` table contains four registered users
- the `groups_table` table contains one group
- the `group_members` table shows all four users are members of that group
- the `expenses` table contains one equal split expense
- the `split_details` table shows participant-wise owed amount and settlement status


## Important Business Logic

### Expense Creation
When an expense is created:
- the owner or payer is stored in `paid_to`
- selected participants are stored in `SplitDetail`
- the split strategy calculates how much each participant owes
- the expense is added to the target group

### Dashboard Summary
`DashboardService` calculates:
- how much the current user owes
- how much other users owe the current user
- per-person balance summary inside a group

### Settlement Logic
`DebtSimplifier` reduces multiple dues into simpler transactions by:
- calculating net balances for each member
- identifying debtors and creditors
- generating the minimum useful settlement list

### Persistence Flow
- `UserRepository.download()` loads users from MySQL
- `GroupRepository.download()` loads groups and members
- `ExpenseRepository.download()` loads expenses and split details
- on application close, repository upload methods persist the latest data back to MySQL

## Design Approaches Used
- Layered architecture
- Strategy pattern for split calculation
- Repository pattern for data access
- Encapsulation through model classes
- Custom exception handling for validation

## Interview-Focused Explanation
This project is a Java desktop expense-sharing application with MySQL persistence. The main strength of the project is that it is not just a UI program. It also includes:
- layered architecture
- multiple split strategies
- debt simplification logic
- group-based expense tracking
- persistent data storage
- settlement status handling at participant level

`I built a Java Swing based expense splitter system where users can create groups, add members, create shared expenses using multiple split strategies, and settle balances. The application uses a layered architecture with managers, services, repositories, strategy classes, and MySQL persistence through JDBC.`

## Current Scope Notes
- The project already supports recurring-expense fields, but full recurring automation is still an extension point.
- Split settlement status is currently tracked using a boolean paid or unpaid value. Partial settelment is refered as future scope.
- Data is maintained in memory during runtime and synchronized with MySQL during load and close operations.
