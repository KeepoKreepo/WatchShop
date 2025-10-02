package com.example.watchshop; // Use your package name

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView; // Link to RegisterActivity
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Ensure this layout exists

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.editTextLoginEmail);
        passwordEditText = findViewById(R.id.editTextLoginPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerTextView = findViewById(R.id.textViewGoToRegister);
        progressBar = findViewById(R.id.progressBarLogin);

        // Login Button Click Listener
        loginButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (validateInput(email, password)) {
                signInUser(email, password);
            }
        });

        // Register Text Click Listener
        registerTextView.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, com.example.watchshop.RegisterActivity.class);
            startActivity(intent);
            // finish(); // Optional: finish LoginActivity if you don't want user to go back
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // User is already logged in, navigate to MainActivity
            Log.d(TAG, "User already logged in: " + currentUser.getUid());
            navigateToMain();
        } else {
            Log.d(TAG, "No user logged in.");
        }
    }


    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
            return false;
        }
        // Basic email format check (can be improved)
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("Enter a valid email address.");
            emailEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required.");
            passwordEditText.requestFocus();
            return false;
        }
        // Optional: Add password length check
        // if (password.length() < 6) {
        //     passwordEditText.setError("Password must be at least 6 characters.");
        //     passwordEditText.requestFocus();
        //     return false;
        // }
        return true;
    }

    private void signInUser(String email, String password) {
        showLoading(true);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();

                        // --- IMPORTANT: Trigger Migration Check Here ---
                        if (user != null) {
                            // Call the migration logic function you created
                            // runFirestoreMigrationIfNecessary(user, getApplicationContext());
                            Log.d(TAG, "Login successful, proceeding to migration check (if implemented).");
                        }
                        // --- End Migration Trigger ---

                        navigateToMain();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        // Clear back stack so user can't go back to Login screen after logging in
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Finish LoginActivity
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            registerTextView.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            registerTextView.setEnabled(true);
        }
    }
}
