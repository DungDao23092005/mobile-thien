package com.example.stushare.core.data.network.models

import com.example.stushare.core.data.models.Document
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DocumentDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    // 🟢 THÊM:
    @Json(name = "description") val description: String? = null,
    @Json(name = "type") val type: String,
    @Json(name = "image_url") val imageUrl: String,
    @Json(name = "file_url") val fileUrl: String? = null,
    @Json(name = "downloads") val downloads: Int,
    @Json(name = "rating") val rating: Float,
    @Json(name = "author") val author: String,
    @Json(name = "course_code") val courseCode: String
)

fun DocumentDto.toDocumentEntity(): Document {
    return Document(
        id = this.id,
        title = this.title,
        // 🟢 THÊM:
        description = this.description ?: "",
        type = this.type,
        imageUrl = this.imageUrl,
        fileUrl = this.fileUrl ?: "",
        downloads = this.downloads,
        rating = this.rating.toDouble(),
        author = this.author,
        courseCode = this.courseCode,
        createdAt = System.currentTimeMillis(),
        authorId = null
    )
}