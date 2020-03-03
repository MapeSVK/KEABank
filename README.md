# KEABank - Android elective final project
### About the project
KEABank is a user friendly android application with a connection to the Firebase authentication and the Firestore database. 

UI/UX design is made according to the modern trends. Mostly it is navigation drawer, snackbars, same sizes of the elements, color selection, minimalistic design, etc.

### Functionalities
In the application, the user is able to:
1. register himself and sign in (Firebase Auth)
2. send password reset email
3. see the total balance of his accounts and balance for each account too
4. add a new account to his list of accounts (some of them can have condition which have to be met)
5. transfer the money between own accounts
6. transfer the money between his/her and different users' accounts
7. pay a bill
8. save the bill to his auto-bill list for the automatic payment
9. change his password
10. set regular transaction to "savings" and "budget" accounts

### How to run the app:
1. Clone this repository
2. Run with Android Studio
3. Register yourself (must be real e-mail address and stronger password) **or** use one of the following accounts:

Older & rich guy => **email:** miso.moravik111@gmail.com => **password:** test123

Young & poor guy => **email:** michal.moravik@icloud.com => **password:** test123

>The young guy is basically completely new user only with 3k DKK for bill payment test.

4. while paying, the card is needed for the verification process. 

![alt text](https://i.imgur.com/X9vvq1i.png)

5. While paying the bill choose billId which has 4 digits (they are: 1010 or 2020 until 6060 ), each of those bills has amount 1000 which needs to be paid and all of them have "isPaid" field set to "false" so they need to be paid. 
