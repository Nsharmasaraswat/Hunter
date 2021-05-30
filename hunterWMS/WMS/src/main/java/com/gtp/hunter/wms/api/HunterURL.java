package com.gtp.hunter.wms.api;

public abstract class HunterURL {
    private static final String CORE_ROOT = "hunter-core";
    private static final String PROCESS_ROOT = "hunter-core";
    private static final String CUSTOM_ROOT = "hunter-custom-solar";

    public static final String CORE = CORE_ROOT + "/api/";
    public static final String PROCESS = PROCESS_ROOT + "/api/";
    public static final String CUSTOM = CUSTOM_ROOT + "/api/";

    public static String IP;
    public static String PORT;
    public static String BASE;
    public static String PROCESS_WS;
    public static boolean useSSL;

    public static void changeURL(String ip, String port, boolean ssl) {
        HunterURL.IP = ip;
        HunterURL.PORT = port;
        HunterURL.useSSL = ssl;
        HunterURL.BASE = (useSSL ? "https://" : "http://") + HunterURL.IP + ":" + HunterURL.PORT + "/";
        HunterURL.PROCESS_WS = (useSSL ? "wss://" : "ws://") + HunterURL.IP + ":" + HunterURL.PORT + "/hunter-core/";
    }
}
