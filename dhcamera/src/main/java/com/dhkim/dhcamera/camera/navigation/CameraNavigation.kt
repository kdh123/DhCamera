package com.dhkim.dhcamera.camera.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.dhkim.dhcamera.camera.CameraScreen
import com.dhkim.dhcamera.camera.CameraViewModel
import com.dhkim.dhcamera.camera.DhCamera
import com.dhkim.dhcamera.camera.InputTextScreen
import com.dhkim.dhcamera.camera.Permission
import com.dhkim.dhcamera.camera.SavedUrl
import kotlinx.collections.immutable.toImmutableList

const val CAMERA_MAIN_ROUTE = "camera_main"
const val CAMERA_ROUTE = "camera"
const val INPUT_TEXT_ROUTE = "input_text"

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(navController: NavController): T {
    val navGraphRoute = destination.parent?.route ?: return viewModel()
    val entry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }

    return viewModel(entry)
}

fun NavGraphBuilder.cameraMainNavigation(
    navController: NavHostController,
    onNext: (SavedUrl) -> Unit,
    onPermissionDenied: (Permission) -> Unit,
    onBack: () -> Unit
) {
    navigation(
        startDestination = CAMERA_ROUTE,
        route = CAMERA_MAIN_ROUTE
    ) {
        composable(CAMERA_ROUTE) {
            val viewModel = it.sharedViewModel<CameraViewModel>(navController = navController)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val sideEffect = remember {
                viewModel.sideEffect
            }

            CameraScreen(
                uiState = uiState,
                sideEffect = sideEffect,
                onAction = viewModel::onAction,
                onNext = onNext,
                onPermissionDenied = onPermissionDenied,
                onNavigateToInputText = navController::navigateToInputText,
                onBack = onBack
            )
        }

        composable(INPUT_TEXT_ROUTE) {
            val viewModel = it.sharedViewModel<CameraViewModel>(navController = navController)
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            InputTextScreen(
                uiState = uiState,
                onAction = viewModel::onAction,
                fonts = DhCamera.getFontElements().toImmutableList(),
                onBack = navController::navigateUp
            )
        }
    }
}

fun NavController.navigateToInputText() {
    navigate(INPUT_TEXT_ROUTE)
}