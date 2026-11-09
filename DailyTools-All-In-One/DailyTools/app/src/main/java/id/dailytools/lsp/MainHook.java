package id.dailytools.lsp;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(LoadPackageParam lp) {
        String pkg = lp.packageName;
        try {
            if (Prefs.PKG.equals(pkg)) {
                // penanda "modul aktif" untuk layar pengaturan
                XposedHelpers.findAndHookMethod(Prefs.PKG + ".SettingsActivity", lp.classLoader,
                        "isModuleActive", XC_MethodReplacement.returnConstant(true));
                return;
            }
            if ("android".equals(pkg)) {
                Prefs.asyncOnly = true;
                FlagSecureHook.hookSystem(lp);
                return;
            }
            if ("com.android.systemui".equals(pkg)) return; // OTP sekarang lewat OtpListenerService
            // App biasa yang dipilih di scope LSPosed
            FlagSecureHook.hookApp(lp);
            CopyTextHook.hook(lp);
            ForceDarkHook.hook(lp);
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] gagal di " + pkg + ": " + t);
        }
    }
}
