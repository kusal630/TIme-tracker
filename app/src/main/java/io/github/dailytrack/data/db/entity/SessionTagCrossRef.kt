package io.github.dailytrack.data.db.entity

import androidx.room.Entity

@Entity(
    tableName = "session_tag_cross_ref",
    primaryKeys = ["sessionId", "tagId"]
)
data class SessionTagCrossRef(
    val sessionId: Long,
    val tagId: Long
)
