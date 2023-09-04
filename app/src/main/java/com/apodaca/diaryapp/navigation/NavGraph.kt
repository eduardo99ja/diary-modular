package com.apodaca.diaryapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.apodaca.auth.navigation.authenticationRoute
import com.apodaca.home.navigation.homeRoute
import com.apodaca.util.Screen
import com.apodaca.write.navigation.writeRoute

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




