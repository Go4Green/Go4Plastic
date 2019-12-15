package com.realtwin.goforplastic;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.transition.Slide;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.building.BuildingPlugin;
import com.realtwin.goforplastic.fragments.ProfileFragment;
import com.realtwin.goforplastic.mapfilters.FilterCategory;
import com.realtwin.goforplastic.mapfilters.FilterItemsManager;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.Icon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;
import static com.realtwin.goforplastic.Constants.TOKEN_PREF;

public class MapboxActivity extends AppCompatActivity implements OnMapReadyCallback, PermissionsListener {


    private PermissionsManager permissionsManager;
    private MapboxMap mapBoxMap;
    private MapView mapView;

    private int tokens;

    private boolean sillyButton1 = true, sillyButton2 = true;// silly booleans for image change

    //Map icons filter staff
    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();

    private FilterItemsManager filterItemsManager;
    private HashMap<FilterCategory, Boolean> filtersState = new HashMap<FilterCategory, Boolean>(){{
        put(FilterCategory.ACTION_POINT, true);
        put(FilterCategory.TRASH_BIN, true);
        put(FilterCategory.OFFER, true);
    }};

    private HashMap<FilterCategory, String> markerNames = new HashMap<FilterCategory, String>(){{
        put(FilterCategory.ACTION_POINT, "map_icons_action");
        put(FilterCategory.TRASH_BIN, "map_icons_recycling_bin");
        put(FilterCategory.OFFER, "map_icons_offers");
    }};


    // --Map icons filter staff

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoidmt5cmlhemFrb3MiLCJhIjoiY2pmMnJwZzdtMHJ0NDJ3cTdzem16NXVjNyJ9.EX8pu58PGuEn9E6MWPxONw");
        setContentView(R.layout.activity_mapbox);

        // Make menu invisible
        RelativeLayout menu = findViewById(R.id.filter_menu);
        menu.setVisibility(View.GONE);

        // Initialize Filters manager
        filterItemsManager = new FilterItemsManager(getApplicationContext());


        // Initialize Mapview
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(this);

        //Add on click listeners
        addFilterListeners();


        CircleImageView profileButton = findViewById(R.id.btn_profile_image);
        profileButton.setOnClickListener(v -> {
            //showFancyDialog("This circular button was developed to put a persisting face in your screen.");
            openProfileScreen();
        });

        tokens = PreferenceManager.getDefaultSharedPreferences(this).getInt(TOKEN_PREF, 5);

        Button tokenIcon = findViewById(R.id.tokenIcon);
        tokenIcon.setText(String.valueOf(tokens));
        tokenIcon.setOnClickListener(v -> {
            Button tokenButton = (Button)v;
            showFancyDialog("You have " + tokenButton.getText()+" eco-tokens. Keep it up!!!");
        });

        Button arButton = findViewById(R.id.btn_AR);
        arButton.setOnClickListener(v -> {
            Button inbox = (Button)v;
            startARActivity();

        });

//        Button dlButton = findViewById(R.id.btn_DL);
//        dlButton.setOnClickListener(v -> {
//            startDetectorActivity();
//        });

    }

    private void addFilterListeners(){
        //Add on click listeners
        Button menuButton1 = findViewById(R.id.btn_filter);
        menuButton1.setOnClickListener(v -> toggleCategory(v, FilterCategory.ACTION_POINT));

        Button menuButton2 = findViewById(R.id.btn_filter2);
        menuButton2.setOnClickListener(v -> toggleCategory(v, FilterCategory.OFFER));
        Button menuButton3 = findViewById(R.id.btn_filter3);
        menuButton3.setOnClickListener(v -> toggleCategory(v, FilterCategory.TRASH_BIN));
    }

    private void toggleCategory(View v, FilterCategory category){
        if(filtersState.get(category)){
            Drawable d = v.getBackground();
            d.setAlpha(125);
            v.setBackground(d);
        }
        else{
            Drawable d = v.getBackground();
            d.setAlpha(255);
            v.setBackground(d);
        }
        filtersState.put(category, !filtersState.get(category));
        updateMarkers();
    }
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        MapboxActivity.this.mapBoxMap = mapboxMap;
        String styleJson = getStyleJson();
        mapboxMap.setStyle(new Style.Builder().fromJson(styleJson),
                style -> {
                    symbolManager = new SymbolManager(mapView, mapboxMap, style);
                    symbolManager.setIconAllowOverlap(true);
                    symbolManager.setIconTranslate(new Float[]{0f, 10f});
                    symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);

                    markerNames.forEach((markerCategory, markerName)-> {
                        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                                getResources().getIdentifier(markerName, "drawable", getPackageName()));
                        style.addImage(markerName,bm);
                    });
                    setupLocationButton();
                    enableLocationComponent(style);
                    updateMarkers();
                    BuildingPlugin buildingPlugin = new BuildingPlugin(mapView, mapBoxMap, style);
                    buildingPlugin.setVisibility(true);
                    buildingPlugin.setMinZoomLevel(14);
                    buildingPlugin.setOpacity(0.5f);
                });

        //Default Dark style
//        mapboxMap.setStyle(Style.DARK, style -> {
//                    mapStyle = style;
//                    enableLocationComponent(style);
//                    updateMarkers();
//                });


        // Also slide in menu button [sloppy]
        RelativeLayout menu = findViewById(R.id.filter_menu);
        menu.setVisibility(View.GONE);
        android.view.animation.Animation translate = AnimationUtils.loadAnimation(MapboxActivity.this, R.anim.slide_left);
        menu.startAnimation(translate);
        menu.setVisibility(View.VISIBLE);

        // setup Tap interaction
        mapboxMap.addOnMapLongClickListener(point -> {

            new FancyAlertDialog.Builder(MapboxActivity.this)
                    .setTitle("You found a Bin?")
                    .setBackgroundColor(Color.parseColor("#C600D47F"))  //Don't pass R.color.colorvalue
                    .setMessage("This will add a bin marker visible to everyone")
                    .setNegativeBtnText("Sorry mistake.")
                    .setPositiveBtnBackground(Color.parseColor("#1F8B25"))  //Don't pass R.color.colorvalue
                    .setPositiveBtnText("Sure!")
                    .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                    .setAnimation(com.shashank.sony.fancydialoglib.Animation.POP)
                    .isCancellable(true)
                    .setIcon(R.drawable.map_icons_recycling_bin, Icon.Visible)
                    .OnPositiveClicked(() -> {
                        //ClearMarkers();
                        AddMarker(FilterCategory.TRASH_BIN, point);
                        //AddMarker(FilterCategory.ACTION_POINT, point);
                        //AddMarker(FilterCategory.OFFER, point);
                        updateMarkers();
                    })
                    .OnNegativeClicked(() -> Toast.makeText(getApplicationContext(), "Too bad", Toast.LENGTH_SHORT).show())
                    .build();
            //Clear markers if
            //ClearMarkers();
            AddMarker(FilterCategory.TRASH_BIN, point);
            //AddMarker(FilterCategory.ACTION_POINT, point);
            //AddMarker(FilterCategory.OFFER, point);
            updateMarkers();
            return false;
        });
    }

    private String getStyleJson(){
        InputStream is = getResources().openRawResource(R.raw.style);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writer.toString();
    }

    private void setupLocationButton(){
        FloatingActionButton FAB = findViewById(R.id.myLocationButton);
        FAB.setOnClickListener(v -> {
            if(mapBoxMap.getLocationComponent().getLastKnownLocation() != null) { // Check to ensure coordinates aren't null, probably a better way of doing this...
                mapBoxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapBoxMap.getLocationComponent()
                        .getLastKnownLocation().getLatitude(),mapBoxMap.getLocationComponent().getLastKnownLocation().getLongitude()),
                        19));
            }
        });
    }

    //TEST user location new
    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapBoxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapBoxMap.getStyle(style -> enableLocationComponent(style));
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // End of TEST




    // Starts the AR activity
    private void startARActivity() {
        Intent i=new Intent(this, ARActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivityForResult(i, Constants.TOKEN_REQUEST);
        //startActivity(i);
    }

//    // Starts the Detector activity
//    private void startDetectorActivity() {
//        Intent i=new Intent(this, DetectorActivity.class);
//        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(i);
//    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.TOKEN_REQUEST && resultCode == RESULT_OK){
            AddToken();
        }
    }

    private void AddToken(){
        tokens++;
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(TOKEN_PREF, tokens).apply();
        Button tokenIcon = findViewById(R.id.tokenIcon);
        tokenIcon.setText(String.valueOf(tokens));

        LocationComponent locationComponent = mapBoxMap.getLocationComponent();
        if(locationComponent != null){
            Location loc = locationComponent.getLastKnownLocation();
            LatLng point = new LatLng(loc.getLatitude(), loc.getLongitude());
            AddMarker(FilterCategory.ACTION_POINT, point);
//            ValueAnimator widthAnimator = ValueAnimator.ofFloat(2.0f, 0.5f);
//            widthAnimator.addUpdateListener(animation -> {
//                float animatedValue = (float) animation.getAnimatedValue();
//                symbolManager.delete(symbol);
//                symbol.setIconSize(animatedValue);
//                symbol.setIconRotate( 5 * animatedValue);
//                symbolManager.create(new SymbolOptions(symbol))
//            });
//            widthAnimator.addListener(new AnimatorListenerAdapter()
//            {
//                @Override
//                public void onAnimationEnd(Animator animation)
//                {
//                    symbol.setIconSize(0.5f);
//                    symbol.setIconRotate(0f);
//                }
//            });
//            widthAnimator.setDuration( 1000).start();
        }

    }

    private Symbol AddMarker(FilterCategory category, LatLng point){
        filterItemsManager.AddMarker(category, point);
        Symbol symbol = null;
        if(symbolManager!= null){
            symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(point)
                    .withIconImage(markerNames.get(category))
                    .withIconSize(0.5f));
            symbols.add(symbol);
        }
        return symbol;
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        // mapView
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation? [prolly not needed]
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("Location Permissions")
                        .setMessage("Provide location permissions")
                        .setPositiveButton("OK", (dialogInterface, i) -> {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(MapboxActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    MY_PERMISSIONS_REQUEST_LOCATION);
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }




    private void setCameraPosition(Location location) {
        mapBoxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 19));
    }




    void showFancyDialog(String description)
    {
        new FancyAlertDialog.Builder(this)
                .setTitle("Button Description")
                .setBackgroundColor(Color.parseColor("#30AF4F"))  //Don't pass R.color.colorvalue
                .setMessage(description)
                .setNegativeBtnText("Not Impressed")
                .setPositiveBtnBackground(Color.parseColor("#FF20D110"))  //Don't pass R.color.colorvalue
                .setPositiveBtnText("OK Great!")
                .setNegativeBtnBackground(Color.parseColor("#FFA9A7A8"))  //Don't pass R.color.colorvalue
                .setAnimation(com.shashank.sony.fancydialoglib.Animation.POP)
                .isCancellable(true)
                .setIcon(R.drawable.bomb_marker, Icon.Visible)
                .OnPositiveClicked(() -> Toast.makeText(getApplicationContext(),"Good!",Toast.LENGTH_SHORT).show())
                .OnNegativeClicked(() -> Toast.makeText(getApplicationContext(),"Too bad",Toast.LENGTH_SHORT).show())
                .build();
    }

    // Add bomb marker
    private void updateMarkers() {
        if(symbolManager == null){
            return;
        }
        symbolManager.delete(symbols);
        symbols.clear();
    /* Source: A data source specifies the geographic coordinate where the image marker gets placed. */
        for(HashMap.Entry<FilterCategory, Boolean>kvp : filtersState.entrySet()){
            Log.d("MapboxActivity", kvp.getKey() + " , " + kvp.getValue());
            if(kvp.getValue()){
                ArrayList<LatLng> categoryMarkers = filterItemsManager.GetMarkers(kvp.getKey());
                if(categoryMarkers!= null){
                    List<SymbolOptions> options = new ArrayList<>();

                    for(LatLng location : categoryMarkers){
                        options.add(new SymbolOptions()
                                .withLatLng(location)
                                .withIconImage(markerNames.get(kvp.getKey()))
                                .withIconSize(0.5f));
//                        features.add(Feature.fromGeometry(Point.fromLngLat(location.getLongitude(), location.getLatitude())));
                    }
                    List<Symbol> curSymbols = symbolManager.create(options);
                    symbols.addAll(curSymbols);

//                    FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
//                    AddLayersOfType(kvp.getKey(), featureCollection);
                }
            }
        }
    }

//    private void AddLayersOfType(FilterCategory filterCategory, FeatureCollection featureCollection){
//        GeoJsonSource source = mapStyle.getSourceAs(markerSources.get(filterCategory));
//        if(source != null){
//            source.setGeoJson(featureCollection);
//            return;
//        }
//        source = new GeoJsonSource(markerSources.get(filterCategory), featureCollection);
//        mapStyle.addSource(source);
//        /* Style layer: A style layer ties together the source and image and specifies how they are displayed on the map. */
//        SymbolLayer markerStyleLayer = new SymbolLayer(markerStyleLayers.get(filterCategory), markerSources.get(filterCategory))
//                .withProperties(
//                        PropertyFactory.visibility(Property.VISIBLE),
//                        PropertyFactory.iconAllowOverlap(true),
//                        PropertyFactory.iconImage(markerNames.get(filterCategory)),
//                        PropertyFactory.iconIgnorePlacement(true),
//                        PropertyFactory.iconOffset(new Float[] {0f, -9f})
//                );
//        mapStyle.addLayer(markerStyleLayer);
//    }

    //////////////////////////////////////////////////////////////////////////////////
    // Stats screen
    ProfileFragment profileScreen;

    private void openProfileScreen() {

        if (profileScreen == null) {
            profileScreen = new ProfileFragment();

            Transition enter = new Slide(Gravity.START);
            enter.setInterpolator(new AccelerateDecelerateInterpolator());
            profileScreen.setEnterTransition(enter);

            Transition exit = new Slide(Gravity.END);
            profileScreen.setExitTransition(exit);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.supercontainer, profileScreen, ProfileFragment.TAG)
                .commit();
    }

    public void exitProfile() {
        getSupportFragmentManager().beginTransaction()
                .remove(profileScreen)
                .commit();
    }
}