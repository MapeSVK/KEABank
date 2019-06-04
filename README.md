# KEABank - Android elective final project
### About the project
KEABank is user friendly android application with the connection to the Firebase authentication and the firestore database. This connection makes the app more sophisticated solution (and challenge) for given exam project. 

UI/UX design is made according to the modern trends. Mostly it is navigation drawer, snackbars, same sizes of the elements, color selection, minimalistic design, etc.

### How to run the app:
1. Clone this repository
2. Run with Android Studio
3. Register yourself (must be real e-mail address and stronger password) **or** use one of the following accounts:

Older & rich guy => **email:** miso.moravik111@gmail.com => **password:** test123

Young & poor guy => **email:** michal.moravik@icloud.com => **password:** test123

>The second one is basicly completely new user only with 3k DKK for bill payment test.

4. while paying, the card is needed for the verification process. 

![alt text](https://i.imgur.com/X9vvq1i.png)

### Functionalities
In the application, the user is able to:
1. register himself and sign in (Firebase Auth)
2. send password reset email
3. see the total balance of his accounts and balance for each account too
4. add a new account to his list of accounts (some of them can have condition which have to be met)
5. transfer the money between owne accounts
6. transfer the money between your and different user's account
7. pay a bill
8. save the bill to his auto-bill list for the automatic payment
9. change his password
10. set regular transaction to "savings" and "budget" accounts

### The Database structure
The main two collections of documents are "bills" and "users".  "users" includes all the users where one of the user has properties firstname, lastname, dateOfBirth and email. The user's subcollections are "accounts", "autoBills", and "regularTransactions". "accounts" is a collection of accounts where one account has its accountId, and the amount (balance). "autoBills" is a collection of saved bills where one bill has billId, amount, and autoBillDay. "regularTransaction" has 2 documents - savings and budget account whose fields are amount and day.
