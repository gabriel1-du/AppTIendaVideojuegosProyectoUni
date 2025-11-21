package com.example.videojuegosandroidtienda.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.videojuegosandroidtienda.MainActivity
import com.example.videojuegosandroidtienda.R
import com.example.videojuegosandroidtienda.data.functions.showCustomErrorToast
import com.example.videojuegosandroidtienda.data.functions.showCustomOkToast
import com.example.videojuegosandroidtienda.data.network.TokenStore
import com.example.videojuegosandroidtienda.data.repository.AuthRepository
import com.example.videojuegosandroidtienda.data.repository.StoreRepository.UserRepository
import com.example.videojuegosandroidtienda.databinding.FragmentProfileBinding
import com.example.videojuegosandroidtienda.ui.adminUi.AdminDashboardActivity
import com.example.videojuegosandroidtienda.ui.auth.LoginFragment
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val repository = AuthRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        repository.loadPersistedToken()

        val token = TokenStore.token
        if (token.isNullOrBlank()) {
            startActivity(Intent(requireContext(), LoginFragment::class.java))
            activity?.finish()
            return
        }

        binding.buttonIraDashboard.setOnClickListener {
            startActivity(Intent(requireContext(), AdminDashboardActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val authUser = repository.getAuthMe()
                val fetchedUser = UserRepository().getUser(authUser.id)
                if (fetchedUser.bloqueo) {
                    repository.logout()
                    showCustomErrorToast(requireContext(), getString(R.string.user_blocked))
                    startActivity(Intent(requireContext(), LoginFragment::class.java))
                    activity?.finish()
                    return@launch
                }
                binding.textName.text = getString(R.string.profile_name_format, fetchedUser.name)
                binding.textEmail.text = getString(R.string.profile_email_format, fetchedUser.email)

                if (fetchedUser.admin) {
                    binding.buttonIraDashboard.visibility = View.VISIBLE
                } else {
                    binding.buttonIraDashboard.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.buttonIraDashboard.visibility = View.GONE
                if (e is HttpException && e.code() == 429) {
                    showCustomErrorToast(requireContext(), getString(R.string.api_limit_error_retry))
                } else {
                    showCustomErrorToast(requireContext(), getString(R.string.profile_load_error))
                }
            }
        }

        binding.buttonLogout.setOnClickListener {
            repository.logout()
            showCustomOkToast(requireContext(), getString(R.string.logout_success))
            startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            activity?.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
