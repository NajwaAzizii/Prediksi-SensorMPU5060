package com.example.najwa_belajarnavigationdrawer

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.najwa_belajarnavigationdrawer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // tombol kembali
        binding.btnKembali.setOnClickListener { finish() }

        // biar enak: tekan "Done" di keyboard password => login
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doLogin()
                true
            } else false
        }

        // tombol masuk
        binding.btnMasuk.setOnClickListener { doLogin() }
    }

    private fun doLogin() {
        val email = binding.etEmail.text?.toString()?.trim().orEmpty()
        val pass = binding.etPassword.text?.toString()?.trim().orEmpty()

        // validasi email
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

        // validasi password
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
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HalamanUtama::class.java))
                    finish()
                } else {
                    val dialog = LoginError(this)
                    dialog.setMessage("Email atau Password Salah")
                    dialog.setOnRetry {
                        binding.etPassword.text?.clear()
                        binding.etPassword.requestFocus()
                    }
                    dialog.show()

                }
            }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.btnMasuk.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading

        // kalau btnMasuk adalah MaterialButton, ini aman:
        binding.btnMasuk.text = if (isLoading) "Memproses..." else "Masuk"
    }
}
