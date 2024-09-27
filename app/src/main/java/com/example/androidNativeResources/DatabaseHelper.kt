package com.example.androidNativeResources

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "FormDataDB", null, 2) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE FormData (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nome TEXT, " +
                    "email TEXT, " +
                    "comentario TEXT, " +
                    "fotoPath TEXT, " +
                    "latitude REAL, " +
                    "longitude REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE FormData ADD COLUMN latitude REAL")
            db.execSQL("ALTER TABLE FormData ADD COLUMN longitude REAL")
        }
    }

    fun insertData(nome: String, email: String, comentario: String, fotoPath: String, latitude: Double?, longitude: Double?) {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put("nome", nome)
            put("email", email)
            put("comentario", comentario)
            put("fotoPath", fotoPath)
            put("latitude", latitude)
            put("longitude", longitude)
        }
        db.insert("FormData", null, contentValues)
        db.close()
    }
}

