package com.squidsentry.mobile.data.model

import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.Instant
import java.time.LocalDate

data class WaterQuality(var timeframe: Int = 0) {
    var measured: MutableList<FloatEntry> = mutableListOf<FloatEntry>().apply { }
    var datetime: MutableList<Instant> = mutableListOf<Instant>().apply { }
}

data class WaterQualityTimeframe(var updatedAt: Instant = Instant.now()) {
    var dailyWaterQuality: WaterQuality = WaterQuality(0)
    var weeklyWaterQuality: WaterQuality = WaterQuality(1)
    var monthlyWaterQuality: WaterQuality = WaterQuality(2)
    var yearlyWaterQuality: WaterQuality = WaterQuality(3)
}

data class WaterQualityData(var updatedAt: Instant = Instant.now()){
    var temperature: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
    var pH: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
    var dissolvedOxygen: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
    var salinity: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
    var tds: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
    var turbidity: MutableMap<LocalDate, WaterQualityTimeframe> =
        mutableMapOf(LocalDate.now() to WaterQualityTimeframe())
}