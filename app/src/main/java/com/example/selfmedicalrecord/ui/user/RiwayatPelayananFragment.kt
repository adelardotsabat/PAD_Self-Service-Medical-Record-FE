package com.example.selfmedicalrecord.ui.user

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.selfmedicalrecord.data.record.MedicalRecord
import com.example.selfmedicalrecord.databinding.FragmentRiwayatPelayananBinding
import com.example.selfmedicalrecord.ui.riwayat.InputRiwayatActivity
import com.example.selfmedicalrecord.ui.riwayat.MedicalRecordAdapter
import com.example.selfmedicalrecord.ui.riwayat.RiwayatUiState
import com.example.selfmedicalrecord.ui.riwayat.RiwayatViewModel
import kotlinx.coroutines.launch

/** Tab Riwayat: daftar catatan pelayanan kesehatan. Fragment hanya menggambar state dari ViewModel. */
class RiwayatPelayananFragment : Fragment() {

    private var _binding: FragmentRiwayatPelayananBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RiwayatViewModel by viewModels()
    private val adapter = MedicalRecordAdapter(onEdit = ::openEdit)

    // Setelah form menyimpan (RESULT_OK), muat ulang daftar.
    private val formLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) viewModel.load()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatPelayananBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvRiwayat.adapter = adapter
        binding.btnRetry.setOnClickListener { viewModel.load() }
        binding.btnAdd.setOnClickListener {
            formLauncher.launch(Intent(requireContext(), InputRiwayatActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { render(it) }
            }
        }
    }

    private fun openEdit(record: MedicalRecord) {
        val personalId = record.personalId ?: return
        formLauncher.launch(
            Intent(requireContext(), InputRiwayatActivity::class.java)
                .putExtra(InputRiwayatActivity.EXTRA_PERSONAL_ID, personalId)
                .putExtra(InputRiwayatActivity.EXTRA_DATE_MILLIS, record.visitDateMillis)
                .putExtra(InputRiwayatActivity.EXTRA_FACILITY, record.facilityName)
                .putExtra(InputRiwayatActivity.EXTRA_COMPLAINT, record.complaint)
                .putExtra(InputRiwayatActivity.EXTRA_MEDICATIONS, record.medications)
        )
    }

    private fun render(state: RiwayatUiState) {
        adapter.submitList(state.records)
        binding.progress.isVisible = state.isLoading
        binding.layoutError.isVisible = state.hasError
        binding.layoutEmpty.isVisible =
            !state.isLoading && !state.hasError && state.records.isEmpty()
        binding.rvRiwayat.isVisible = !state.hasError
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvRiwayat.adapter = null
        _binding = null
    }
}
