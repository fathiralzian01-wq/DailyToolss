package id.dailytools.lsp;

import android.app.Activity;
import android.os.Bundle;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Paksa algorithmic darkening (force dark) di app yang belum punya dark mode.
 * Syarat: mode gelap sistem harus aktif.
 */
public final class ForceDarkHook {

    static void hook(final LoadPackageParam lp) {
        final String pkg = lp.packageName;

        // 1) Izinkan force dark di tiap Activity
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam p) {
                    try {
                        if (!enabled(pkg)) return;
                        ((Activity) p.thisObject).getWindow().getDecorView().setForceDarkAllowed(true);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] ForceDark onCreate: " + t);
        }

        // 2) Paksa keputusan force dark di ViewRootImpl (nama method beda antar versi Android)
        try {
            Class<?> vr = XposedHelpers.findClass("android.view.ViewRootImpl", lp.classLoader);
            int count = 0;
            for (Method m : vr.getDeclaredMethods()) {
                if (!m.getName().startsWith("determineForceDark")) continue;
                final Class<?> rt = m.getReturnType();
                if (rt != boolean.class && rt != int.class) continue;
                XposedBridge.hookMethod(m, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        if (!enabled(pkg)) return;
                        if (rt == boolean.class) p.setResult(true);
                        else p.setResult(1); // ForceDarkType.FORCE_DARK
                    }
                });
                count++;
            }
            if (count == 0) {
                XposedBridge.log("[DailyTools] determineForceDark* tidak ditemukan di ROM ini");
            }
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] ForceDark ViewRootImpl: " + t);
        }
    }

    private static boolean enabled(String pkg) {
        return Prefs.bool(Prefs.K_DARK, true) && Prefs.isDarkTarget(pkg);
    }
}
