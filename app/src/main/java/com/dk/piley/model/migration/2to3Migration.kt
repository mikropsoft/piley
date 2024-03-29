package com.dk.piley.model.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // add new completion times column
        db.execSQL("ALTER TABLE Task ADD COLUMN averageCompletionTimeInHours INTEGER NOT NULL DEFAULT 0")
        // add task order to pile table
        db.execSQL("ALTER TABLE tasks ADD COLUMN taskOrder TEXT")
        db.execSQL("UPDATE tasks SET taskOrder = (SELECT '[' || GROUP_CONCAT(id ORDER BY id) || ']' FROM tasks)")
        db.execSQL("COMMIT;")
    }
}