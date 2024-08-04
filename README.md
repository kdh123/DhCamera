## What is DhCamera?
DhCamera is a camera library which is designed to make you develop camera feature easily for your app. You can also take a photo which has image or text you want like a time stamp photo with this library other than basic camera feature.

## Including in your project
### Required
- JDK 17 or higher
- AGP 8.0.2 or higher

### Gradle
- Project Level
```kotlin
repositories {
    maven { url 'https://jitpack.io' }
}
```
- App Level
```kotlin
dependencies {
    implementation("com.github.kdh123:DhCamera:1.0.0-alpha03")
}
```


### AndroidManifest.xml
```xml
<!-- Declare camera, storage, Internet permissions  --> 
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission
    android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

...

<aplication>
  ...
  <!-- Declare CameraActivity --> 
  <activity android:name="com.dhkim.dhcamera.camera.CameraActivity"/>
  ...
</application>

```
## Warning
- If OS version is less than 10, you should allow storage permission or a photo can't be saved into the storage and can't be loaded.
- If OS version is 10 or higher, you don't need to allow storage permission

## How to use
You can start Camera Activity through like below code.
```kotlin
DhCamera.Builder(context)
            .backgroundItems(backgroundImages)
            .folderName("DhCamera")
            .thumbnailBackground(R.drawable.thumbnail_background)
            .prevBtnImage(R.drawable.prev_btn)
            .nextBtnImage(R.drawable.next_btn)
            .onPermissionDenied { permission ->
                when (permission) {
                    Manifest.permission.CAMERA -> "camera"
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE -> "storage"
                    else -> ""
                }.let {
                    Toast.makeText(this@MainActivity, "Please allow $it permission to use camera", Toast.LENGTH_SHORT).show()
                }
            }
            .onCompleted(isFinishCamera = true) {
                Intent(this@MainActivity, ImageActivity::class.java).apply {
                    putExtra("savedUrl", it)
                }.run {
                    startActivity(this)
                }
            }
            .start()
```
### Properties
- folderName : the folder's name for saving a photo
- backgroundItems : If you want to take a photo which has text or image you want, you can use backgroundItems property. The backgroundItems' type is `List<BackgroundItem>`.
    - Text
      ```kotlin
      BackgroundText.Builder(context)
                .text("Hello World!")
                .width(300)
                .height(64)
                .textAlign(DhCamera.TEXT_CENTER)
                .align(DhCamera.TOP_CENTER)
                .font(R.font.my_font)
                .padding(start = 10, top = 10)
                .showTextBackground()
                .build()
      ```
    - Image
      - If you use drawable in resource, you can use drawable property. You should put the name of drawable resource.
      - If you use the url of image, you can use imageUrl property.
      - If you use both drawable and imageUrl properties, the drawable will be loaded.
      ```kotlin
      BackgroundImage.Builder(context)
                .drawable("my_image")
                //.imageUrl("https://picsum.photos/200")
                //.fillMaxSize()
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .padding(start = 10, top = 10)
                .build()
      ```
    you can create backgroundItems value like below.
    ```kotlin
    val backgroundItems = listOf(
            BackgroundText.Builder(context)
                .text("Hello World!")
                .height(64)
                .textAlign(DhCamera.TEXT_START)
                .font(R.font.my_font)
                .textSize(36)
                .textColor(R.color.purple_200)
                .align(DhCamera.CENTER)
                .build(),
            BackgroundImage.Builder(context)
                .drawable("my_image")
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .build()
    )
    ``` 
      
- thumbnailBackground : If you use backgroundItems of properties, a small screen image list supposed to be rendered on a screen which has text or image will be shown. The tumbnailBackground of properties is the background image of item of screen list.
- onCompleted : It is next process after saving a photo. If you want to finish Camera Activity after running next process, set the isFinishCamera property to true like below code. (If not, set to false)
  ```kotlin
  onCompleted(isFinishCamera = true) {
                // todo
            }
  ```
- onPermissionDenied: You can deal with the case when required permission got denied.
- prevBtnImage, nextBtnImage : You can change image of retake button and save button. (retake button is left button and save button is right button in below image)
  
  <img width="277" alt="image" src="https://github.com/user-attachments/assets/4735195a-797c-4081-8a54-4695d664fa7e">

## Use Cases
- [나의이야기](https://play.google.com/store/apps/details?id=com.dhkim.timecapsule) : The app that can make time capsule and you can check it in the future.

## License
```
Copyright 2024 kdh123 (Dohyun Kim)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

