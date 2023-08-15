package com.apodaca.diaryapp.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.apodaca.diaryapp.data.repository.MongoDB
import com.apodaca.diaryapp.model.Diary
import com.apodaca.diaryapp.model.Mood
import com.apodaca.diaryapp.model.RequestState
import com.apodaca.diaryapp.presentation.components.DisplayAlertDialog
import com.apodaca.diaryapp.presentation.screens.auth.AuthenticationScreen
import com.apodaca.diaryapp.presentation.screens.auth.AuthenticationViewModel
import com.apodaca.diaryapp.presentation.screens.home.HomeScreen
import com.apodaca.diaryapp.presentation.screens.home.HomeViewModel
import com.apodaca.diaryapp.presentation.screens.write.WriteScreen
import com.apodaca.diaryapp.presentation.screens.write.WriteViewModel
import com.apodaca.diaryapp.util.Constants.APP_ID
import com.apodaca.diaryapp.util.Constants.WRITE_SCREEN_ARGUMENT_KEY
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.stevdzasan.messagebar.rememberMessageBarState
import com.stevdzasan.onetap.rememberOneTapSignInState
import io.realm.kotlin.mongodb.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun SetupNavGraph(
    startDestination: String,
    navController: NavHostController,
    onDataLoaded: () -> Unit
) {

    NavHost(
        startDestination = startDestination,
        navController = navController
    ) {
        authenticationRoute(
            navigateToHome = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Authentication.route) {
                        inclusive = true
                    }
                }
            },
            onDataLoaded = onDataLoaded
        )
        homeRoute(
            navigateToWrite = {
                navController.navigate(Screen.Write.route)
            },
            navigateToWriteWithArgs = { diaryId ->
                navController.navigate(Screen.Write.passDiaryId(diaryId))
            },
            navigateToAuth = {
                navController.navigate(Screen.Authentication.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = true
                    }
                }
            },
            onDataLoaded = onDataLoaded
        )
        writeRoute(
            navigateBack = {
                navController.popBackStack()
            }
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.authenticationRoute(
    navigateToHome: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Authentication.route) {
        val viewModel: AuthenticationViewModel = viewModel()
        val authenticated by viewModel.authenticated
        val loadingState by viewModel.loadingState
        val oneTapState = rememberOneTapSignInState()
        val messageBarState = rememberMessageBarState()

        LaunchedEffect(key1 = Unit) {
            onDataLoaded()
        }

        AuthenticationScreen(
            authenticated = authenticated,
            loadingState = oneTapState.opened,
            oneTapState = oneTapState,
            messageBarState = messageBarState,
            onTokenIdReceived = { tokenId ->
                viewModel.signInWithMongoAtlas(
                    tokenId = tokenId,
                    onSuccess = {
                        messageBarState.addSuccess("Successfully signed in")
                        viewModel.setLoading(false)
                    },
                    onError = {
                        messageBarState.addError(it)
                        viewModel.setLoading(false)
                    }
                )
            },
            onDialogDismissed = { message ->
                messageBarState.addError(Exception(message))
                viewModel.setLoading(false)
            },
            navigateToHome = navigateToHome
        ) {
            oneTapState.open()
            viewModel.setLoading(true)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
fun NavGraphBuilder.homeRoute(
    navigateToWrite: () -> Unit,
    navigateToWriteWithArgs: (String) -> Unit,
    navigateToAuth: () -> Unit,
    onDataLoaded: () -> Unit
) {
    composable(route = Screen.Home.route) {
        val viewModel: HomeViewModel = viewModel()
        val diaries by viewModel.diaries
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        var signOutDialogOpened by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        LaunchedEffect(key1 = diaries) {
            if (diaries !is RequestState.Loading) {
                onDataLoaded()
            }
        }
        HomeScreen(
            diaries = diaries,
            drawerState = drawerState,
            onMenuClicked = {
                scope.launch {
                    drawerState.open()

                }
            },
            onSignOutClicked = {
                signOutDialogOpened = true
            },
            navigateToWrite = navigateToWrite,
            navigateToWriteWithArgs = navigateToWriteWithArgs,
        )

        LaunchedEffect(key1 = Unit) {
            MongoDB.configureTheRealm()
        }
        DisplayAlertDialog(title = "Sign out",
            message = "Are you sure?",
            dialogOpened = signOutDialogOpened,
            onDialogClosed = {
                signOutDialogOpened = false
            },
            onYesClicked = {
                scope.launch(Dispatchers.IO) {
                    val user = App.create(APP_ID).currentUser
                    user?.logOut().also {
                        withContext(Dispatchers.Main) {
                            navigateToAuth()
                        }
                    }

                }
            })
    }
}

@OptIn(ExperimentalPagerApi::class)
fun NavGraphBuilder.writeRoute(
    navigateBack: () -> Unit
) {

    composable(
        route = Screen.Write.route,
        arguments = listOf(navArgument(name = WRITE_SCREEN_ARGUMENT_KEY) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        })
    ) {
        val viewModel: WriteViewModel = viewModel()

        val context = LocalContext.current
        val uiState = viewModel.uiState
        val pagerState = rememberPagerState()
        val pageNumber by remember { derivedStateOf { pagerState.currentPage } }

        WriteScreen(
            uiState = uiState,
            moodName = { Mood.values()[pageNumber].name },
            pagerState = pagerState,
            onTitleChange = { viewModel.setTitle(title = it) },
            onDescriptionChange = { viewModel.setDescription(description = it) },
            onDeleteConfirmed = {
                viewModel.deleteDiary(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        navigateBack()
                    },
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
            onDateTimeUpdated = { viewModel.updateDateTime(zonedDateTime = it) },
            onBackPressed = navigateBack,
            onSaveClicked = {
                viewModel.upsertDiary(
                    diary = it.apply { mood = Mood.values()[pageNumber].name },
                    onSuccess = navigateBack,
                    onError = { message ->
                        Toast.makeText(
                            context,
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            },
        )

    }
}