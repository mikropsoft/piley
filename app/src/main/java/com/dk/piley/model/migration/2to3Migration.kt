package com.dk.piley.model.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // add new completion times column
        db.execSQL("ALTER TABLE 'Task' ADD COLUMN 'averageCompletionTimeInHours' INTEGER NOT NULL DEFAULT 0")

        // Add a new column for the index of the task position within
        db.execSQL("ALTER TABLE Task ADD COLUMN positionInPile INTEGER")
        // Create a temporary table to hold the updated index values
        db.execSQL("CREATE TEMPORARY TABLE temp_table AS SELECT pileId, createdAt, ROW_NUMBER() OVER (PARTITION BY pileId ORDER BY createdAt) - 1 AS index_value FROM Task")
        // Update the index column based on createdAt for each foreign key
        db.execSQL("UPDATE Task SET positionInPile = (SELECT index_value FROM temp_table WHERE temp_table.pileId = Task.pileId AND temp_table.createdAt = Task.createdAt)")
        // Drop the temporary table
        db.execSQL("DROP TABLE temp_table")
    }
}