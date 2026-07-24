package com.rahul.dailytask.controller;

import com.rahul.dailytask.entity.User;
import com.rahul.dailytask.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // =========================================
    // REGISTER
    // =========================================
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("📥 Registration request received");
            System.out.println("📝 Data: " + request);

            String fullName = (String) request.get("fullName");
            String email = (String) request.get("email");
            String username = (String) request.get("username");
            String password = (String) request.get("password");
            String phone = (String) request.get("phone");
            String company = (String) request.get("company");

            // ===== VALIDATION =====
            if (fullName == null || fullName.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Full name is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (username == null || username.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Username is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (password == null || password.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (password.length() < 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password must be at least 6 characters");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Check if email exists
            if (userRepository.existsByEmail(email)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email already registered. Please use a different email.");
                errorResponse.put("error", "EMAIL_EXISTS");
                errorResponse.put("field", "email");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // Check if username exists
            if (userRepository.existsByUsername(username)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Username already taken. Please choose a different username.");
                errorResponse.put("error", "USERNAME_EXISTS");
                errorResponse.put("field", "username");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }

            // ===== CREATE USER =====
            User user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setPhone(phone != null ? phone : "");
            user.setCompany(company != null ? company : "");
            user.setRole("USER");
            user.setStatus("ACTIVE");
            user.setCreatedDate(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            System.out.println("✅ User saved successfully! ID: " + savedUser.getId());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Registration successful! Please login.");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", savedUser.getId());
            userData.put("fullName", savedUser.getFullName());
            userData.put("email", savedUser.getEmail());
            userData.put("username", savedUser.getUsername());
            userData.put("role", savedUser.getRole());
            response.put("user", userData);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            System.err.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Registration failed: " + e.getMessage());
            errorResponse.put("error", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // =========================================
    // LOGIN
    // =========================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            System.out.println("📥 Login request received for: " + request.get("email"));

            String email = request.get("email");
            String password = request.get("password");

            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (password == null || password.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid email or password");
                errorResponse.put("error", "INVALID_CREDENTIALS");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid email or password");
                errorResponse.put("error", "INVALID_CREDENTIALS");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful!");

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("fullName", user.getFullName());
            userData.put("email", user.getEmail());
            userData.put("username", user.getUsername());
            userData.put("role", user.getRole());
            userData.put("company", user.getCompany() != null ? user.getCompany() : "");
            userData.put("phone", user.getPhone() != null ? user.getPhone() : "");
            response.put("user", userData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Login failed: " + e.getMessage());
            errorResponse.put("error", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // =========================================
    // FORGOT PASSWORD - WITH EMAIL LOGGING
    // =========================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String phone = request.get("phone");
            String method = request.get("method");

            System.out.println("📥 Forgot password request:");
            System.out.println("   Email: " + email);
            System.out.println("   Phone: " + phone);
            System.out.println("   Method: " + method);

            // ===== VALIDATION =====
            if (method == null || method.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Please select a delivery method (email or phone)");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            User user = null;

            // ===== FIND USER BY EMAIL OR PHONE =====
            if (method.equalsIgnoreCase("email")) {
                if (email == null || email.trim().isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Email is required");
                    errorResponse.put("error", "VALIDATION_ERROR");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                user = userRepository.findByEmail(email).orElse(null);
                
                if (user == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "If your email is registered, you will receive a password reset link.");
                    response.put("method", "email");
                    return ResponseEntity.ok(response);
                }
                
            } else if (method.equalsIgnoreCase("phone")) {
                if (phone == null || phone.trim().isEmpty()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "Phone number is required");
                    errorResponse.put("error", "VALIDATION_ERROR");
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                user = userRepository.findByPhone(phone).orElse(null);
                
                if (user == null) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("message", "If your phone number is registered, you will receive a password reset link.");
                    response.put("method", "phone");
                    return ResponseEntity.ok(response);
                }
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid delivery method. Use 'email' or 'phone'");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // ===== GENERATE RESET TOKEN =====
            String resetToken = UUID.randomUUID().toString();
            
            // ===== SAVE TOKEN TO USER WITH EXPIRY (1 hour) =====
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);

            String resetLink = "http://localhost:8080/reset-password.html?token=" + resetToken;

            // =========================================
            // SEND VIA SELECTED METHOD
            // =========================================
            String deliveryMessage = "";

            if (method.equalsIgnoreCase("email")) {
                // For development, print link to console
                System.out.println("=========================================");
                System.out.println("📧 PASSWORD RESET LINK (EMAIL)");
                System.out.println("To: " + user.getEmail());
                System.out.println("Link: " + resetLink);
                System.out.println("=========================================");
                
                // TODO: Uncomment when email is configured
                // sendResetEmail(user.getEmail(), resetLink);
                
                deliveryMessage = "Password reset link sent to your email: " + user.getEmail();
                
            } else if (method.equalsIgnoreCase("phone")) {
                // For development, print link to console
                System.out.println("=========================================");
                System.out.println("📱 PASSWORD RESET LINK (PHONE)");
                System.out.println("To: " + user.getPhone());
                System.out.println("Link: " + resetLink);
                System.out.println("=========================================");
                
                // TODO: Implement SMS sending
                // sendResetSms(user.getPhone(), resetLink);
                
                deliveryMessage = "Password reset link sent to your phone number: " + user.getPhone();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", deliveryMessage);
            response.put("method", method);
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());

            // For development only - show link in response
            response.put("resetLink", resetLink);
            response.put("resetToken", resetToken);

            System.out.println("🔑 Reset Token: " + resetToken);
            System.out.println("🔗 Reset Link: " + resetLink);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Forgot password error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process request. Please try again.");
            errorResponse.put("error", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // =========================================
    // RESET PASSWORD
    // =========================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");

            System.out.println("📥 Reset password request received");
            System.out.println("🔑 Token: " + token);

            if (token == null || token.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid reset token");
                errorResponse.put("error", "INVALID_TOKEN");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "New password is required");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            if (newPassword.length() < 6) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Password must be at least 6 characters");
                errorResponse.put("error", "VALIDATION_ERROR");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            User user = userRepository.findByResetToken(token).orElse(null);
            
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Invalid or expired reset token");
                errorResponse.put("error", "INVALID_TOKEN");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Reset token has expired. Please request a new one.");
                errorResponse.put("error", "TOKEN_EXPIRED");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }

            // Update password
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setResetToken(null);
            user.setResetTokenExpiry(null);
            userRepository.save(user);

            System.out.println("✅ Password reset successfully for: " + user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully!");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("❌ Reset password error: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to reset password. Please try again.");
            errorResponse.put("error", "SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // =========================================
    // TEST ENDPOINT
    // =========================================
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Auth API is working!");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }

    // =========================================
    // CHECK EMAIL
    // =========================================
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("email", email);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }

    // =========================================
    // CHECK USERNAME
    // =========================================
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        Map<String, Object> response = new HashMap<>();
        response.put("exists", exists);
        response.put("username", username);
        response.put("available", !exists);
        return ResponseEntity.ok(response);
    }
}