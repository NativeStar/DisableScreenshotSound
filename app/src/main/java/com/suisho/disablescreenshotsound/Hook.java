package com.suisho.disablescreenshotsound;

import android.media.MediaPlayer;
import android.os.Build;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.contains("com.android.systemui")) return;
        //Android版本适配
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.UPSIDE_DOWN_CAKE:
                hookAndroid14(lpparam);
                break;
            case Build.VERSION_CODES.VANILLA_ICE_CREAM:
                hookAndroid15(lpparam);
                break;
            default:
                XposedBridge.log("[DisableScreenshotSound] Unsupported android version,trying default method");
                hookAndroid14(lpparam);
                return;
        }
        XposedBridge.log("[DisableScreenshotSound] Success! Current SDK version:"+Build.VERSION.SDK_INT);
    }

    private void hookAndroid14(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        Class<?> runnableLambda = XposedHelpers.findClass("com.android.systemui.screenshot.ScreenshotController$$ExternalSyntheticLambda4", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(runnableLambda, "run", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object screenshotControllerInstance = param.thisObject;
                int classId = screenshotControllerInstance.getClass().getDeclaredField("$r8$classId").getInt(screenshotControllerInstance);
                //当classId为1时 原定会调用MediaPlayer.play()
                if(classId == 1) {
                    param.setResult(null);
                }
            }
        });
    }

    private void hookAndroid15(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        //将原MediaPlayer静音
        Class<?> soundControlImpl$PlayerClass = XposedHelpers.findClass("com.android.systemui.screenshot.ScreenshotSoundControllerImpl$player$1", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(soundControlImpl$PlayerClass, "invokeSuspend", Object.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                //借此返回的MediaPlayer
                MediaPlayer originResult = (MediaPlayer) param.getResult();
                originResult.setVolume(0.0f, 0.0f);
                param.setResult(originResult);
            }
        });
        //阻止播放
        Class<?> soundControlImpl$PlayScreenshotSoundClass = XposedHelpers.findClass("com.android.systemui.screenshot.ScreenshotSoundControllerImpl$playScreenshotSound$2", loadPackageParam.classLoader);
        XposedHelpers.findAndHookMethod(soundControlImpl$PlayScreenshotSoundClass, "invokeSuspend", Object.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                param.setResult(null);
            }
        });
    }
}
