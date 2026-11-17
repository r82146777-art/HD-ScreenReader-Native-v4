# HD ScreenReader Native v4

صفحه‌خوان HD — نسخه ۴ (کاوش سریع‌تر)

این مخزن سورس پروژه Android Studio است + workflow برای ساخت خودکار APK در GitHub Actions.

## ساخت APK

- به صورت خودکار با هر push به main یا با اجرای دستی workflow «Build APK» ساخته می‌شود.
- پس از اتمام، APK در بخش Actions → Artifacts قابل دانلود است (نام: `HD-ScreenReader-debug-apk`).

## نصب
1. APK را دانلود و نصب کنید.
2. به تنظیمات > دسترس‌پذیری بروید و «صفحه‌خوان HD» را فعال کنید.
3. برای کاوش لمسی و TTS فارسی، موتور گفتار را تنظیم کنید.

## تغییرات نسبت به سورس اصلی
- فقط import گمشده `android.app.AlertDialog` در MainActivity اضافه شد (بدون آن کامپایل نمی‌شد).
- `app/build.gradle.kts` برای پشتیبانی از compileOptions و kotlinOptions تکمیل شد.
- هیچ تغییری در منطق سرویس دسترس‌پذیری یا UI اصلی ایجاد نشد.

## لینک
https://github.com/r82146777-art/HD-ScreenReader-Native-v4
