package com.dhkim.dhcamera.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle

internal class CameraActivity : ComponentActivity() {

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val sideEffect = remember {
                viewModel.sideEffect
            }

            CameraScreen(
                uiState = uiState,
                sideEffect = sideEffect,
                onChangeBackgroundImage = viewModel::onChangeBackgroundImage,
                onSavingPhoto = viewModel::onSavingPhoto,
                onSavedPhoto = viewModel::onSavedPhoto,
                onTakePhoto = viewModel::onTakePhoto,
                onResetPhoto = viewModel::onResetPhoto,
                onNext = DhCamera.getOnCompleted(),
                onPermissionDenied = DhCamera.getOnPermissionDenied(),
                onBack = {
                    onBackPressed()
                }
            )
        }
    }
}