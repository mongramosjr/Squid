package com.squidsentry.mobile.data.model

import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.Instant

data class WaterQualityData(val timeframe: Int = 0) {
    var measured: MutableList<FloatEntry> = mutableListOf<FloatEntry>().apply { }
    var datetime: MutableList<Instant> = mutableListOf<Instant>().apply { }
}

data class WaterQualityTimeframe(val datetime: Instant = Instant.now()) {
    var dailyWaterQualityData: WaterQualityData = WaterQualityData(0)
    var weeklyWaterQualityData: WaterQualityData = WaterQualityData(1)
    var monthlyWaterQualityData: WaterQualityData = WaterQualityData(2)
    var yearlyWaterQualityData: WaterQualityData = WaterQualityData(3)
}