package id.shivelight.xridipaper4;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import id.shivelight.xridipaper4.hooks.TextViewHook;

public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.ridi.paper")) return;

        TextViewHook textViewHook = new TextViewHook();
        textViewHook.hook(lpparam);
    }

}
