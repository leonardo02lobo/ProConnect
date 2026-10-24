# Registration validation

## Application Overview

Registration form at /register (anonymous, use NO_SESSION storage state). Fields: register-name, register-username, register-email, register-password (required, minlength=8), register-submit, register-error (role=alert, hidden until an error). Backend: POST http://localhost:18080/api/auth/register with body {name, userName, email, password}; login is POST /api/auth/login {email, password}. All entities use the uid fixture. Never mutate Alice (alice.e2e@proconnect.test, seed user).

## Test Scenarios

### 1. Registration validation

**Seed:** `tests/seed.spec.ts`

#### 1.1. Duplicate email is rejected

**File:** `tests/register-validation.spec.ts`

**Steps:**
  1. Use NO_SESSION. Data created: name 'Dup ${uid}', userName 'dup_${uid}', email alice.e2e@proconnect.test (existing), password 'Passw0rd!123'. Go to /register and wait for register-submit to be visible. Fill the four fields with this data, listen for the POST /api/auth/register response, click register-submit.
    - expect: Response status is 409 with body message 'El email ya está registrado' (observed)
    - expect: register-error is visible and has text 'El email ya está registrado'
    - expect: URL stays http://localhost:14321/register; add-post-submit is not visible
  2. Verify no account was created: API login POST /api/auth/login with the attempted credentials. Also verify Alice's account is intact (Alice's own password still logs in via USERS.alice).
    - expect: Login with an email derived from the new username, or the attempted password against Alice's email, returns 401 (the attempted password 'Passw0rd!123' does not log in as Alice)
    - expect: Alice's original credentials still return 200

#### 1.2. Password shorter than 8 characters is rejected

**File:** `tests/register-validation.spec.ts`

**Steps:**
  1. Use NO_SESSION. Data created: name 'Short ${uid}', userName 'short_${uid}', email 'short_${uid}@proconnect.test', password '1234567' (7 chars). Go to /register, fill the four fields, register a page.on('request') / waitForRequest spy for POST /api/auth/register, click register-submit.
    - expect: register-password validity.tooShort is true (native message 'Please lengthen this text to 8 characters or more (you are currently using 7 characters).')
    - expect: No POST /api/auth/register request is fired (observed: zero auth requests)
    - expect: URL stays /register and register-error is not visible (native validation blocks submit, no server error shown)
  2. Attempt API login POST /api/auth/login with email 'short_${uid}@proconnect.test' and password '1234567'.
    - expect: Status 401 (body 'Credenciales inválidas'): the account does not exist
  3. Informational, backend defense: POST http://localhost:18080/api/auth/register directly with {name:'Direct ${uid}', userName:'direct_${uid}', email:'direct_${uid}@proconnect.test', password:'1234567'}, then try to log in with it. Note the JSON key must be userName (using 'username' yields 400 'name y userName son obligatorios').
    - expect: Observed: 400 {message:'El password debe tener al menos 8 caracteres', error:'Bad Request'}; the backend independently enforces the minimum
    - expect: Subsequent login returns 401, no account created

#### 1.3. Successful registration with unique data

**File:** `tests/register-validation.spec.ts`

**Steps:**
  1. Use NO_SESSION. Data created: name 'Ok ${uid}', userName 'ok_${uid}', email 'ok_${uid}@proconnect.test', password 'Passw0rd!123'. Go to /register, wait for register-submit, fill the four fields, click register-submit.
    - expect: Page navigates to http://localhost:14321/ (use expect(page).toHaveURL)
    - expect: Feed is hydrated: add-post-submit is visible
    - expect: header-user-menu contains 'Ok ${uid}'
    - expect: Cookie pc_session is set for localhost (context.cookies())
    - expect: register-error never became visible
