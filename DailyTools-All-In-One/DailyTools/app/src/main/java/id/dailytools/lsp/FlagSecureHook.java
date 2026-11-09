package id.dailytools.lsp;

import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public final class FlagSecureHook {
    private static final int SECURE = WindowManager.LayoutParams.FLAG_SECURE;

    private static boolean on() {
        return Prefs.bool(Prefs.K_FLAG, true);
    }

    /** Scope "android": screenshot & screen record sistem tidak lagi menganggap window secure. */
    static void hookSystem(LoadPackageParam lp) {
        try {
            Class<?> ws = XposedHelpers.findClassIfExists("com.android.server.wm.WindowState", lp.classLoader);
            if (ws == null) {
                XposedBridge.log("[DailyTools] WindowState tidak ditemukan");
                return;
            }
            XposedBridge.hookAllMethods(ws, "isSecureLocked", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (on()) p.setResult(false);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] FlagSecure(system): " + t);
        }
    }

    /** Scope app: hapus FLAG_SECURE sebelum dipasang oleh app. */
    static void hookApp(LoadPackageParam lp) {
        try {
            XposedHelpers.findAndHookMethod(Window.class, "setFlags", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (!on()) return;
                    p.args[0] = ((int) p.args[0]) & ~SECURE;
                    p.args[1] = ((int) p.args[1]) & ~SECURE;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] Window.setFlags: " + t);
        }
        try {
            XposedHelpers.findAndHookMethod(Window.class, "addFlags", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (on()) p.args[0] = ((int) p.args[0]) & ~SECURE;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] Window.addFlags: " + t);
        }
        try {
            XposedHelpers.findAndHookMethod(Window.class, "setAttributes",
                    WindowManager.LayoutParams.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam p) {
                            if (on()) strip(p.args[0]);
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] Window.setAttributes: " + t);
        }
        try {
            XposedHelpers.findAndHookMethod(SurfaceView.class, "setSecure", boolean.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (on()) p.args[0] = false;
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] SurfaceView.setSecure: " + t);
        }
        // Dialog / popup yang menambah window langsung lewat WindowManager
        try {
            Class<?> wmi = XposedHelpers.findClass("android.view.WindowManagerImpl", lp.classLoader);
            XC_MethodHook h = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) {
                    if (on()) strip(p.args[1]);
                }
            };
            XposedHelpers.findAndHookMethod(wmi, "addView", android.view.View.class,
                    android.view.ViewGroup.LayoutParams.class, h);
            XposedHelpers.findAndHookMethod(wmi, "updateViewLayout", android.view.View.class,
                    android.view.ViewGroup.LayoutParams.class, h);
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] WindowManagerImpl: " + t);
        }
    }

    private static void strip(Object lp) {
        if (lp instanceof WindowManager.LayoutParams) {
            ((WindowManager.LayoutParams) lp).flags &= ~SECURE;
        }
    }
}
