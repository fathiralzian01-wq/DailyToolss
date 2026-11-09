package id.dailytools.lsp;

import android.app.Notification;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

/**
 * Auto-copy OTP tanpa LSPosed: membaca notifikasi lewat API resmi Android.
 * Aktifkan lewat WebUI (tombol "Aktifkan akses notifikasi") atau Setelan > Akses notifikasi.
 */
public class OtpListenerService extends NotificationListenerService {
    private static String lastKey = "";
    private static long lastTime = 0;
    private final Handler main = new Handler(Looper.getMainLooper());

    private boolean enabled() {
        try {
            String v = Settings.Global.getString(getContentResolver(), Prefs.K_OTP);
            return v == null || !(v.equals("0") || v.equalsIgnoreCase("false"));
        } catch (Throwable t) {
            return true;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        try {
            if (sbn == null || !enabled()) return;
            Notification n = sbn.getNotification();
            if (n == null || (n.flags & Notification.FLAG_ONGOING_EVENT) != 0) return;
            String code = OtpParser.extract(n.extras);
            if (code == null) return;

            String key = sbn.getKey() + "|" + code;
            long now = System.currentTimeMillis();
            if (key.equals(lastKey) && now - lastTime < 30_000) return;
            lastKey = key;
            lastTime = now;
            copy(code);
        } catch (Throwable ignored) {
        }
    }

    private void copy(final String code) {
        main.post(() -> {
            try {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("OTP", code);
                PersistableBundle pb = new PersistableBundle();
                pb.putBoolean("android.content.extra.IS_SENSITIVE", true);
                clip.getDescription().setExtras(pb);
                cm.setPrimaryClip(clip);
                Toast.makeText(this, "OTP disalin: " + code, Toast.LENGTH_SHORT).show();
            } catch (Throwable ignored) {
            }
        });
    }
}
