package id.dailytools.lsp;

import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemClock;
import android.provider.Settings;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.AndroidAppHelper;
import de.robv.android.xposed.XposedHelpers;

/**
 * Pengaturan dibaca dari Settings.Global. WebUI KernelSU menulisnya sebagai root:
 *   settings put global dt_flag_secure 0
 * Settings.Global bisa dibaca semua proses (app, SystemUI, system_server) tanpa masalah SELinux.
 */
public final class Prefs {
    public static final String PKG = "id.dailytools.lsp";

    public static final String K_FLAG = "dt_flag_secure";
    public static final String K_OTP = "dt_auto_otp";
    public static final String K_COPY = "dt_copy_text";
    public static final String K_DARK = "dt_force_dark";
    public static final String K_DARK_PKGS = "dt_dark_pkgs";

    private static final String[] KEYS = {K_FLAG, K_OTP, K_COPY, K_DARK, K_DARK_PKGS};
    private static final ConcurrentHashMap<String, String> CACHE = new ConcurrentHashMap<>();
    private static final AtomicBoolean BUSY = new AtomicBoolean(false);
    private static volatile boolean loaded = false;
    private static volatile long last = 0;

    /** true di system_server: jangan pernah membaca sinkron di jalur panas (bisa memegang lock WM). */
    public static volatile boolean asyncOnly = false;

    private static Context ctx() {
        Context c = AndroidAppHelper.currentApplication();
        if (c != null) return c;
        try {
            Object at = XposedHelpers.callStaticMethod(
                    XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
            if (at != null) return (Context) XposedHelpers.callMethod(at, "getSystemContext");
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static void load() {
        try {
            Context c = ctx();
            if (c == null) return;
            ContentResolver r = c.getContentResolver();
            for (String k : KEYS) {
                String v = Settings.Global.getString(r, k);
                if (v == null) CACHE.remove(k);
                else CACHE.put(k, v);
            }
            loaded = true;
        } catch (Throwable ignored) {
        }
    }

    private static void ensure() {
        if (!loaded && !asyncOnly) {
            synchronized (Prefs.class) {
                if (!loaded) load();
            }
            last = SystemClock.uptimeMillis();
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (now - last >= 2000 && BUSY.compareAndSet(false, true)) {
            last = now;
            Thread t = new Thread(() -> {
                try {
                    load();
                } finally {
                    BUSY.set(false);
                }
            }, "dt-prefs");
            t.setDaemon(true);
            t.start();
        }
    }

    public static boolean bool(String key, boolean def) {
        ensure();
        String v = CACHE.get(key);
        if (v == null) return def;
        return !(v.equals("0") || v.equalsIgnoreCase("false"));
    }

    public static String str(String key, String def) {
        ensure();
        String v = CACHE.get(key);
        return v == null ? def : v;
    }

    /** "*" = semua app di scope; "none" atau daftar paket dipisah koma. */
    public static boolean isDarkTarget(String pkg) {
        String list = str(K_DARK_PKGS, "*");
        if (list == null || list.trim().isEmpty()) return false;
        for (String s : list.split("[,\\s]+")) {
            if (s.equals("*") || s.equals(pkg)) return true;
        }
        return false;
    }
}
