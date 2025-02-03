package com.suisho.disablescreenshotsound;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook;

public class Hook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(!lpparam.packageName.contains("com.android.systemui")) return;
        Class<?> runnableLambda= XposedHelpers.findClass("com.android.systemui.screenshot.ScreenshotController$$ExternalSyntheticLambda4",lpparam.classLoader);
        XposedHelpers.findAndHookMethod(runnableLambda, "run", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Object screenshotControllerInstance= param.thisObject;
                int classId=screenshotControllerInstance.getClass().getDeclaredField("$r8$classId").getInt(screenshotControllerInstance);
                //当classId为1时 原定会调用MediaPlayer.play()
                if(classId==1){
                    param.setResult(null);
                }
            }
        });

    }
}
