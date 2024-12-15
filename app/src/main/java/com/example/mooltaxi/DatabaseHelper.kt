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
    fun insertUser(name: String, email: String, licenseType: String, age: String, createdAt: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_LICENSE_TYPE, licenseType)
            put(COLUMN_AGE, age)
            put(COLUMN_CREATED_AT, createdAt)
        }

        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getUserByEmail(email: String): User? {
        val db = readableDatabase
        val cursor: Cursor = db.query(
            TABLE_NAME, // Table name
            arrayOf(COLUMN_NAME, COLUMN_EMAIL, COLUMN_LICENSE_TYPE, COLUMN_AGE), // Columns to return
            "$COLUMN_EMAIL = ?", // Where clause
            arrayOf(email), // Arguments for WHERE clause
            null, null, null // Group by, having, and order by (not used)
        )

        if (cursor != null && cursor.moveToFirst()) {
            // Check if all the columns exist before accessing
            val nameIndex = cursor.getColumnIndex(COLUMN_NAME)
            val emailIndex = cursor.getColumnIndex(COLUMN_EMAIL)
            val licenseTypeIndex = cursor.getColumnIndex(COLUMN_LICENSE_TYPE)
            val ageIndex = cursor.getColumnIndex(COLUMN_AGE)

            // If any column index is -1, it means the column doesn't exist in the Cursor
            if (nameIndex == -1 || emailIndex == -1 || licenseTypeIndex == -1 || ageIndex == -1) {
                cursor.close()
                return null
            }

            // Retrieve values only if indices are valid
            val name = cursor.getString(nameIndex)
            val userEmail = cursor.getString(emailIndex)
            val licenseType = cursor.getString(licenseTypeIndex)
            val age = cursor.getString(ageIndex)
            cursor.close()

            return User(name, userEmail, age, licenseType) // Return User object
        }

        cursor.close()
        return null // Return null if user is not found
    }
}
data class User(
    val name: String,
    val email: String,
    val age: String,
    val licenseType: String
)
