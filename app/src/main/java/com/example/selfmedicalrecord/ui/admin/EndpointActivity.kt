package com.example.selfmedicalrecord.ui.admin

import android.app.Dialog
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.R
import com.example.selfmedicalrecord.databinding.ActivityEndpointBinding
import com.example.selfmedicalrecord.utils.applySystemBarInsetsAsPadding
import com.example.selfmedicalrecord.utils.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/** Atur Endpoint: URL server, port, API key, dan saklar koneksi integrasi. */
class EndpointActivity : AdminActivity() {

    private lateinit var binding: ActivityEndpointBinding
    private val viewModel: EndpointViewModel by viewModels()
    private var resultDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!ensureAdmin()) return

        binding = ActivityEndpointBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.root.applySystemBarInsetsAsPadding()

        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.etUrl.doAfterTextChanged { viewModel.onInputChanged() }
        binding.etPort.doAfterTextChanged { viewModel.onInputChanged() }
        binding.etApiKey.doAfterTextChanged { viewModel.onInputChanged() }
        binding.btnSave.setOnClickListener {
            hideKeyboard()
            viewModel.save(url(), port(), apiKey(), binding.switchEnabled.isChecked)
        }
        binding.btnPing.setOnClickListener {
            hideKeyboard()
            viewModel.ping(url(), port(), apiKey(), binding.switchEnabled.isChecked)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { state -> render(state) } }
                launch { viewModel.events.collect { event -> handle(event) } }
            }
        }
    }

    private fun handle(event: EndpointEvent) {
        resultDialog?.dismiss()
        resultDialog = when (event) {
            is EndpointEvent.Message -> {
                Snackbar.make(binding.root, event.resId, Snackbar.LENGTH_SHORT).show()
                null
            }
            EndpointEvent.SaveSucceeded -> ResultDialog.show(
                context = this,
                kind = ResultDialog.Kind.SUCCESS,
                title = R.string.admin_popup_saved_title,
                message = R.string.admin_popup_saved_message,
                primaryText = R.string.admin_popup_understood
            )
            EndpointEvent.PingSucceeded -> ResultDialog.show(
                context = this,
                kind = ResultDialog.Kind.SUCCESS,
                title = R.string.admin_popup_ping_ok_title,
                message = R.string.admin_popup_ping_ok_message,
                primaryText = R.string.admin_popup_done
            )
            EndpointEvent.PingFailed -> ResultDialog.show(
                context = this,
                kind = ResultDialog.Kind.ERROR,
                title = R.string.admin_popup_ping_failed_title,
                message = R.string.admin_popup_ping_failed_message,
                primaryText = R.string.admin_popup_retry,
                onPrimary = { viewModel.ping(url(), port(), apiKey(), binding.switchEnabled.isChecked) },
                secondaryText = R.string.admin_popup_back_to_settings
            )
        }
    }

    override fun onDestroy() {
        resultDialog?.dismiss()
        resultDialog = null
        super.onDestroy()
    }

    private fun url() = binding.etUrl.text?.toString().orEmpty()
    private fun port() = binding.etPort.text?.toString().orEmpty()
    private fun apiKey() = binding.etApiKey.text?.toString().orEmpty()

    private fun render(state: EndpointUiState) {
        state.config?.let { config ->
            binding.etUrl.setText(config.serverUrl)
            binding.etPort.setText(config.port)
            binding.etApiKey.setText(config.apiKey)
            binding.switchEnabled.isChecked = config.enabled
            viewModel.onConfigShown()
        }

        binding.tilUrl.error = state.urlError?.let { getString(it) }
        binding.tilPort.error = state.portError?.let { getString(it) }
        binding.tilApiKey.error = state.apiKeyError?.let { getString(it) }

        binding.btnSave.isEnabled = !state.isBusy
        binding.btnPing.isEnabled = !state.isBusy
        binding.btnSave.setText(if (state.isSaving) R.string.action_loading else R.string.admin_endpoint_save)
        binding.btnPing.setText(if (state.isPinging) R.string.admin_endpoint_pinging else R.string.admin_endpoint_ping)
    }
}
