package com.gtp.hunter.wms.fragment;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.gtp.hunter.R;
import com.gtp.hunter.util.ImageUtil;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.geometry.ProjectedMeters;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Projection;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;

import java.util.Objects;

public abstract class LocationDocumentFragment extends DocumentFragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private static final String ID_FORKLIFT_ICON = "forklift_icon-id";
    private static final String ID_GEOJSON_SOURCE = "source-id";
    private static final String ID_SYMBOL_LAYER = "layer-id";
    private static final String ID_IMAGE_SOURCE = "image_source-id";
    private static final String ID_IMAGE_LAYER = "image_layer-id";

    private static final int MAP_UPDATE_INTERVAL = 500;

    private long lastMapUpdateTS = SystemClock.elapsedRealtime();
    private Drawable iconDrawable;
    private int iconResource;

    protected ImageView mapImg;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private GeoJsonSource geoJsonSource;
    private LatLng currentPosition = new LatLng(0, 0);
    private ValueAnimator animator;


    private final ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    LatLng animatedPosition = (LatLng) valueAnimator.getAnimatedValue();

                    geoJsonSource.setGeoJson(Point.fromLngLat(animatedPosition.getLongitude(), animatedPosition.getLatitude()));
                }
            };

    // Class is used to interpolate the marker animation.
    private static final TypeEvaluator<LatLng> latLngEvaluator = new TypeEvaluator<LatLng>() {

        private final LatLng latLng = new LatLng();

        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude()
                    + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude()
                    + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    };

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
// When the user clicks on the map, we want to animate the marker to that
// location.
        if (animator != null && animator.isStarted()) {
            currentPosition = (LatLng) animator.getAnimatedValue();
            animator.cancel();
        }

        animator = ObjectAnimator
                .ofObject(latLngEvaluator, currentPosition, point)
                .setDuration(MAP_UPDATE_INTERVAL);
        animator.addUpdateListener(animatorUpdateListener);
        animator.start();

        currentPosition = point;
        return true;
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        geoJsonSource = new GeoJsonSource(ID_GEOJSON_SOURCE,
                Feature.fromGeometry(Point.fromLngLat(0, 0)));
        mapboxMap.setStyle(Style.DARK, style -> {
            double width = ImageUtil.MAP_WIDTH / 2;
            double height = ImageUtil.MAP_HEIGHT / 2;
            Projection mapProjection = mapboxMap.getProjection();
            // Set the latitude and longitude values for the image's four corners
            LatLngQuad quad = new LatLngQuad(
                    mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(height, -width)),
                    mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(height, width)),
                    mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(-height, width)),
                    mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(-height, -width))
            );
            ImageSource warehouseMapSource = new ImageSource(ID_IMAGE_SOURCE, quad, R.drawable.map_macaiba);
            LatLng nwCorner = mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(ImageUtil.MAP_HEIGHT, ImageUtil.MAP_WIDTH));
            LatLng seCorner = mapProjection.getLatLngForProjectedMeters(new ProjectedMeters(-ImageUtil.MAP_HEIGHT, -ImageUtil.MAP_WIDTH));
            LatLngBounds bounds = LatLngBounds.from(nwCorner.getLatitude(), nwCorner.getLongitude(), seCorner.getLatitude(), seCorner.getLongitude());

            warehouseMapSource.setVolatile(false);
            // Add an ImageSource to the map
            style.addSource(warehouseMapSource);

            // Create a raster layer and use the imageSource's ID as the layer's data. Then add a RasterLayer to the map.
            style.addLayer(new RasterLayer(ID_IMAGE_LAYER, ID_IMAGE_SOURCE));

            style.addImage(ID_FORKLIFT_ICON, BitmapFactory.decodeResource(getResources(), iconResource), true);

            style.addSource(geoJsonSource);

            style.addLayer(new SymbolLayer(ID_SYMBOL_LAYER, ID_GEOJSON_SOURCE)
                    .withProperties(
//                            PropertyFactory.iconSize(0.3f),
                            PropertyFactory.iconKeepUpright(true),
                            PropertyFactory.iconImage(ID_FORKLIFT_ICON),
                            PropertyFactory.iconIgnorePlacement(true),
                            PropertyFactory.iconAllowOverlap(true)
                            , PropertyFactory.iconAnchor(Property.ICON_ANCHOR_CENTER)
                            , PropertyFactory.iconHaloWidth(1f)
                            , PropertyFactory.iconColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.red))
                            , PropertyFactory.iconHaloColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.orange))
                    ));

            //Map Bounds
            mapboxMap.setLatLngBoundsForCameraTarget(bounds);

            // Set the maximum zoom level of the map camera
            mapboxMap.setMaxZoomPreference(20);

            // Set the minimum zoom level of the map camera
            mapboxMap.setMinZoomPreference(8);

            mapboxMap.addOnMapClickListener(LocationDocumentFragment.this);
        });
    }

    protected void initMap(MapView v, Bundle savedInstanceState) {
        mapView = v;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    protected void initMarker(int resId) {
        iconDrawable = ContextCompat.getDrawable(Objects.requireNonNull(getActivity()), resId);
        iconResource = resId;
    }

    protected void updateMap(double x, double y) {
        long init = SystemClock.elapsedRealtime();
        long elapsed = init - lastMapUpdateTS;

        if (elapsed >= MAP_UPDATE_INTERVAL) {
            if (mapboxMap != null) {
                LatLng pos = mapboxMap.getProjection().getLatLngForProjectedMeters(new ProjectedMeters(y, x));

                if (!mapboxMap.getProjection().getVisibleRegion().latLngBounds.contains(pos)) {
                    CameraPosition position = new CameraPosition.Builder().target(pos).build();

                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), MAP_UPDATE_INTERVAL);
                }
//                // When the user clicks on the map, we want to animate the marker to that location.
                if (animator != null && animator.isStarted()) {
                    currentPosition = (LatLng) animator.getAnimatedValue();
                    animator.cancel();
                }

                animator = ObjectAnimator.ofObject(latLngEvaluator, currentPosition, pos).setDuration(MAP_UPDATE_INTERVAL * 2);
                animator.addUpdateListener(animatorUpdateListener);
                animator.start();
                currentPosition = pos;
            } else {
                Bitmap bm = ImageUtil.decodeCroppedBitmapFromResource(getResources(), R.drawable.map_macaiba, x, y, 100, 100);

                if (bm != null) {
                    mapImg.setImageBitmap(bm);
                    mapImg.invalidate();
                }
            }
            lastMapUpdateTS = SystemClock.elapsedRealtime();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (animator != null)
            animator.cancel();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
