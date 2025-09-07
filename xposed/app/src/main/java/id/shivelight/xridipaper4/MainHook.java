package id.shivelight.xridipaper4;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import id.shivelight.xridipaper4.hooks.DictionaryHook;
import id.shivelight.xridipaper4.hooks.FileManagementHook;
import id.shivelight.xridipaper4.hooks.TextViewHook;

public class MainHook implements IXposedHookLoadPackage {

    public static Context appContext;
    public static final File DATA_DIR = new File(Environment.getExternalStorageDirectory().getPath() + "/XRIDIPAPER4");
    public static final String TAG = "XRIDIPAPER4";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.ridi.paper")) return;

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                appContext = (Context) param.args[0];
                XposedBridge.log(TAG + ": Application context obtained!");
            }
        });

        XposedBridge.log(TAG + ": 🪝 Hooking TextView...");
        TextViewHook textViewHook = new TextViewHook();
        textViewHook.hook(lpparam);

        XposedBridge.log(TAG + ": 🪝 Hooking FileManagement...");
        FileManagementHook fileBrowserHook = new FileManagementHook();
        fileBrowserHook.hook(lpparam);

        XposedBridge.log(TAG + ": 🪝 Hooking Dictionary...");
        DictionaryHook dictionaryHook = new DictionaryHook();
        dictionaryHook.hook(lpparam);

        XposedBridge.log(TAG + ": 🦈 Hooking done!");
    }

}
