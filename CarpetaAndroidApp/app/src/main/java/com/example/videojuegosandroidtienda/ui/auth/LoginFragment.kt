package com.example.videojuegosandroidtienda.ui.auth

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
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
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginFragment : Fragment() {
    private val repository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inputEmail = view.findViewById<EditText>(R.id.inputEmail)
        val inputPassword = view.findViewById<EditText>(R.id.inputPassword)
        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val linkSignup = view.findViewById<TextView>(R.id.linkSignup)

        buttonLogin.setOnClickListener {
            val email = inputEmail.text.toString()
            val password = inputPassword.text.toString()
            if (email.isBlank() || password.isBlank()) {
                val dialog = Dialog(requireActivity())
                dialog.setContentView(R.layout.custom_toast_error)
                dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                val textView = dialog.findViewById<TextView>(R.id.toast_text)
                textView.text = getString(R.string.login_fill_credentials)

                dialog.show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    repository.login(email, password)
                    val authUser = repository.getAuthMe()
                    val fullUser = UserRepository().getUser(authUser.id)
                    if (fullUser.bloqueo) {
                        repository.logout()
                        showCustomErrorToast(requireActivity(), getString(R.string.user_blocked))
                        return@launch
                    }

                    showCustomOkToast(requireActivity(), getString(R.string.login_success))
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } catch (e: Exception) {
                    val baseMsg = getString(R.string.login_error_base)
                    if (e is HttpException) {
                        when (e.code()) {
                            403 -> {
                                showCustomErrorToast(requireActivity(), getString(R.string.login_invalid_credentials))
                            }
                            429 -> {
                                showCustomErrorToast(requireActivity(), getString(R.string.api_limit_error_retry))
                            }
                            else -> {
                                val errBody = e.response()?.errorBody()?.string()
                                Log.e("LoginFragment", "HTTP ${e.code()} ${errBody ?: ""}")
                                showCustomErrorToast(requireActivity(), getString(R.string.http_error_format, baseMsg, e.code()))
                            }
                        }
                    } else {
                        Log.e("LoginFragment", e.localizedMessage ?: baseMsg)
                        showCustomErrorToast(requireActivity(), baseMsg)
                    }
                }
            }
        }

        linkSignup.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignUpFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
