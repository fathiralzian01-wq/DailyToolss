#!/system/bin/sh
# Jalan tiap boot: jaga akses notifikasi dan pengecualian baterai (Infinix cukup agresif mematikan app)
PKG="id.dailytools.lsp"
until [ "$(getprop sys.boot_completed)" = "1" ]; do sleep 5; done
sleep 10
if pm path "$PKG" >/dev/null 2>&1; then
  dumpsys deviceidle whitelist +$PKG >/dev/null 2>&1
  cmd notification allow_listener "$PKG/$PKG.OtpListenerService" >/dev/null 2>&1
fi
