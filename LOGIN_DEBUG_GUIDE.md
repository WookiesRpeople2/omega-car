# Login Debug Guide

## What I've Fixed

### 1. Database Boolean Type Issue
- Fixed `User.emailValidated` field to use `columnDefinition = "TINYINT(1)"` for MySQL compatibility
- Changed boolean comparison from `==` to `Boolean.TRUE.equals()` for null-safety

### 2. Added Comprehensive Debug Logging

The application now logs at every step of the login process:

#### A. Login & Token Generation (UserService)
- User lookup
- Email validation status
- Password verification
- Token generation

#### B. Cookie Setting (LoginView)
- API response
- Token presence and length
- Cookie creation
- Navigation decision

#### C. Cookie Reading (JwtAuthFilter)
- Request path
- Cookie presence
- Token parsing
- Authentication setting

#### D. Role Authorization (Rbac)
- Required roles
- User authentication
- User authorities
- Access decision

## How to Test

1. **Restart your Spring Boot application**
   ```bash
   # Stop any running instances
   pkill -f "java.*omega-car"
   
   # Start the application (adjust command as needed)
   mvn spring-boot:run
   # OR
   ./mvnw spring-boot:run
   ```

2. **Try to login** at http://localhost:8080/login
   - Email: keenan.tobiansky@gmail.com
   - Password: [your password]

3. **Watch the console logs** - you should see DEBUG messages showing:
   - ✅ DEBUG: User found
   - ✅ DEBUG: Email validated: true
   - ✅ DEBUG: Password match: true
   - ✅ DEBUG: Login successful, token generated
   - ✅ DEBUG LoginView: Token length: [number]
   - ✅ DEBUG LoginView: Cookie added to response
   - ✅ DEBUG JWT Filter: Token from cookie=present
   - ✅ DEBUG JWT Filter: Successfully authenticated
   - ✅ DEBUG Rbac: Access granted

## Common Issues to Look For

### Issue 1: Token not generated
**Symptoms:** "DEBUG: Password match: true" but no "Login successful" message
**Solution:** Check JWT configuration in application.properties

### Issue 2: Cookie not set
**Symptoms:** Login succeeds but JWT Filter shows "No cookies in request"
**Solution:** Cookie setting issue in Vaadin - might need SameSite or Secure flag adjustment

### Issue 3: Cookie not read
**Symptoms:** Cookie is set but JWT Filter doesn't find it
**Solution:** Check cookie name matches ("AUTH")

### Issue 4: Token parsing fails
**Symptoms:** "Token parsing failed" in JWT Filter
**Solution:** Check jwt.signing.secret and jwt.aes.key in application.properties

### Issue 5: Role mismatch
**Symptoms:** Token parsed but Rbac denies access
**Solution:** Check role in database matches expected role ("User", "Driver", or "Admin")

## Quick Manual Email Verification

If you need to manually verify the email (bypassing the token):

```bash
curl -X POST http://localhost:8080/api/debug/verify-email-manual \
  -H "Content-Type: application/json" \
  -d '{"email":"keenan.tobiansky@gmail.com"}'
```

## Debug Endpoints

While the application is running, you can check:

1. **List all users:**
   ```bash
   curl http://localhost:8080/api/debug/users
   ```

2. **Check password:**
   ```bash
   curl -X POST http://localhost:8080/api/debug/check-password \
     -H "Content-Type: application/json" \
     -d '{"email":"keenan.tobiansky@gmail.com","password":"your-password"}'
   ```

## Next Steps

After testing, **please share:**
1. The DEBUG log output from your console
2. What you see in the browser (error message, blank page, redirects, etc.)
3. Any JavaScript console errors (F12 in browser)

This will help pinpoint the exact issue!

