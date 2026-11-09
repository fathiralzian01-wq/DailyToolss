#!/system/bin/sh
ui_print "- Daily Tools v2.0 (all-in-one)"

PKG="id.dailytools.lsp"
APK="$MODPATH/apk/DailyTools.apk"

if [ -f "$APK" ]; then
  ui_print "- Memasang APK Daily Tools..."
  if pm install -r "$APK" >/dev/null 2>&1; then
    ui_print "  APK terpasang"
  else
    ui_print "  Gagal otomatis, pasang apk/DailyTools.apk secara manual"
  fi
else
  ui_print "- Tidak ada apk/DailyTools.apk, pasang APK secara manual"
fi

ui_print "- Mengaktifkan akses notifikasi (Auto-copy OTP)..."
cmd notification allow_listener "$PKG/$PKG.OtpListenerService" >/dev/null 2>&1
dumpsys deviceidle whitelist +$PKG >/dev/null 2>&1

ui_print "- Selesai."
ui_print "- Untuk FLAG_SECURE, copy teks, dark mode: aktifkan di LSPosed,"
ui_print "  centang scope System Framework + app target, lalu reboot."
ui_print "- Buka WebUI dari daftar modul untuk mengatur fitur."
