## What is DhCamera?
DhCamera is a camera library which is designed to make you develop camera feature easily for your app. You can take a photo which has image or text you want like a time stamp photo other than basic camera feature with DhCamera.

## How to use
### Required
- JDK 17 or higher
- AGP 8.0.2 or higher

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
- If os version is less than 10, you should allow storage permission or a photo can't be saved into the storage and can't be loaded. 
