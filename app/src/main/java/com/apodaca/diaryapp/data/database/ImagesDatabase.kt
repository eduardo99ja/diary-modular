package com.apodaca.diaryapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.apodaca.diaryapp.data.database.entity.ImageToUpload


@Database(
    entities = [ImageToUpload::class],
    version = 2,
    exportSchema = false
)
abstract class ImagesDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImageToUploadDao
}