/**
 *
 * @author  THTN
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

import static com.example.mobilefinal.DatabaseHelper.DATABASE_NAME;
import static com.example.mobilefinal.DatabaseHelper.DATABASE_VERSION;
import static com.example.mobilefinal.LoginActivity.PASSWORD_STRING;
import static com.example.mobilefinal.LoginActivity.USERNAME_STRING;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    public static final String CONFIRM_PASSWORD_STRING = "confirmPasswordString";
    Button registerButton, returnButton;
    DatabaseHelper database;
    EditText username, password, confirmPassword;
    ImageView passwordVisibility, confirmPasswordVisibility;
    boolean password_is_visible = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        database = new DatabaseHelper(this, DATABASE_NAME, null, DATABASE_VERSION);

        assignViews();
        assignListeners();
    }

    // Assigns views to local view variables using findViewById().
    private void assignViews() {
        username = (EditText) findViewById(R.id.et_register_email);
        password = (EditText) findViewById(R.id.et_register_password);
        confirmPassword = (EditText) findViewById(R.id.et_register_confirm_password);
        passwordVisibility = (ImageView) findViewById(R.id.iv_register_password_visibility);
        confirmPasswordVisibility = (ImageView) findViewById(R.id.iv_register_confirm_password_visibility);
        registerButton = (Button) findViewById(R.id.btn_register);
        returnButton = (Button) findViewById(R.id.btn_return);
    }

    // Assigns view listeners to specific individual views.
    private void assignListeners() {
        registerButton.setOnClickListener(registerOnClickListener);
        returnButton.setOnClickListener(returnOnClickListener);
        passwordVisibility.setOnClickListener(passwordVisibilityOnClickListener);
        confirmPasswordVisibility.setOnClickListener(passwordVisibilityOnClickListener);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USERNAME_STRING, username.getText().toString());
        outState.putString(PASSWORD_STRING, password.getText().toString());
        outState.putString(CONFIRM_PASSWORD_STRING, confirmPassword.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        username.setText(savedInstanceState.getString(USERNAME_STRING));
        password.setText(savedInstanceState.getString(PASSWORD_STRING));
        confirmPassword.setText(savedInstanceState.getString(CONFIRM_PASSWORD_STRING));
    }

    // Saves entered login data into the the database.
    View.OnClickListener registerOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String usernameString = username.getText().toString();
            String passwordString = password.getText().toString();
            String confirmPasswordString = confirmPassword.getText().toString();

            if (usernameString.isEmpty() || passwordString.isEmpty() || confirmPasswordString.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please enter required information.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passwordString.equals(confirmPasswordString)) {
                Toast.makeText(RegisterActivity.this, "Passwords not matching.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!database.usernameNotFound(usernameString)) {
                Toast.makeText(RegisterActivity.this, "Username already exists.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!database.insertUser(usernameString, passwordString)) {
                Toast.makeText(RegisterActivity.this, "Failed to create new account.", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(RegisterActivity.this, "Registered successfully.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }
    };

    View.OnClickListener returnOnClickListener = v -> finish();

    // Hides and Unhinges password text fields on user specific actions.
    View.OnClickListener passwordVisibilityOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (password_is_visible) {
                password_is_visible = false;
                passwordVisibility.setImageResource(R.drawable.ic_show_password);
                confirmPasswordVisibility.setImageResource(R.drawable.ic_show_password);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                return;
            }

            password_is_visible = true;
            passwordVisibility.setImageResource(R.drawable.ic_hint_password);
            confirmPasswordVisibility.setImageResource(R.drawable.ic_hint_password);
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    };
}
