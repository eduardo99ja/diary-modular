package com.apodaca.diaryapp.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.apodaca.diaryapp.model.Diary
import com.apodaca.diaryapp.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    uiState: UiState,
    selectedDiary: Diary?,
    pagerState: PagerState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDeleteConfirmed: () -> Unit,
    onBackPressed: () -> Unit,
) {
    // Update the Mood when selecting an existing Diary
    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }
    Scaffold(
        topBar = {
            WriteTopBar(
                selectedDiary = selectedDiary,
                onDeleteConfirmed = onDeleteConfirmed,
                onBackPressed = onBackPressed
            )
        },
        content = {
            WriteContent(
                pagerState = pagerState,
                title = uiState.title,
                onTitleChanged = onTitleChange,
                description = uiState.description,
                onDescriptionChanged = onDescriptionChange,
                paddingValues = it
            )
        }
    )
}