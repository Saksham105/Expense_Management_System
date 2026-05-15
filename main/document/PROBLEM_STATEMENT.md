# Expense Management System

## Problem Statement
    Design and implement a split-wise like expense sharing system, that allows you to:
        - Add expense
        - Split expense among many user
        - Track balances between users
        - Minimize no. of transactions required to settle amount (expense)

    System should be modular, extensible and follow OOP principle

### Functional Requirement
    1. User Management
        - user can be added to system
        - each user has: (
            unique_id, 
            name, 
            email <optional>
        )

    2. Expense Creation
        - user can create an expense by specifying: (
            amount,
            paid by <one user>,
            list of participant,
            type of split
        )

    3. Supported Split Types
        - equal split (amount is divided equally among all participant)
        ex. amount=300, participant=3 i.e. 100/- each

        - percentage split (each participant pays according to percentage)
        ex. amount=1000, participant (A:50%, B:30%, C:20%)

        - exact split (<optional> each participant has exact shared amount)

    4. Balance Tracking
        - The system should maintain all transactions done by all participant
        - Balance must update dynamically after each expense payment

    5. Show Balances
        - System should support showAllTransactions() and of perticulat user.

### Non Functional Requirement
    1. Code must be modular
    2. Follow solid principle
    3. Use proper package structure
    4. Extensible for future split types
    5. Should avoid floating precision issues
    6. System should be thread safe

### Constraints and Edge Cases
    1. What if percentage sum is not 100 ?
    2. What if participant list is empty ?
    3. What if paid-by-user is not in participant list ?
    4. How to avoid negative balance ?
    5. How to restrict duplicate users ?
    6. How to avoid rounding errors ?

### Expected
    1. You should think in the term of:
        - model layer (user, expense)
        - strategy later (split strategies)
        - service layer (expense services)
        - balance manager
        - exception layer (custom exceptions)
        - repository layer (in memory storage)