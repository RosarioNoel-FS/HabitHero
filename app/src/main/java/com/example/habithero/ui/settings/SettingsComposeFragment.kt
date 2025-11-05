package com.example.habithero.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.habithero.AuthenticationActivity
import com.example.habithero.ui.theme.HeroTheme

class SettingsComposeFragment : Fragment() {

    private lateinit var viewModel: SettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val state by viewModel.state.collectAsState()

                HeroTheme {
                    SettingsScreen(
                        state = state,
                        onBack = { requireActivity().supportFragmentManager.popBackStack() },
                        onChangeName = { viewModel.onNameChange(it) },
                        onSaveName = { viewModel.saveName() },
                        onPickPhoto = { viewModel.uploadPhoto(it) },
                        onSignOut = {
                            viewModel.signOut()
                            // Navigate to the new, correct AuthenticationActivity
                            val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}
