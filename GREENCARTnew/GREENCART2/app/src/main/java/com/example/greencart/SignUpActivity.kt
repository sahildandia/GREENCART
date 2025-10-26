package com.example.greencart

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException


class SignUpActivity : AppCompatActivity() {
    private var isEmailValid = false
    private var isPasswordValid = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val emailInputLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val signUpButton = findViewById<MaterialButton>(R.id.signUpButton)
        val signInLink = findViewById<TextView>(R.id.signInLink)

        fun validateForm() {
            signUpButton.isEnabled = isEmailValid && isPasswordValid
        }

        emailInput.doAfterTextChanged { text ->
            val email = text.toString()
            if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInputLayout.error = null
                isEmailValid = true
            } else {
                emailInputLayout.error = "Invalid email address"
                isEmailValid = false
            }
            validateForm()
        }

        passwordInput.doAfterTextChanged { text ->
            if (text.toString().length >= 6) {
                passwordInputLayout.error = null
                isPasswordValid = true
            } else {
                passwordInputLayout.error = "Password must be at least 6 characters"
                isPasswordValid = false
            }
            validateForm()
        }

        fun isOnline(): Boolean {
            val cm = getSystemService<ConnectivityManager>() ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        fun mapAuthError(t: Throwable?): String = when (t) {
            is FirebaseAuthUserCollisionException -> "An account with this email already exists. Try signing in."
            is FirebaseAuthInvalidCredentialsException -> "Invalid credentials. Check your email formatting or password."

            is FirebaseNetworkException -> "Network error. Check your internet connection."
            else -> t?.localizedMessage ?: "Authentication failed."
        }

        validateForm()

        signUpButton.setOnClickListener {
            if (!isOnline()) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Send verification email
                        val user = auth.currentUser
                        user?.sendEmailVerification()
                            ?.addOnCompleteListener { vTask ->
                                if (vTask.isSuccessful) {
                                    Toast.makeText(
                                        baseContext,
                                        "Sign up successful! Verification email sent.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(baseContext, mapAuthError(vTask.exception), Toast.LENGTH_SHORT).show()
                                }
                                finish()
                            }
                    } else {
                        Toast.makeText(baseContext, mapAuthError(task.exception), Toast.LENGTH_SHORT).show()
                    }
                }
        }

        signInLink.setOnClickListener { finish() }
    }
}
