package com.example.greencart

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.getSystemService
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class MainActivity : AppCompatActivity() {
    private var isEmailValid = false
    private var isPasswordValid = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val mainContent = findViewById<ConstraintLayout>(R.id.main_content)
        val emailInputLayout = findViewById<TextInputLayout>(R.id.emailInputLayout)
        val passwordInputLayout = findViewById<TextInputLayout>(R.id.passwordInputLayout)
        val emailInput = findViewById<TextInputEditText>(R.id.emailInput)
        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val signInButton = findViewById<MaterialButton>(R.id.signInButton)
        val loadingIndicator = findViewById<ProgressBar>(R.id.loadingIndicator)
        val signUpLink = findViewById<TextView>(R.id.signUpLink)
        val forgotPasswordLink = findViewById<TextView>(R.id.forgotPasswordLink)

        // Animate the main content
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        mainContent.startAnimation(slideUp)

        fun validateForm() {
            signInButton.isEnabled = isEmailValid && isPasswordValid
            signInButton.background.alpha = if (signInButton.isEnabled) 255 else 128
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

        validateForm()

        fun isOnline(): Boolean {
            val cm = getSystemService<ConnectivityManager>() ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        fun mapAuthError(t: Throwable?): String {
            return when (t) {
                is FirebaseAuthInvalidUserException -> "No account found for this email. Check or sign up."
                is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
                is FirebaseNetworkException -> "Network error. Check your internet connection."
                else -> t?.localizedMessage ?: "Authentication failed."
            }
        }

        signInButton.setOnClickListener {
            if (!isOnline()) {
                Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInButton.isEnabled = false
            loadingIndicator.visibility = View.VISIBLE

            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, open home activity
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        val msg = mapAuthError(task.exception)
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    }
                    validateForm()
                    loadingIndicator.visibility = View.GONE
                }
        }

        signUpLink.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordLink.setOnClickListener {
            val emailEditText = EditText(this)
            emailEditText.hint = "Enter your email"

            val passwordResetDialog = AlertDialog.Builder(this)
            passwordResetDialog.setTitle("Reset Password")
            passwordResetDialog.setMessage("Enter your email address and we'll send you a link to reset your password.")
            passwordResetDialog.setView(emailEditText)

            passwordResetDialog.setPositiveButton("Send Reset Link") { _, _ ->
                val email = emailEditText.text.toString()
                if (email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (!isOnline()) {
                        Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { resetTask ->
                            if (resetTask.isSuccessful) {
                                val resetLinkSentDialog = AlertDialog.Builder(this)
                                resetLinkSentDialog.setTitle("Reset Link Sent!")
                                resetLinkSentDialog.setMessage("Check your email for a link to reset your password.")
                                resetLinkSentDialog.setPositiveButton("Got it") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                resetLinkSentDialog.show()
                            } else {
                                Toast.makeText(baseContext, mapAuthError(resetTask.exception), Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(baseContext, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
            }

            passwordResetDialog.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            passwordResetDialog.show()
        }
    }
}
