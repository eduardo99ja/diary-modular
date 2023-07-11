package com.apodaca.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.apodaca.diaryapp.model.Diary
import com.google.accompanist.pager.ExperimentalPagerApi

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    selectedDiary: Diary?,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed
            )
        },
        content = {


        }
    )
}