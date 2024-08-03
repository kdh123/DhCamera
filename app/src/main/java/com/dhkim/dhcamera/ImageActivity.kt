package com.dhkim.dhcamera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage

class ImageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.getStringExtra("savedUrl") ?: ""

        setContent {
            Column {
                GlideImage(
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.FillWidth
                    ),
                    imageModel = {
                        url
                    },
                    modifier = Modifier
                )
            }
        }
    }
}