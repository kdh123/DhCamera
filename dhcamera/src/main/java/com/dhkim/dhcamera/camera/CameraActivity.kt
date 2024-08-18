package com.dhkim.dhcamera.camera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.dhkim.dhcamera.camera.navigation.CAMERA_MAIN_ROUTE
import com.dhkim.dhcamera.camera.navigation.cameraMainNavigation

internal class CameraActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            WindowCompat.setDecorFitsSystemWindows(window, false)

            NavHost(
                navController = navController,
                startDestination = CAMERA_MAIN_ROUTE
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
            }
        }
    }
}