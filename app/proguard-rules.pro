# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --------------------------------------------基本指令区--------------------------------------------#
#指定外部模糊字典
-obfuscationdictionary dic.txt
#指定class模糊字典
-classobfuscationdictionary dic.txt
#指定package模糊字典
-packageobfuscationdictionary dic.txt

# 代码混淆压缩比，在0~7之间，默认为5，一般不做修改
-optimizationpasses 5

# 混合时不使用大小写混合，混合后的类名为小写
-dontusemixedcaseclassnames

# 指定不去忽略非公共库的类
-dontskipnonpubliclibraryclasses

# 这句话能够使我们的项目混淆后产生映射文件
# 包含有类名->混淆后类名的映射关系
-verbose

# 指定不去忽略非公共库的类成员
-dontskipnonpubliclibraryclassmembers

# 不做预校验，preverify是proguard的四个步骤之一，Android不需要preverify，去掉这一步能够加快混淆速度。
-dontpreverify

# 保留Annotation不混淆
-keepattributes *Annotation*,InnerClasses

# 避免混淆泛型
-keepattributes Signature

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# 指定混淆是采用的算法，后面的参数是一个过滤器
# 这个过滤器是谷歌推荐的算法，一般不做更改
-optimizations !code/simplification/cast,!field/*,!class/merging/*

# 保留我们使用的四大组件，自定义的Application等等这些类不被混淆
# 因为这些子类都有可能被外部调用
-keep public class * extends androidx.**
-keep public class * extends android.**

# 关闭警告信息 - Xposed
-dontwarn de.robv.android.xposed.**
-dontwarn android.content.res.XModuleResources
-dontwarn android.content.res.XResources

# 关闭警告信息 - 抖音
-dontwarn com.ss.android.ugc.aweme.**
-dontwarn com.ss.ugc.aweme.**
-dontwarn dmt.**

# 关闭警告信息 - 其他
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.conscrypt.Conscrypt$Version
-dontwarn org.conscrypt.Conscrypt
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# 模块核心
-keepclassmembers class io.github.xpler.core.** { *; }
-keep,allowobfuscation class io.github.xpler.core.KeepParam
-keep,allowobfuscation class io.github.xpler.core.Param
-keep,allowobfuscation class io.github.xpler.core.FutureHook
-keep,allowobfuscation class io.github.xpler.core.HookOnce
-keep,allowobfuscation class io.github.xpler.core.OnBefore
-keep,allowobfuscation class io.github.xpler.core.OnAfter
-keep,allowobfuscation class io.github.xpler.core.OnReplace
-keep,allowobfuscation class io.github.xpler.core.OnConstructorBefore
-keep,allowobfuscation class io.github.xpler.core.OnConstructorAfter
-keep,allowobfuscation class io.github.xpler.core.OnConstructorReplace
-keepclassmembers class * {
    @io.github.xpler.core.KeepParam <methods>;
    @io.github.xpler.core.Param <methods>;
    @io.github.xpler.core.FutureHook <methods>;
    @io.github.xpler.core.HookOnce <methods>;
    @io.github.xpler.core.OnBefore <methods>;
    @io.github.xpler.core.OnAfter <methods>;
    @io.github.xpler.core.OnReplace <methods>;
    @io.github.xpler.core.OnConstructorBefore <methods>;
    @io.github.xpler.core.OnConstructorAfter <methods>;
    @io.github.xpler.core.OnConstructorReplace <methods>;
}
# 模块状态
-keep class io.github.xpler.HookInit extends * { *; }
-keep class io.github.xpler.HookStatus extends * { *; }