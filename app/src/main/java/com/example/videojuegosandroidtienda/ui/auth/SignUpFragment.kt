package com.example.videojuegosandroidtienda.ui.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class SignUpFragment : Fragment() {
    private val repository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputName = view.findViewById<EditText>(R.id.inputName)
        val inputEmail = view.findViewById<EditText>(R.id.inputEmail)
        val inputPassword = view.findViewById<EditText>(R.id.inputPassword)
        val buttonSignup = view.findViewById<Button>(R.id.buttonSignup)

        buttonSignup.setOnClickListener {
            val name = inputName.text.toString()
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()

            if (name.isBlank() || email.isBlank() || password.isBlank()) {
                showCustomErrorToast(requireActivity(), getString(R.string.signup_fields_required))
                return@setOnClickListener
            }
            if (password.length < 8) {
                showCustomErrorToast(requireActivity(), getString(R.string.password_length_error))
                return@setOnClickListener
            }
            if (!password.any { it.isLetter() }) {
                showCustomErrorToast(requireActivity(), getString(R.string.password_letter_error))
                return@setOnClickListener
            }
            if (!password.any { !it.isLetterOrDigit() }) {
                showCustomErrorToast(requireActivity(), getString(R.string.password_special_char_error))
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    repository.register(name, email, password)
                    showCustomOkToast(requireActivity(), getString(R.string.signup_succes))
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } catch (e: Exception) {
                    val baseMsg = "Error al crear cuenta (Comunicar con soporte)"
                    if (e is HttpException) {
                        val code = e.code()
                        val errBody = e.response()?.errorBody()?.string()
                        Log.e("SignUpFragment", "HTTP $code ${errBody ?: ""}")
                        val message = if (code == 429) {
                            getString(R.string.api_limit_error_retry)
                        } else {
                            getString(R.string.http_error_format, baseMsg, code)
                        }
                        showCustomErrorToast(requireActivity(), message)
                    } else {
                        Log.e("SignUpFragment", e.localizedMessage ?: baseMsg)
                        showCustomErrorToast(requireActivity(), baseMsg)
                    }
                }
            }
        }
    }
}
