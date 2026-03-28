# Expense Splitter System
Java (OOP + Collections + Multithreading)

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

---

## Folder Structure
    
    Expense Splitter System/
    |->  main.output/               # .png files of all outputs of system
    |
    |->  main.documentation/
    |    |-- README.md           # instructions about "how to use the system"
    |    |-- DOCUMENTATION.md    # information about the whole system
    |
    |->  main.java.com.expense.split/
    |    |
    |    |-- model/
    |    |   |-- User
    |    |   |-- Expense
    |    |   |-- SplitDetail
    |    |   |-- SplitType (enum)
    |    |
    |    |-- strategy/
    |    |   |-- SplitStrategy
    |    |   |-- EqualSplitStrategy
    |    |   |-- PercentageSplitStrategy
    |    |
    |    |-- service/
    |    |   |-- UserService
    |    |   |-- ExpenseService
    |    |
    |    |-- manager/
    |    |   |-- UserManager
    |    |   |-- ExpenseManager
    |    |   |-- BalanceManager
    |    |
    |    |-- repository/
    |    |   |-- UserRepository
    |    |   |-- ExpenseRepository
    |    |
    |    |-- exception/
    |    |   |-- InvalidUserException
    |    |   |-- InvalidSplitException
    |    |   |-- PercentageNot100Exception
    |    |   |-- InvalidBalanceException
    |    |   |-- IllegalNameException
    |    |   |-- IllegalAmountException

---

## Detailed structure of class
### Model
    1. User         #(id, name, email/mob_no)
    2. Expense      #(id, totalAmount, paid_to <User>,type <SplitType>, splitDetails <SplitDetails[]>, createdAt)
    3. SplitDetail  #(user <User>, totalAmountOwed, status [PAID/UNPAID])

### Strategy
    1. SplitStrategy <interface>    #(List<SplitDetail> calculateSplit(totalAmount, participants))
    2. EqualSplitStrategy
    3. PercentageSplitStrategy

### Exception
    1. InvalidBalanceException      # throws when split balance is zero or negative
    2. IllegalNameException         # throws when user name is empty or have special symbols
    3. IllegalAmountException       # throws when account balance is going less than limit
    4. InvalidUserException         # throws when empty user is in split participant
    5. InvalidSplitException        # thows when user enters an invalid split type
    6. PercentageNot100Exception    # throws when user enters invalid percentages in percentage split

### Service
    1. UserService (
        - CreateUser(name, email)
        - getUserById(id)
        - getAllUsers()
    )

    2. ExpenseService (
        - createExpense(paidBy, amount, participants, strategy)
    )

### Manager
    1. ExpenseManager     # uses ExpenseService and ExpenseRepository
    2. UserManager      # uses UserService and UserRepository
    3. BalanceManager   # tracks self (user's) balance (split)

### Repository
    UserRepository    # stores all registered user
    ExpenseRepository    # stores all expenses

---

## Program Flow

client side |   middleware      |  server side
                ________
            |->| Service|-------|-> | Model       |
            |  |________|       |
            |        ^          |-> | Strategy    |
main()------|        |          |
            |   ________        |-> | Repository  |
            |  | Manager|       |
            |->|________|       |-> | Exception   |

---

## Functionalities

    User can:
        1. signup (name, email, password)
        2. login (email, password)
        
        After login only:
            1. Update profile (name, email, password)
            2. Make a split
            3. Settle split
            4. Split history
            5. Clear history
            6. Logout
            7. Delete account

    While making a split:
        1. User who is making split is refered internally
        2. total amount must be entered
        3. participant <User> details must be entered

    User can settle the split:
        This includes the analysis of all splits and making round off of all splits
        ex. if (
                A -> B
                B -> C
            ) then, A -> C

            pays to (->)
