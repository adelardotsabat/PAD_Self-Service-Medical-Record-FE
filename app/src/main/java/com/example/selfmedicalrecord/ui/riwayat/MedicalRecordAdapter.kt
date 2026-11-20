package com.example.selfmedicalrecord.ui.riwayat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.selfmedicalrecord.data.record.MedicalRecord
import com.example.selfmedicalrecord.databinding.ItemMedicalRecordBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * Menampilkan daftar kartu riwayat. ListAdapter + DiffUtil hanya menggambar ulang yang berubah.
 * [onEdit] dipanggil saat ikon pensil ditekan; ikon hanya muncul di catatan mandiri.
 */
class MedicalRecordAdapter(
    private val onEdit: (MedicalRecord) -> Unit = {}
) : ListAdapter<MedicalRecord, MedicalRecordAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(
        private val binding: ItemMedicalRecordBinding,
        private val onEdit: (MedicalRecord) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Tanggal disimpan UTC, jadi diformat UTC juga agar tidak bergeser sehari.
        private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        fun bind(item: MedicalRecord) {
            binding.tvFacility.text = item.facilityName
            binding.tvDate.text = dateFormat.format(item.visitDateMillis)

            // Baris yang tidak ada isinya disembunyikan (diagnosa hanya ada di catatan klinik).
            binding.rowDiagnosis.isVisible = !item.diagnosis.isNullOrBlank()
            binding.tvDiagnosis.text = item.diagnosis.orEmpty()
            binding.tvComplaint.text = item.complaint
            binding.rowMedicine.isVisible = item.medications.isNotBlank()
            binding.tvMedicine.text = item.medications

            binding.btnEdit.isVisible = item.isEditable
            binding.btnEdit.setOnClickListener { onEdit(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            ItemMedicalRecordBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            onEdit
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private object DiffCallback : DiffUtil.ItemCallback<MedicalRecord>() {
        override fun areItemsTheSame(old: MedicalRecord, new: MedicalRecord) = old.id == new.id
        override fun areContentsTheSame(old: MedicalRecord, new: MedicalRecord) = old == new
    }
}
