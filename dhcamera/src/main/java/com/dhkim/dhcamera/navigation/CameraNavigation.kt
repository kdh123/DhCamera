package com.dhkim.dhcamera.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.dhkim.dhcamera.camera.CameraScreen
import com.dhkim.dhcamera.camera.CameraViewModel
import com.dhkim.dhcamera.camera.Permission
import com.dhkim.dhcamera.camera.SavedUrl
import com.dhkim.dhcamera.model.FontAlign
import com.dhkim.dhcamera.inputText.InputTextScreen
import com.dhkim.dhcamera.inputText.InputTextViewModel
import kotlinx.serialization.Serializable

const val CAMERA_ROUTE = "camera"

fun NavGraphBuilder.cameraMainNavigation(
    navController: NavHostController,
    onNext: (SavedUrl) -> Unit,
    onPermissionDenied: (Permission) -> Unit,
    onBack: () -> Unit
) {
    composable(CAMERA_ROUTE) {
        val inputText = it.savedStateHandle.get<InputTextRoute>("inputText")
        val viewModel = viewModel<CameraViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val sideEffect = remember {
            viewModel.sideEffect
        }

        CameraScreen(
            inputText = inputText,
            uiState = uiState,
            sideEffect = sideEffect,
            onAction = remember(viewModel) {
                viewModel::onCameraAction
            },
            onNext = onNext,
            onPermissionDenied = onPermissionDenied,
            onNavigateToInputText = navController::navigateToInputText,
            onInitInputText = {
                it.savedStateHandle["inputText"] = null
            },
            onBack = onBack
        )
    }
}

fun NavGraphBuilder.inputTextNavigation(
    onCompleted: (InputTextRoute) -> Unit,
    onBack: () -> Unit
) {
    composable<InputTextRoute> {
        val currentFontProperties = it.toRoute<InputTextRoute>()
        val viewModel = viewModel<InputTextViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val sideEffect = remember {
            viewModel.sideEffect
        }

        InputTextScreen(
            currentFontProperties = currentFontProperties,
            uiState = uiState,
            sideEffect = sideEffect,
            onAction = viewModel::onAction,
            onCompleted = onCompleted,
            onBack = onBack
        )
    }
}

fun NavController.navigateToInputText(
    data: InputTextRoute?
) {
    if (data != null) {
        navigate(data)
    } else {
        navigate(InputTextRoute())
    }
}

@Serializable
data class InputTextRoute(
    val id: String = "",
    val text: String = "",
    val font: Int = 0,
    val color: Int = 0,
    val alignment: FontAlign = FontAlign.Center
): java.io.Serializable