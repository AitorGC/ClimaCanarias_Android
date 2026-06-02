# 🛡️ ClimaCanarias Advanced ProGuard/R8 Security Hardening Rules

# ----------------------------------------------------------------------------------
# 1. Obfuscation & Package Restructuring
# ----------------------------------------------------------------------------------
# Flatten and repackage your classes to high-obscurity root paths.
-repackageclasses 'c.e.o'
-allowaccessmodification

# Strip verbose debug signatures and source maps
-keepattributes !SourceFile,!LineNumberTable,!Signature,!InnerClasses,!EnclosingMethod,!*Annotations*

# Overload class/member names aggressively (using the a, b, c scheme)
-useuniqueclassmembernames

# ----------------------------------------------------------------------------------
# 2. Network Parsing & Serialization Rules (CORESafe: Moshi & Retrofit Protection)
# ----------------------------------------------------------------------------------
# Moshi converter relies on reflection/adapters for data integration models.
# Keep the API payload models and their structure intact to prevent Jackson/Moshi crash.
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep class com.example.data.** { *; }
-keep class com.example.db.** { *; }

# Keep OkHttp & Retrofit annotations/mechanisms
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,Signature
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**

# ----------------------------------------------------------------------------------
# 3. Local Persistence Rules (Room Protection)
# ----------------------------------------------------------------------------------
# Ensure Room classes and DAO interfaces are unperturbed to avoid SQL binding crashes.
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }

# ----------------------------------------------------------------------------------
# 4. Asynchronous Threading Rules (Coroutines Protection)
# ----------------------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# ----------------------------------------------------------------------------------
# 5. Native Log & Static Deobfuscation Mitigation
# ----------------------------------------------------------------------------------
# Strip all standard logging in Release builds (to prevent leaks of business flows)
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}
