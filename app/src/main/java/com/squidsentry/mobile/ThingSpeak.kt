package com.squidsentry.mobile

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName

data class Channel(
    @SerializedName("id")
    @Expose
    val id: Int? = null)
{
    @SerializedName("name")
    @Expose
    val name: String? = null

    @SerializedName("description")
    @Expose
    val description: String? = null

    @SerializedName("latitude")
    @Expose
    val latitude: String? = null

    @SerializedName("longitude")
    @Expose
    val longitude: String? = null

    @SerializedName("field1")
    @Expose
    val field1: String? = null

    @SerializedName("field2")
    @Expose
    val field2: String? = null

    @SerializedName("field3")
    @Expose
    val field3: String? = null

    @SerializedName("field4")
    @Expose
    val field4: String? = null

    @SerializedName("field5")
    @Expose
    val field5: String? = null

    @SerializedName("field6")
    @Expose
    val field6: String? = null

    @SerializedName("field7")
    @Expose
    val field7: String? = null

    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null

    @SerializedName("updated_at")
    @Expose
    val updatedAt: String? = null

    @SerializedName("last_entry_id")
    @Expose
    val lastEntryId: Int? = null
}

data class Feed(
    @SerializedName("entry_id")
    @Expose
    val entryId: Int? = null
) {
    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null

    @SerializedName("field1")
    @Expose
    val field1: Float? = null

    @SerializedName("field2")
    @Expose
    val field2: Float? = null

    @SerializedName("field3")
    @Expose
    val field3: Float? = null

    @SerializedName("field4")
    @Expose
    val field4: Float? = null

    @SerializedName("field5")
    @Expose
    val field5: Float? = null

    @SerializedName("field6")
    @Expose
    val field6: Float? = null

    @SerializedName("field7")
    @Expose
    val field7: Float? = null
}

data class ThingSpeak(
    @SerializedName("channel")
    @Expose
    val channel: Channel? = null
) {
    @SerializedName("feeds")
    @Expose
    val feeds: List<Feed>? = null
}
