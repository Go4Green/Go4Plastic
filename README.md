ForwardARGame Android Acceptance Test
================================

Simple Map-based Android app with various UI funcionalities and AR capabilities.

## Requirements-Implementation:

### 1. Mapbox Map
MapView functionality implemented in `MapBoxActivity.java`
### 2. Current Location
Location update & Map-update implemented using **Mapbox Location Engine** and **Mapbox Location Layer** plugin.
### 3. Custom Marker
Inserted ![Bomb Marker](https://bytebucket.org/vkyriazakos/forwardartest/raw/bc19a511db144816fd84fc4a2d9ef79ac628c13d/app/src/main/res/drawable/bomb_marker.png) marker that spawns randomly near the user.
### 4. User Profile Circle button [top-right]
Implemented using CircleImageView package `de.hdodenhof:circleimageview:2.2.0`
### 5. Inbox button bottom right of User Profile
Implemented using custom Round TextButton and a RelativeLayout.
### 6. Menu with two buttons dynamically sliding out
Implemented using custom `slide_left.xml` animation on a `RelativeLayout` using ![ON Button](https://bytebucket.org/vkyriazakos/forwardartest/raw/f203316d3f22a2b087e99017356ea70113f524f6/app/src/main/res/drawable/btn_menu1_1_small.png) and ![OFF Button](https://bytebucket.org/vkyriazakos/forwardartest/raw/f203316d3f22a2b087e99017356ea70113f524f6/app/src/main/res/drawable/btn_menu2_1_small.png) along with ![ON Nothing](https://bytebucket.org/vkyriazakos/forwardartest/raw/f203316d3f22a2b087e99017356ea70113f524f6/app/src/main/res/drawable/btn_menu1_2_small.png) and ![OFF Nothing](https://bytebucket.org/vkyriazakos/forwardartest/raw/f203316d3f22a2b087e99017356ea70113f524f6/app/src/main/res/drawable/btn_menu2_2_small.png)
as the two ImageButtons.
### 7. Tapping each button shows animating dialog with description
Implemented using **FancyAlertDialog** from [here](https://android-arsenal.com/details/1/6626 )
### 8. Map Screen Portrait-only
Done by setting `android:screenOrientation="portrait"  
android:configChanges="orientation|keyboardHidden"` on the Manifest.
### 9. Button on bottom-right of screen goes on ARCore Demo
Integrated **hello_ar_java** sample from Google found [here](https://developers.google.com/ar/develop/java/quickstart)

