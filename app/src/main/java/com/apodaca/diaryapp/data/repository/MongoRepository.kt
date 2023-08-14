package com.apodaca.diaryapp.data.repository

import com.apodaca.diaryapp.model.Diary
import com.apodaca.diaryapp.model.RequestState
import kotlinx.coroutines.flow.Flow
import org.mongodb.kbson.ObjectId
import java.time.LocalDate

typealias Diaries = RequestState<Map<LocalDate, List<Diary>>>
interface MongoRepository {
    fun configureTheRealm()

    fun getAllDiaries(): Flow<Diaries>

    fun getSelectedDiary(diaryId: ObjectId): RequestState<Diary>

    suspend fun insertDiary(diary: Diary): RequestState<Diary>
}