### Account transactions app

#### Installing

`./gradlew clean build`

#### Running

`./gradlew run`

#### API

`GET /accounts` - list accounts

`GET /accounts/:id` - fetch single account

`POST /accounts` - create account, body:

```json
{
  "currency": "USD",
  "balance": "100.00"
}
```

`POST /accounts/transfer` transfer funds from one account to another, body:

```json
{
  "from": "source account UUID",
  "to": "source account UUID",
  "amount": "100.00"
}
```

#### Tests

Tests are run during build

Standalone run: `./gradlew clean test`