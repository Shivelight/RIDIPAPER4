package id.shivelight.xridipaper4.hooks;

import android.os.Environment;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FileBrowserHook {

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> runtimeObject = XposedHelpers.findClass("com.ridi.books.viewer.RidibooksApp", lpparam.classLoader);

        // Set the root directory to <external storage directory>/ which allow user to load ebook from any directory.
        XposedHelpers.setStaticObjectField(runtimeObject, "b", new File(Environment.getExternalStorageDirectory(), "/"));
        XposedHelpers.findAndHookMethod("i.s.b.o", lpparam.classLoader, "l", String.class, Object.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (param.args[1].equals("/Documents")) {
                    param.setResult(new File(Environment.getExternalStorageDirectory(), "/").toString());
                }
            }
        });

    }

}
