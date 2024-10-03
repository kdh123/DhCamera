package com.dhkim.dhcamera

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import com.dhkim.dhcamera.model.BackgroundImage
import com.dhkim.dhcamera.model.BackgroundText
import com.dhkim.dhcamera.camera.DhCamera
import com.dhkim.dhcamera.model.FontElement
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
            BackgroundImage.Builder()
                .imageSrc(R.drawable.ic_apartment_skyblue)
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .padding(start = 10, top = 150)
                .build(),
            BackgroundImage.Builder()
                .imageSrc("https://picsum.photos/200")
                .fillMaxSize()
                .padding(start = 50, end = 50)
                .build(),
            BackgroundImage.Builder()
                .imageSrc(R.drawable.ic_launcher_background)
                .width(64)
                .height(64)
                .align(DhCamera.BOTTOM_CENTER)
                .build(),
            BackgroundImage.Builder()
                .imageSrc(R.drawable.ic_launcher_foreground)
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .build()
        )

        val fontsIds = listOf<Int>(
            R.font.bm_dohyun,
            R.font.bm_euljiro_10,
            R.font.bm_euljiro_orae,
            R.font.bm_hanna_10,
            R.font.bm_hanna_pro,
            R.font.bm_jiro,
            R.font.bm_jua,
            R.font.bm_kirang,
            R.font.bm_yeonsun
        )

        val fonts = mutableListOf<FontElement>().apply {
            fontsIds.forEachIndexed { index, font ->
                add(
                    FontElement.Builder()
                        .text("font $index")
                        .font(font)
                        .build()
                )
            }
        }

        setContent {
            DhCameraTheme {
                DhCamera.Builder(this@MainActivity)
                    .folderName("DhKim")
                    .backgroundItems(backgroundImages)
                    .enableInputText(true)
                    .enableAddGalleryImage(true)
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