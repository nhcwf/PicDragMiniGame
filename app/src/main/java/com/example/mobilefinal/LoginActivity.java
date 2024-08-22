/**
 *
 * @author  THTN
 * @version 1.2
 * @since   2023-10-26
 */
package com.example.mobilefinal;

import static com.example.mobilefinal.DatabaseHelper.DATABASE_NAME;
import static com.example.mobilefinal.DatabaseHelper.DATABASE_VERSION;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    public static final String USERNAME_STRING = "usernameString";
    public static final String PASSWORD_STRING = "passwordString";
    public static final String USERNAME_BUNDLE = "usernameBundle";
    Button login, signup;
    EditText username, password;
    ImageView passwordVisibility;
    boolean password_is_visible = false;
    DatabaseHelper database;

    @SuppressLint({"MissingInflatedId", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = new DatabaseHelper(LoginActivity.this, DATABASE_NAME, null, DATABASE_VERSION);

        assignViews();
        assignListeners();
    }

    // Assigns views to local view variables using findViewById().
    private void assignViews() {
        username = (EditText) findViewById(R.id.et_email);
        password = (EditText) findViewById(R.id.et_password);
        passwordVisibility = (ImageView) findViewById(R.id.iv_password_visibility);
        login = (Button) findViewById(R.id.btn_login);
        signup = (Button) findViewById(R.id.btn_sign_in);
    }

    // Assigns view listeners to specific individual views.
    private void assignListeners() {
        login.setOnClickListener(loginOnClickListener);
        signup.setOnClickListener(signinOnClickListener);
        passwordVisibility.setOnClickListener(passwordVisibilityOnClickListener);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USERNAME_STRING, username.getText().toString());
        outState.putString(PASSWORD_STRING, password.getText().toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        username.setText(savedInstanceState.getString(USERNAME_STRING));
        password.setText(savedInstanceState.getString(PASSWORD_STRING));
    }

    View.OnClickListener passwordVisibilityOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (password_is_visible) {
                password_is_visible = false;
                passwordVisibility.setImageResource(R.drawable.ic_show_password);
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                return;
            }

            password_is_visible = true;
            passwordVisibility.setImageResource(R.drawable.ic_hint_password);
            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    };

    View.OnClickListener loginOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String usernameString = LoginActivity.this.username.getText().toString();
            String passwordString = LoginActivity.this.password.getText().toString();

            if (usernameString.isEmpty() || passwordString.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter required information.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!database.isValidLogin(usernameString, passwordString)) {
                Toast.makeText(LoginActivity.this, "Incorrect username or password.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            Bundle bundle = new Bundle();

            bundle.putString(USERNAME_STRING, usernameString);
            intent.putExtra(USERNAME_BUNDLE, bundle);

            startActivity(intent);
        }
    };

    View.OnClickListener signinOnClickListener = v -> startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
}
