package com.dhkim.dhcamera.camera

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dhkim.dhcamera.navigation.CAMERA_ROUTE
import com.dhkim.dhcamera.navigation.cameraMainNavigation
import com.dhkim.dhcamera.navigation.inputTextNavigation

internal class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            val navController = rememberNavController()

            WindowCompat.setDecorFitsSystemWindows(window, false)

            NavHost(
                navController = navController,
                startDestination = CAMERA_ROUTE
            ) {
                cameraMainNavigation(
                    navController = navController,
                    onNext = {
                        DhCamera.getOnCompleted()(it)
                        if (DhCamera.isFinishCamera()) {
                            finish()
                        }
                    },
                    onPermissionDenied = DhCamera.getOnPermissionDenied(),
                    onBack = {
                        onBackPressed()
                    }
                )

                inputTextNavigation(
                    onCompleted = { inputTextProperty ->
                        navController.navigateUp()
                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("inputText", inputTextProperty)
                    },
                    onBack = navController::navigateUp
                )
            }
        }
    }
}