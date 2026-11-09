package id.dailytools.lsp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Layar info saja. Semua pengaturan ada di WebUI modul KernelSU "Daily Tools WebUI". */
public class SettingsActivity extends Activity {

    /** Di-hook oleh MainHook menjadi true saat modul aktif. */
    static boolean isModuleActive() {
        return false;
    }

    private static final String[] MANAGERS = {"com.rifsxd.ksunext", "me.weishu.kernelsu"};

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        int bg = 0xFF0A0E1A;
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(bg);
        root.setPadding(dp(28), dp(80), dp(28), dp(28));

        TextView title = new TextView(this);
        title.setText("Daily Tools");
        title.setTextColor(0xFFEEF1FF);
        title.setTextSize(26);
        root.addView(title);

        TextView st = new TextView(this);
        boolean on = isModuleActive();
        st.setText(on ? "● Modul LSPosed aktif" : "● Modul belum aktif di LSPosed");
        st.setTextColor(on ? 0xFF34D399 : 0xFFFB7185);
        st.setPadding(0, dp(10), 0, dp(24));
        root.addView(st);

        TextView info = new TextView(this);
        info.setText("Pengaturan ada di WebUI.\n\nBuka KernelSU Next → Modul → Daily Tools WebUI → ketuk ikon WebUI.");
        info.setTextColor(0xFF98A2C3);
        info.setTextSize(15);
        info.setLineSpacing(0, 1.25f);
        info.setGravity(Gravity.CENTER);
        root.addView(info);

        Button btn = new Button(this);
        btn.setText("Buka KernelSU");
        btn.setOnClickListener(v -> {
            for (String p : MANAGERS) {
                Intent i = getPackageManager().getLaunchIntentForPackage(p);
                if (i != null) {
                    startActivity(i);
                    return;
                }
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(28);
        root.addView(btn, lp);

        setContentView(root);
    }
}
