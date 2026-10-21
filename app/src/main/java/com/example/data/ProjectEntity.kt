package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val aspectRatio: String,
    val durationMs: Long,
    val fps: Int,
    val lastModified: Long,
    val dataJson: String
)
