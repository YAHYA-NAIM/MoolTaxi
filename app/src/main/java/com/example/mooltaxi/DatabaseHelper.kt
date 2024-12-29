package com.example.mooltaxi

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "user_info"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_LICENSE_TYPE = "licenseType"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_CREATED_AT = "createdAt"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_LICENSE_TYPE TEXT,
                $COLUMN_AGE TEXT,
                $COLUMN_CREATED_AT TEXT
            );
        """
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insert a new user
    fun insertUser(name: String, email: String, licenseType: String, age: String, createdAt: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_LICENSE_TYPE, licenseType)
            put(COLUMN_AGE, age)
            put(COLUMN_CREATED_AT, createdAt)
        }

        // Insert and return the result (row id or -1 if error)
        return db.insert(TABLE_NAME, null, values).also {
            db.close()
        }
    }

    // Get user by email
    fun getUserByEmail(email: String): User? {
        if (email.isEmpty()) return null

        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_NAME, COLUMN_EMAIL, COLUMN_LICENSE_TYPE, COLUMN_AGE),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        return cursor.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndex(COLUMN_NAME))
                val userEmail = it.getString(it.getColumnIndex(COLUMN_EMAIL))
                val licenseType = it.getString(it.getColumnIndex(COLUMN_LICENSE_TYPE))
                val age = it.getString(it.getColumnIndex(COLUMN_AGE))
                User(name, userEmail, age, licenseType)
            } else {
                null
            }
        }
    }
}

// User data class
data class User(
    val name: String,
    val email: String,
    val age: String,
    val licenseType: String
)
