# ProConnect — critical flows test plan

Seed: `tests/seed.spec.ts` (Alice logged in, feed hydrated). Seed data from `global-setup.ts`:
users Alice and Bob (`fixtures/users.ts`), post "E2E seed post" by Alice with an image,
one like and one comment by Bob.

## 1. Auth (`tests/auth.spec.ts`)
1. Register from `/register` with unique name/userName/email → account exists (API login works).
2. Login with valid credentials on `/login` → feed `/`, header shows the user name, `pc_session` is HttpOnly.
3. Login with wrong password → error message, still on login, no `pc_session`.
4. Logout from the header menu (dedicated user) → back to login; `/` redirects to login again.
5. Without session, `/`, `/Profile` and `/user/{id}` redirect to login.
6. Without session, API answers 401 for `/api/auth/me`, `GET /api/posts`, `POST /api/posts/create`.

## 2. Feed (`tests/feed.spec.ts`)
1. The seed post shows author name, author photo, image loaded through the gateway, like and comment counters ≥ 1 and Bob's comment.

## 3. Create post (`tests/post.spec.ts`)
1. Composer submit is disabled until title + (content or image).
2. Create post with title, content and image (unique) → appears in the feed with image loaded and author = Alice, and in `/Profile`.

## 4. Like (`tests/like.spec.ts`)
1. On a post created for the test: like → counter 1, button pressed; persists after reload; unlike → 0; persists after reload.

## 5. Comment (`tests/comment.spec.ts`)
1. On a post created by Bob for the test, Alice comments (unique text) → comment visible with author Alice, counter 1, persists after reload.

## 6. Profile (`tests/profile.spec.ts`)
1. Own profile `/Profile` shows the user name and their posts.
2. As Bob, `/user/{aliceId}` shows Alice's name and her posts.
3. Clicking the author of a post in the feed opens `/user/{id}`.
