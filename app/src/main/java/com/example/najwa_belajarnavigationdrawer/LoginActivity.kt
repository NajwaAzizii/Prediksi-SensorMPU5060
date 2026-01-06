package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLoginBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    // ✅ daftar email admin yang diizinkan (pakai lowercase semua)
    private val adminEmails = setOf(
        "najwa23ti@mahasiswa.pcr.ac.id",
        "admin@gmail.com"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.btnKembali.setOnClickListener { finish() }

        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doLogin()
                true
            } else false
        }

        binding.btnMasuk.setOnClickListener { doLogin() }

        // ✅ Lupa Password
        binding.tvForgot.setOnClickListener { doForgotPassword() }
    }

    private fun doForgotPassword() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            binding.etEmail.error = "Isi email dulu untuk reset password"
            binding.etEmail.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Reset Password")
            .setMessage("Kirim link reset password ke:\n$email ?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Kirim") { _, _ ->
                setLoading(true)
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        setLoading(false)
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Link reset sudah dikirim. Cek email kamu.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Gagal mengirim reset: ${task.exception?.message ?: "Unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            .show()
    }

    private fun doLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString()?.trim().orEmpty()

        if (email.isEmpty()) {
            binding.etEmail.error = "Email wajib diisi"
            binding.etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return
        }
        if (pass.isEmpty()) {
            binding.etPassword.error = "Password wajib diisi"
            binding.etPassword.requestFocus()
            return
        }
        if (pass.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return
        }

        setLoading(true)

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this) { task ->
                setLoading(false)

                if (task.isSuccessful) {
                    val userEmail = task.result.user?.email?.trim()?.lowercase()

                    if (userEmail != null && adminEmails.contains(userEmail)) {
                        val i = Intent(this, BlogListActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(i)
                    } else {
                        auth.signOut()
                        showError("Akun ini bukan admin")
                    }
                } else {
                    showError("Email atau Password Salah")
                }
            }
    }

    private fun showError(msg: String) {
        val dialog = LoginError(this)
        dialog.setMessage(msg)
        dialog.setOnRetry {
            binding.etPassword.text?.clear()
            binding.etPassword.requestFocus()
        }
        dialog.show()
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnMasuk.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
        binding.tvForgot.isEnabled = !isLoading
        binding.btnKembali.isEnabled = !isLoading

        binding.btnMasuk.text = if (isLoading) "Memproses..." else "Masuk"
        binding.tvForgot.alpha = if (isLoading) 0.5f else 1f
    }
}