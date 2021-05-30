//----------------------------------------------------------------------------------------------
// Copyright (c) 2013 Technology Solutions UK Ltd. All rights reserved.
//----------------------------------------------------------------------------------------------

package com.gtp.hunter;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.room.Room;

import com.gtp.hunter.rfid.asciiprotocol.AsciiCommander;
import com.gtp.hunter.util.AlertSeverity;
import com.gtp.hunter.util.AlertType;
import com.gtp.hunter.wms.activity.LoginActivity;
import com.gtp.hunter.wms.client.AlertClient;
import com.gtp.hunter.wms.model.Address;
import com.gtp.hunter.wms.model.AddressField;
import com.gtp.hunter.wms.model.Alert;
import com.gtp.hunter.wms.model.Product;
import com.gtp.hunter.wms.model.User;
import com.gtp.hunter.wms.repository.MobileDatabase;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import timber.log.Timber;

public class HunterMobileWMS extends Application {

    private static MobileDatabase db;
    private static final String CUSTOMER_PREFIX = "534C52";
    private static boolean mapboxInitialized = false;

    public static final Comparator<Product> compareBySku = (Product o1, Product o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o2 == null) return -1;
        if (o1 == null) return 1;
        if (o1.getSku() == null && o2.getSku() == null) return 0;
        if (o2.getSku() == null) return -1;
        if (o1.getSku() == null) return 1;
        return o1.getSku().compareTo(o2.getSku());
    };

    public static final Comparator<Product> compareByName = (Product o1, Product o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o2 == null) return -1;
        if (o1 == null) return 1;
        if (o1.getName() == null && o2.getName() == null) return 0;
        if (o2.getName() == null) return -1;
        if (o1.getName() == null) return 1;
        return o1.getName().compareTo(o2.getName());
    };

    public static final Comparator<Address> compareByMetaname = (Address o1, Address o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o2 == null) return -1;
        if (o1 == null) return 1;
        if (o1.getMetaname() == null && o2.getMetaname() == null) return 0;
        if (o2.getMetaname() == null) return -1;
        if (o1.getMetaname() == null) return 1;
        return o1.getMetaname().compareTo(o2.getMetaname());
    };

    private static List<Address> addrList;
    private static List<Product> productList;

    private static HunterMobileWMS instance;

    private static User user;
    private static String token;
    private static boolean rfidDisabled;
    private static AsciiCommander commander;

    public static boolean isAddressesLoaded() {
        return addrList != null && !addrList.isEmpty();
    }

    private static boolean qrcodeRead;

    public static boolean isProductsLoaded() {
        return productList != null && !productList.isEmpty();
    }

    public static boolean isSynchronized() {
        return isAddressesLoaded() && isProductsLoaded();
    }

    public static CopyOnWriteArrayList<Address> filterAddressParent() {
        CopyOnWriteArrayList<Address> addressList = new CopyOnWriteArrayList<>(getAddressList("ROAD"));
        addressList.addAll(getAddressList("BLOCK"));
        addressList.addAll(getAddressList("RACK"));
        addressList.addAll(getAddressList("DRIVE-IN"));
        addressList.addAll(getAddressList("DOCK"));
        Address[] ar = new Address[addressList.size()];


        ar = addressList.toArray(ar);
        Arrays.sort(ar, compareByMetaname);
        addressList.clear();
        addressList.addAll(Arrays.asList(ar));
        return addressList;
    }

    public static boolean shouldScanForTag() {
        return !qrcodeRead && getUser().getProperties().containsKey("requires-rtls-tag") && Objects.requireNonNull(HunterMobileWMS.getUser().getProperties().get("requires-rtls-tag")).equalsIgnoreCase("TRUE");
    }

    public static boolean shouldScanTransporter() {
        return !qrcodeRead && getUser().getProperties().containsKey("requires-transporter-tag") && Objects.requireNonNull(HunterMobileWMS.getUser().getProperties().get("requires-transporter-tag")).equalsIgnoreCase("TRUE");
    }

    public static void setQrcodeRead(boolean read) {
        qrcodeRead = read;
    }

    /// Returns the current AsciiCommander

    public AsciiCommander getCommander() {
        return commander;
    }
    /// Sets the current AsciiCommander


    public void setCommander(AsciiCommander _commander) {
        commander = _commander;
    }

    public static HunterMobileWMS getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance;
        // or return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        Timber.e("Hunter Mobile WMS is being initialized");
        instance = this;
        super.onCreate();
        initializeDatabase();
        if (!mapboxInitialized)
            initializeMapbox();
        initializeTimber();
        if (BuildConfig.DEBUG)
            initializeExceptionCatcher();
    }

    private void initializeTimber() {
        Timber.plant(new Timber.DebugTree());
    }

    private void initializeDatabase() {
        db = Room.databaseBuilder(getApplicationContext(), MobileDatabase.class, "hunter4").fallbackToDestructiveMigration().build();
    }

    private void initializeExceptionCatcher() {
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            Future<Boolean> fut = Executors.newSingleThreadExecutor().submit(() -> {
                AlertClient alertClient = new AlertClient(this);
                Alert al = new Alert();
                String version = "";
                StringBuilder msg = new StringBuilder("{\"stacktrace\":[");

                for (StackTraceElement el : e.getStackTrace()) {
                    if (el.getClassName().startsWith("com.gtp.hunter")) {
                        msg.append(serializeElement(el));
                    }
                }
                msg.append("]");
                if (e.getCause() != null) {
                    msg.append(",\"cause\":[");
                    for (StackTraceElement el : e.getCause().getStackTrace()) {
                        msg.append(serializeElement(el));
                    }
                    msg.append("]");
                }
                msg.append("}");
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

                    version = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException ignored) {
                }
                al.setDescription("Crashed with error: " + e.getLocalizedMessage());
                al.setItem("Hunter Mobile WMS " + version);
                al.setMsg(msg.toString());
                al.setSeverity(AlertSeverity.SEVERE);
                al.setType(AlertType.DEVICE);
                return alertClient.save(al) != null;
            });
            try {
                if (fut.get()) {
                    AlarmManager alarm = (AlarmManager) getContext().getSystemService(Activity.ALARM_SERVICE);
                    PendingIntent pi = PendingIntent.getActivity(getContext(), 12345, new Intent(getContext(),
                                    LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK),
                            PendingIntent.FLAG_ONE_SHOT);
                    alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, pi);
                }
            } catch (ExecutionException | InterruptedException ignored) {
            }
            Timber.d(e, "Thread Exception %s", thread.getName());
            System.exit(2);
        });
    }

    private void initializeMapbox() {
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getApplicationContext(), getString(R.string.mapbox_access_token));
        // Set up the OfflineManager
        OfflineManager offlineManager = OfflineManager.getInstance(this);
        byte[] metadata;
        // Create a bounding box for the offline region
        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                .include(new LatLng(0.2, 0.2)) // Northeast
                .include(new LatLng(-0.2, -0.2)) // Southwest
                .build();
        // Define region of map tiles
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                "mapbox://styles/mateustormin/ckgb45nep2e3z19mi30pocrg4",
                latLngBounds,
                0,
                6,
                getResources().getDisplayMetrics().density
        );

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("region_name", "SOLAR BR");
            String json = jsonObject.toString();
            metadata = json.getBytes(StandardCharsets.UTF_8);
        } catch (Exception exception) {
            Timber.e("Failed to encode metadata: %s", exception.getMessage());
            metadata = null;
        }
        // Create the region asynchronously
        if (metadata != null) {
            offlineManager.createOfflineRegion(definition, metadata,
                    new OfflineManager.CreateOfflineRegionCallback() {
                        @Override
                        public void onCreate(OfflineRegion offlineRegion) {
                            offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);

                            // Monitor the download progress using setObserver
                            offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
                                @Override
                                public void onStatusChanged(OfflineRegionStatus status) {

                                    // Calculate the download percentage
                                    double percentage = status.getRequiredResourceCount() >= 0
                                            ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                                            0.0;

                                    if (status.isComplete()) {
                                        // Download complete
                                        Timber.d("Region downloaded successfully.");
                                    } else if (status.isRequiredResourceCountPrecise()) {
                                        Timber.d("Percentage: %.2f", percentage);
                                    }
                                }

                                @Override
                                public void onError(OfflineRegionError error) {
                                    // If an error occurs, print to logcat
                                    Timber.e("onError reason: %s", error.getReason());
                                    Timber.e("onError message: %s", error.getMessage());
                                }

                                @Override
                                public void mapboxTileCountLimitExceeded(long limit) {
                                    // Notify if offline region exceeds maximum tile count
                                    Timber.e("Mapbox tile count limit exceeded: %d", limit);
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            Timber.e("Error: %s", error);
                        }
                    });
        }
        mapboxInitialized = true;
    }

    public static List<Product> getProductList() {
        return productList;
    }

    public static void setProductList(List<Product> prdList) {
        productList = prdList;
    }

    public static List<Product> getProductList(String type) {
        List<Product> ret = new ArrayList<>();


        for (Product p : productList) {
            if (p.getModel().getMetaname().equals(type))
                ret.add(p);
        }
        return ret;
    }

    public static List<Address> getAddressList(String type) {
        List<Address> ret = new ArrayList<>();

        for (Address a : addrList) {
            if (a.getModel().getMetaname().equals(type))
                ret.add(a);
        }

        return sortAddresses(ret);
    }

    public static List<Address> sortAddresses(List<Address> addrList) {
        Address[] ar = new Address[addrList.size()];

        ar = addrList.toArray(ar);
        Arrays.sort(ar, compareByMetaname);
        return Arrays.asList(ar);
    }

    private static final List<String> PA_EXCEPTION_SKUS = new ArrayList();
    private static final List<UUID> PA_PM_LIST = new ArrayList<>();
    private static final List<UUID> PD_PM_LIST = new ArrayList<>();
    private static final List<UUID> MP_PM_LIST = new ArrayList<>();
    private static final List<UUID> VAS_PM_LIST = new ArrayList<>();

    static {
        PA_PM_LIST.add(UUID.fromString("c69fcf60-2b0f-11e9-a948-0266c0e70a8c"));//PA
        PA_PM_LIST.add(UUID.fromString("23019dd9-59b4-47a7-bcb0-e99d4ab12e91"));//ACH
        PA_PM_LIST.add(UUID.fromString("1d34281a-691c-45d7-8215-4af66a53b563"));//AGC
        PA_PM_LIST.add(UUID.fromString("7d695ae3-99f0-4210-b136-91e7d47b4cf3"));//AGU
        PA_PM_LIST.add(UUID.fromString("cd534614-277d-4ea5-b364-41b2aa9703ff"));//BBV
        PA_PM_LIST.add(UUID.fromString("db72e8de-7e7d-4976-891a-0c1f77e12241"));//BLZ
        PA_PM_LIST.add(UUID.fromString("30593258-ec1c-4967-9356-f3260d610098"));//CBO
        PA_PM_LIST.add(UUID.fromString("24c7feed-e9fd-4f6a-94e7-2f973b91eeb9"));//CER
        PA_PM_LIST.add(UUID.fromString("70068575-4aba-4cff-981e-f90c03413544"));//CHA
        PA_PM_LIST.add(UUID.fromString("03beab7b-56e2-401e-a9e3-d727dbcbb1a2"));//ENE
        PA_PM_LIST.add(UUID.fromString("d2427d65-e166-407a-bed9-bdccb31dd8d9"));//H&I
        PA_PM_LIST.add(UUID.fromString("d6e8cc38-e074-4d37-9b22-63c0f20a23bd"));//REF
        PA_PM_LIST.add(UUID.fromString("17133b52-785f-4fc3-9835-5cd4272013e4"));//RFC
        PA_PM_LIST.add(UUID.fromString("cc20fe4c-ebe2-4ec2-a5e4-091efe73865b"));//RFP
        PA_PM_LIST.add(UUID.fromString("54e20a45-e0fc-4265-bbc2-28d53ef6827d"));//SUC

        PD_PM_LIST.add(UUID.fromString("95b564e9-ea5a-4caa-adbe-06fc7dd0b966"));//PALLET
        PD_PM_LIST.add(UUID.fromString("b257e34f-f2b4-4dca-8153-a310e1feebe3"));//PALLET
        PD_PM_LIST.add(UUID.fromString("5548bd5d-b089-4413-b8f4-f5f3ccf43d28"));//EUCATEX

        MP_PM_LIST.add(UUID.fromString("c69d79f4-2b0f-11e9-a948-0266c0e70a8c"));//MP

        VAS_PM_LIST.add(UUID.fromString("de9a6c5d-7d0e-42b3-822d-8e7f6cd8341c"));//VAS

        PA_EXCEPTION_SKUS.add("1000087");//Cilindro CO2
        PA_EXCEPTION_SKUS.add("1200081");//Copo 300
        PA_EXCEPTION_SKUS.add("1200073");//Tampa 300
        PA_EXCEPTION_SKUS.add("1200474");//Copo 400
        PA_EXCEPTION_SKUS.add("1200475");//Copo 500
        PA_EXCEPTION_SKUS.add("1200568");//Tampa 400/500
    }

    public static List<Product> getPAProductList() {
        List<Product> prdList = new ArrayList<>();

        for (Product a : productList) {
            if (PA_PM_LIST.contains(a.getModel().getId()))
                prdList.add(a);
            else if (PA_EXCEPTION_SKUS.contains(a.getSku()))
                prdList.add(a);
        }
        Product[] pr = new Product[prdList.size()];

        pr = prdList.toArray(pr);
        Arrays.sort(pr, compareBySku);
        return Arrays.asList(pr);
    }

    public static List<Product> getPDProductList() {
        List<Product> prdList = new ArrayList<>();

        for (Product a : productList) {
            if (PD_PM_LIST.contains(a.getModel().getId()))
                prdList.add(a);
        }
        Product[] pr = new Product[prdList.size()];
        pr = prdList.toArray(pr);
        Arrays.sort(pr, compareBySku);
        return Arrays.asList(pr);
    }

    public static List<Product> getRSTProductList() {
        List<Product> ret = new ArrayList<>();

        for (Product a : productList) {
            if (PD_PM_LIST.contains(a.getId()))
                ret.add(a);
            else if ((a.getName().contains(" LT ") || a.getName().contains(" LATA ") || a.getName().startsWith("LT ") || a.getName().startsWith("LATA "))
                    && !(a.getName().startsWith("TAMPA ") || a.getName().startsWith("TP "))
                    && MP_PM_LIST.contains(a.getModel().getId())
                    ||
                    (a.getName().startsWith("VASIL") || a.getName().startsWith("EMB "))
                            && VAS_PM_LIST.contains(a.getModel().getId())
            )
                ret.add(a);
        }
        Product[] pr = new Product[ret.size()];
        pr = ret.toArray(pr);
        Arrays.sort(pr, compareBySku);
        return Arrays.asList(pr);
    }

    public static List<Address> getBottomAddressListTopType(String topType) {
        List<UUID> top = new ArrayList<>();
        List<UUID> middle = new ArrayList<>();
        List<Address> ret = new ArrayList<>();

        for (Address a : addrList) {
            if (a.getModel().getMetaname().equals(topType))
                top.add(a.getId());
        }
        for (Address a : addrList) {
            if (top.contains(a.getParent_id()))
                middle.add(a.getId());
        }
        for (Address a : addrList) {
            if (middle.contains(a.getParent_id()))
                ret.add(a);
        }
        Address[] ar = new Address[ret.size()];

        ar = ret.toArray(ar);
        Arrays.sort(ar, compareByMetaname);
        return Arrays.asList(ar);
    }

    public static void setAddressList(List<Address> addressList) {
        addrList = addressList;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User sr) {
        user = sr;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String tkn) {
        token = tkn;
    }

    public static String getCustomerPrefix() {
        return CUSTOMER_PREFIX;
    }

    public static void setRFIDDisabled(boolean b) {
        rfidDisabled = b;
    }

    public static boolean isRFIDAvailable() {
        return !rfidDisabled;
    }

    public static Product findProduct(UUID product_id) {
        for (Product p : productList)
            if (p.getId().equals(product_id))
                return p;
        return null;
    }

    public static Address findAddress(UUID address_id) {
        for (Address a : addrList)
            if (a.getId().equals(address_id)) {
                if (BuildConfig.DEBUG) Log.d("Address Found", a.getName());
                return a;
            }
        return null;
    }

    public static void putFields(UUID address_id, Collection<? extends AddressField> col) {
        for (Address a : addrList)
            if (a.getId().equals(address_id)) {
                if (a.getFields().size() != col.size())
                    a.getFields().addAll(col);
                break;
            }
    }

    public static MobileDatabase getDB() {
        return db;
    }

    private String serializeElement(StackTraceElement el) {
        return "{\"file\":\"" + el.getFileName() +
                "\",\"line\":" +
                el.getLineNumber() +
                ",\"method\":\"" +
                el.getMethodName() +
                "\"}";
    }
}
