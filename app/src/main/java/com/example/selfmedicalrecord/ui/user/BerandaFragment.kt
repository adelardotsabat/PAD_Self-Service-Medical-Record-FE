package com.example.selfmedicalrecord.ui.user

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.selfmedicalrecord.databinding.FragmentBerandaBinding
import com.example.selfmedicalrecord.utils.applyFigmaShadow
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class BerandaFragment : Fragment() {

    private var _binding: FragmentBerandaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBerandaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Terapkan Soft Shadow ala Figma
        setupSoftShadows()

        // 2. Load Dummy Data Grafik
        setupBmiChart()
        setupBpChart()
    }

    private fun setupSoftShadows() {
        binding.btnSearch.applyFigmaShadow(4f)
        binding.btnNotification.applyFigmaShadow(4f)

        binding.cardWeight.applyFigmaShadow(6f)
        binding.cardHeight.applyFigmaShadow(6f)
        binding.cardBmi.applyFigmaShadow(6f)
        binding.cardBp.applyFigmaShadow(6f)
    }

    private fun setupBmiChart() {
        val entries = listOf(
            Entry(0f, 18.2f),
            Entry(1f, 18.4f),
            Entry(2f, 18.3f),
            Entry(3f, 18.6f),
            Entry(4f, 18.5f),
            Entry(5f, 18.8f),
            Entry(6f, 18.6f)
        )

        val dataSet = LineDataSet(entries, "BMI").apply {
            color = Color.parseColor("#478CE3")
            setCircleColor(Color.parseColor("#478CE3"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartBmi.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            invalidate()
        }
    }

    private fun setupBpChart() {
        val sysEntries = listOf(
            Entry(0f, 110f), Entry(1f, 130f), Entry(2f, 118f),
            Entry(3f, 135f), Entry(4f, 142f), Entry(5f, 120f), Entry(6f, 128f)
        )
        val diaEntries = listOf(
            Entry(0f, 75f), Entry(1f, 80f), Entry(2f, 78f),
            Entry(3f, 88f), Entry(4f, 85f), Entry(5f, 75f), Entry(6f, 80f)
        )

        val sysDataSet = LineDataSet(sysEntries, "Sistolik").apply {
            color = Color.parseColor("#478CE3")
            setCircleColor(Color.parseColor("#478CE3"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val diaDataSet = LineDataSet(diaEntries, "Diastolik").apply {
            color = Color.parseColor("#9DC1F4")
            setCircleColor(Color.parseColor("#9DC1F4"))
            lineWidth = 2.5f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        binding.chartBp.apply {
            data = LineData(sysDataSet, diaDataSet)
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Mencegah memory leak
    }
}