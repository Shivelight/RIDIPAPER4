package id.shivelight.xridipaper4;

import android.app.Application;
import android.content.Context;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import id.shivelight.xridipaper4.hooks.DictionaryHook;
import id.shivelight.xridipaper4.hooks.FileBrowserHook;
import id.shivelight.xridipaper4.hooks.TextViewHook;

public class MainHook implements IXposedHookLoadPackage {

    public static Context appContext;
    public static final String TAG = "XRIDIPAPER4";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.ridi.paper")) return;

        XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) {
                appContext = (Context) param.args[0];
                XposedBridge.log(TAG + ": Application context obtained successfully!");
            }
        });
        TextViewHook textViewHook = new TextViewHook();
        textViewHook.hook(lpparam);

        FileBrowserHook fileBrowserHook = new FileBrowserHook();
        fileBrowserHook.hook(lpparam);

        DictionaryHook dictionaryHook = new DictionaryHook();
        dictionaryHook.hook(lpparam);
    }

}
