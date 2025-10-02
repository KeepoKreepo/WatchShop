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

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView; // Link back to LoginActivity
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register); // Ensure this layout exists

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize Views
        emailEditText = findViewById(R.id.editTextRegisterEmail);
        passwordEditText = findViewById(R.id.editTextRegisterPassword);
        confirmPasswordEditText = findViewById(R.id.editTextRegisterConfirmPassword);
        registerButton = findViewById(R.id.buttonRegister);
        loginTextView = findViewById(R.id.textViewGoToLogin);
        progressBar = findViewById(R.id.progressBarRegister);

        // Register Button Click Listener
        registerButton.setOnClickListener(view -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String confirmPassword = confirmPasswordEditText.getText().toString().trim();

            if (validateInput(email, password, confirmPassword)) {
                createUser(email, password);
            }
        });

        // Login Text Click Listener
        loginTextView.setOnClickListener(view -> {
            // Go back to LoginActivity
            // finish(); // Or use Intent if LoginActivity might have been finished
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Finish RegisterActivity
        });
    }

    private boolean validateInput(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required.");
            emailEditText.requestFocus();
            return false;
        }
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
        if (password.length() < 6) { // Firebase default minimum password length
            passwordEditText.setError("Password must be at least 6 characters.");
            passwordEditText.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError("Confirm Password is required.");
            confirmPasswordEditText.requestFocus();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordEditText.setError("Passwords do not match.");
            confirmPasswordEditText.requestFocus();
            // Optionally clear confirm password field: confirmPasswordEditText.setText("");
            return false;
        }
        return true;
    }

    private void createUser(String email, String password) {
        showLoading(true);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterActivity.this, "Registration Successful.", Toast.LENGTH_SHORT).show();

                        // --- IMPORTANT: Trigger Migration Check Here ---
                        // Migration usually runs on first LOGIN, not necessarily registration,
                        // but if you want data synced immediately, you could trigger it.
                        // However, the standard pattern is to migrate on login.
                        // if (user != null) {
                        //    runFirestoreMigrationIfNecessary(user, getApplicationContext());
                        // }
                        Log.d(TAG, "Registration successful. User should now log in.");
                        // --- End Migration Trigger ---

                        // Navigate to Login screen after successful registration
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        // Optional: Clear previous activities if needed
                        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish(); // Finish RegisterActivity

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            loginTextView.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            loginTextView.setEnabled(true);
        }
    }
}
