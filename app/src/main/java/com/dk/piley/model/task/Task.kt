package com.dk.piley.model.task

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.dk.piley.model.pile.Pile
import java.time.Instant

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Pile::class,
            parentColumns = arrayOf("pileId"),
            childColumns = arrayOf("pileId"),
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val pileId: Long = 1,
    val description: String = "",
    val createdAt: Instant = Instant.now(),
    val modifiedAt: Instant = Instant.now(),
    val completionTimes: List<Instant> = emptyList(),
    val reminder: Instant? = null,
    val isRecurring: Boolean = false,
    val recurringTimeRange: RecurringTimeRange = RecurringTimeRange.DAILY,
    val recurringFrequency: Int = 1,
    val status: TaskStatus = TaskStatus.DEFAULT,
)
