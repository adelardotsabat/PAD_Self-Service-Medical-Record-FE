package com.example.selfmedicalrecord.ui.user

import androidx.annotation.ColorInt
import com.example.selfmedicalrecord.utils.DayUtils
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToLong

/** Konfigurasi dan penggambaran grafik garis Beranda dari [ChartData]. */
internal object ChartRenderer {

    /** Dipanggil sekali per grafik saat view dibuat. */
    fun setup(chart: LineChart, noDataText: String, @ColorInt textColor: Int, @ColorInt gridColor: Int) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setScaleEnabled(false)
            setNoDataText(noDataText)
            setNoDataTextColor(textColor)
            setExtraOffsets(4f, 8f, 12f, 4f)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                this.textColor = textColor
            }
            axisLeft.apply {
                setDrawAxisLine(false)
                this.gridColor = gridColor
                this.textColor = textColor
            }
        }
    }

    /**
     * @param colors warna tiap garis, searah dengan [ChartData.lines]
     * @param defaultMin batas bawah sumbu Y minimum (diperlebar otomatis jika data keluar rentang)
     * @param defaultMax batas atas sumbu Y minimum
     */
    fun render(
        chart: LineChart,
        data: ChartData,
        @ColorInt colors: List<Int>,
        defaultMin: Float,
        defaultMax: Float
    ) {
        if (data.isEmpty) {
            chart.clear() // menampilkan teks "tidak ada data"
            return
        }

        val isWeek = data.period == ChartPeriod.WEEK
        val dataSets = data.lines.mapIndexedNotNull { index, points ->
            if (points.isEmpty()) return@mapIndexedNotNull null
            LineDataSet(points.map { Entry(it.x, it.y) }, "").apply {
                color = colors[index]
                setCircleColor(colors[index])
                lineWidth = 2.5f
                circleRadius = 4f
                setDrawCircles(points.size <= MAX_POINTS_WITH_CIRCLES)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }
        }

        chart.xAxis.apply {
            axisMinimum = 0f
            axisMaximum = (data.period.days - 1).toFloat()
            if (isWeek) setLabelCount(data.period.days, true) else setLabelCount(5, false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val day = data.startEpochDay + value.roundToLong()
                    return if (isWeek) DayUtils.weekdayShort(day) else DayUtils.format(day, "d MMM")
                }
            }
        }

        val values = data.lines.flatten().map { it.y }
        chart.axisLeft.apply {
            axisMinimum = min(defaultMin, floor(values.min()) - 1f)
            axisMaximum = max(defaultMax, ceil(values.max()) + 1f)
        }

        chart.data = LineData(dataSets)
        chart.invalidate()
    }

    private const val MAX_POINTS_WITH_CIRCLES = 31
}
