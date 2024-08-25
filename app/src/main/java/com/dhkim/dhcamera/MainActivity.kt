package com.dhkim.dhcamera

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.dhkim.dhcamera.camera.model.BackgroundImage
import com.dhkim.dhcamera.camera.model.BackgroundText
import com.dhkim.dhcamera.camera.DhCamera
import com.dhkim.dhcamera.camera.model.FontElement
import com.dhkim.dhcamera.ui.theme.DhCameraTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val backgroundImages = listOf(
            BackgroundText.Builder(this)
                .text("Hello World!")
                .width(300)
                .height(64)
                .textAlign(DhCamera.TEXT_CENTER)
                .align(DhCamera.TOP_CENTER)
                .padding(start = 10, top = 90)
                .showTextBackground()
                .build(),
            BackgroundText.Builder(this)
                .text("Good! \nHello")
                .height(64)
                .textAlign(DhCamera.TEXT_START)
                .textSize(36)
                .textColor(R.color.purple_200)
                .align(DhCamera.CENTER)
                .padding(start = 10, top = 50)
                .build(),
            BackgroundImage.Builder(this)
                .drawable("ic_launcher_background")
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .padding(start = 10, top = 10)
                .build(),
            BackgroundImage.Builder(this)
                .imageUrl("https://picsum.photos/200")
                .fillMaxSize()
                .padding(start = 50, end = 50)
                .build(),
            BackgroundImage.Builder(this)
                .drawable("none_mode_white")
                .width(64)
                .height(64)
                .align(DhCamera.BOTTOM_CENTER)
                .build(),
            BackgroundImage.Builder(this)
                .drawable("ic_flip_camera_android_white")
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .build(),
            BackgroundImage.Builder(this)
                .drawable("none_mode_white")
                .width(64)
                .height(64)
                .align(DhCamera.BOTTOM_END)
                .build(),
            BackgroundImage.Builder(this)
                .drawable("ic_flip_camera_android_white")
                .width(64)
                .height(64)
                .align(DhCamera.CENTER)
                .build(),
        )

        val fonts = mutableListOf<FontElement>().apply {
            repeat(10) {
                add(
                    FontElement.Builder(this@MainActivity)
                        .text("폰트 $it")
                        .font(if (it % 2 == 0) {
                            R.font.bm_font
                        } else {
                            R.font.bm_dohyun_font
                        })
                        .build()
                )
            }
        }

        setContent {
            DhCameraTheme {
                DhCamera.Builder(this@MainActivity)
                    .folderName("DhKim")
                    .backgroundItems(backgroundImages)
                    .fontElements(fonts)
                    .onPermissionDenied { permission ->
                        val deniedPermission = when (permission) {
                            Manifest.permission.CAMERA -> {
                                "카메라"
                            }

                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                                "저장 공간"
                            }

                            else -> {
                                ""
                            }
                        }
                        Toast.makeText(
                            this@MainActivity,
                            "$deniedPermission 권한을 허용해주세요",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .onCompleted(true) {
                        Intent(this@MainActivity, ImageActivity::class.java).apply {
                            putExtra("savedUrl", it)
                        }.run {
                            startActivity(this)
                        }
                    }
                    .start()

                Button(onClick = {
                    DhCamera.Builder(this@MainActivity)
                        .folderName("DhKim")
                        .backgroundItems(backgroundImages)
                        .onPermissionDenied {
                            Toast.makeText(this@MainActivity, "카메라 권한을 허용해주세요", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .onCompleted(true) {
                            Intent(this@MainActivity, ImageActivity::class.java).apply {
                                putExtra("savedUrl", it)
                            }.run {
                                startActivity(this)
                            }
                        }
                        .start()
                }) {
                    Text(text = "카메라 시작")
                }
            }
        }
    }
}