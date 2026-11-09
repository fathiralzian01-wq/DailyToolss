package id.dailytools.lsp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.WeakHashMap;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Tekan lama pada teks apa pun -> muncul dialog berisi teks yang bisa diseleksi dan disalin.
 * Tidak mengubah view asli, jadi klik/scroll app tidak terganggu.
 * Dilewati kalau teks (atau induknya) sudah punya aksi tekan-lama sendiri.
 */
public final class CopyTextHook {
    private static final WeakHashMap<Activity, GestureDetector> DETECTORS = new WeakHashMap<>();

    static void hook(LoadPackageParam lp) {
        try {
            XposedHelpers.findAndHookMethod(Activity.class, "dispatchTouchEvent", MotionEvent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam p) {
                            try {
                                if (!Prefs.bool(Prefs.K_COPY, true)) return;
                                Activity a = (Activity) p.thisObject;
                                GestureDetector gd = DETECTORS.get(a);
                                if (gd == null) {
                                    gd = new GestureDetector(a, new GestureDetector.SimpleOnGestureListener() {
                                        @Override
                                        public void onLongPress(MotionEvent e) {
                                            onLong(a, e);
                                        }
                                    });
                                    DETECTORS.put(a, gd);
                                }
                                gd.onTouchEvent((MotionEvent) p.args[0]);
                            } catch (Throwable t) {
                                XposedBridge.log("[DailyTools] CopyText: " + t);
                            }
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] CopyText hook gagal: " + t);
        }
    }

    private static void onLong(Activity a, MotionEvent e) {
        View root = a.getWindow().getDecorView();
        TextView tv = find(root, e.getRawX(), e.getRawY());
        if (tv == null) return;
        if (tv instanceof EditText || tv.isTextSelectable()) return;
        CharSequence text = tv.getText();
        if (text == null || text.length() == 0) return;
        // hormati aksi tekan-lama bawaan app
        for (View v = tv; v != null; ) {
            if (v.isLongClickable()) return;
            Object parent = v.getParent();
            v = (parent instanceof View) ? (View) parent : null;
        }
        show(a, text.toString());
    }

    private static TextView find(View v, float x, float y) {
        if (v == null || v.getVisibility() != View.VISIBLE) return null;
        int[] loc = new int[2];
        v.getLocationOnScreen(loc);
        Rect r = new Rect(loc[0], loc[1], loc[0] + v.getWidth(), loc[1] + v.getHeight());
        if (!r.contains((int) x, (int) y)) return null;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = g.getChildCount() - 1; i >= 0; i--) {
                TextView t = find(g.getChildAt(i), x, y);
                if (t != null) return t;
            }
        }
        return (v instanceof TextView) ? (TextView) v : null;
    }

    private static void show(Activity a, String text) {
        try {
            TextView content = new TextView(a);
            content.setText(text);
            content.setTextSize(16);
            content.setTextIsSelectable(true);
            int pad = (int) (20 * a.getResources().getDisplayMetrics().density);
            content.setPadding(pad, pad, pad, pad);
            ScrollView sv = new ScrollView(a);
            sv.addView(content);
            new AlertDialog.Builder(a)
                    .setTitle("Salin teks")
                    .setView(sv)
                    .setPositiveButton("Salin semua", (d, w) -> {
                        ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("teks", text));
                        Toast.makeText(a, "Teks disalin", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Tutup", null)
                    .show();
        } catch (Throwable t) {
            XposedBridge.log("[DailyTools] dialog copy: " + t);
        }
    }
}
