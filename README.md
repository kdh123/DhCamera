## What is DhCamera?
DhCamera is a camera library which is designed to help you develop camera feature easily for your app. And not only can you take a photo with an image or text, such as a timestamp, but you can also insert customized text or images from the gallery into a photo other than basic camera features using this library.
> <img width="250" alt="image" src="https://github.com/user-attachments/assets/957fc12e-bcb3-4dd9-8c9a-5b67f97836f9">


## Required
- JDK 17 or higher
- AGP 8.0.2 or higher
- Kotlin version 1.9.0 or higher
  
## Including in your project
### Gradle
- Project Level
```kotlin
repositories {
    maven { url 'https://jitpack.io' }
}
```
- Module Level
```kotlin
dependencies {
    implementation("com.github.kdh123:DhCamera:1.0.0-alpha05")
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
- If OS version is less than 10, you should allow storage permission or a photo can't be saved into the storage and rendered on the screen.

## How to use
You can start Camera Activity through like below code.
```kotlin
DhCamera.Builder(context)
            .folderName("DhCamera")
            .backgroundItems(backgroundImages)
            .thumbnailBackground(R.drawable.thumbnail_background)
            .enableInputText(true)
            .fontElements(fonts)
            .enableAddGalleryImage(true)
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
      - The unit of width and height is dp.
      ```kotlin
      BackgroundText.Builder(context)
                .text("Hello World!")
                .width(300)
                .height(64)
                .textSize(36)
                .textAlign(DhCamera.TEXT_CENTER)
                .align(DhCamera.TOP_CENTER)
                .font(R.font.my_font)
                .padding(start = 10, top = 10)
                .showTextBackground()
                .build()
      ```
    - Image
      - The unit of width and height is dp.
      - You can input the image resource(url, drawable resource or something) into imageSrc property.  
      ```kotlin
      BackgroundImage.Builder()
                //.imageSrc(R.drawable.my_image)
                .imageSrc("https://picsum.photos/200")
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
            BackgroundImage.Builder()
                .imageSrc(R.drawable.my_image)
                .width(64)
                .height(64)
                .align(DhCamera.TOP_CENTER)
                .build()
    )
    ``` 
      
- thumbnailBackground : If you use backgroundItems of properties, a small screen image list supposed to be rendered on a screen which has text or image will be shown. The tumbnailBackground of properties is the background image of item of screen list.
- enableInputText, enableAddGalleryImage : If you set these properties to true, you can insert text customized or image from gallery into a photo taken like below.
> <img width="250" alt="image" src="https://github.com/user-attachments/assets/64fd58b5-ded8-484f-816d-32907f5c50af">   <img width="250" alt="image" src="https://github.com/user-attachments/assets/ba92347f-2b64-438d-b1fd-0ef36701ddf3">
- fontElements : The font list in below image for customized text. If you don't use this property or fontElements value is empty, font list is not shown.
> <img width="250" alt="image" src="https://github.com/user-attachments/assets/5764a071-7a94-487e-9301-5ae10596eb52">
  &nbsp;You can create fontElements value like below.
 ```kotlin
  val fontsIds = listOf<Int>(
          R.font.font1,
          R.font.font2,
          R.font.font3,
          R.font.font4,
          R.font.font5
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
  ```

- onCompleted : It is next process after saving a photo. If you want to finish Camera Activity after running next process, set the isFinishCamera property to true like below code. (If not, set to false)
  - onCompleted delivers 'savedUri' which is the uri of image saved.
  ```kotlin
  onCompleted(isFinishCamera = true) { savedUri ->
                // todo
            }
- onPermissionDenied: You can handle the case when required permission got denied.
- prevBtnImage, nextBtnImage : You can change image of retake button and save button. (retake button is left button and save button is right button in below image)
> <img width="250" alt="image" src="https://github.com/user-attachments/assets/c251a5d7-c5ba-4f68-98ed-c32d06d52819">

## Use Cases
- [나의이야기](https://play.google.com/store/apps/details?id=com.dhkim.timecapsule) : The app that can make time capsule and you can check it in the future.
- [펫메모리](https://play.google.com/store/apps/details?id=com.dohyun.petmemory) : The app that can make a memory that you've been together with your pets.

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

