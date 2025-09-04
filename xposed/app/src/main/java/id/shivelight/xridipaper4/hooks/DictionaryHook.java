package id.shivelight.xridipaper4.hooks;

import static id.shivelight.xridipaper4.MainHook.appContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.lang.ref.WeakReference;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DictionaryHook {

    private static final String COLORDICT_INTENT = "colordict.intent.action.SEARCH";
    private String selection;
    private WeakReference<Object> epubReaderActivityRef;

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {

        // Add "Dict" button on the selection popup
        XposedHelpers.findAndHookMethod("com.ridi.books.viewer.reader.SelectionPopupController", lpparam.classLoader, "i", "android.graphics.RectF", "java.util.List", "com.ridi.books.viewer.reader.SelectionPopupController$Position", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                List<?> actionItemList = (List<?>) param.args[1];
                Object dictActionItem = XposedHelpers.callMethod(param.thisObject, "a", "Dict", (View.OnClickListener) view -> {
                    try {
                        openPopup(appContext, selection);
                    } catch (Exception e) {
                        XposedBridge.log("Error opening popup: " + e.getMessage());
                    }

                    Object epubReaderActivity;
                    if (epubReaderActivityRef != null && (epubReaderActivity = epubReaderActivityRef.get()) != null) {
                        Object selectionManager = XposedHelpers.callMethod(epubReaderActivity, "Y1");
                        // Clear selection method
                        XposedHelpers.callMethod(selectionManager, "b");
                    }
                });

                XposedHelpers.callMethod(actionItemList, "add", 0, dictActionItem);

            }
        });

        // Store selection
        XposedHelpers.findAndHookMethod("com.ridi.books.viewer.reader.epub.EPubReaderActivity", lpparam.classLoader, "onSelectionInfo", "java.lang.String", "java.lang.String", boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                selection = (String) param.args[1];
                // EpubReaderActivity has a reference to SelectionManager
                epubReaderActivityRef = new WeakReference<>(param.thisObject);
            }

        });
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        @SuppressLint("QueryPermissionsNeeded")
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return !list.isEmpty();
    }

    public final void openPopup(Context context, String keyword) {
        Intent intent = new Intent(COLORDICT_INTENT);
        intent.putExtra("EXTRA_QUERY", keyword);
        intent.putExtra("EXTRA_HEIGHT", getWindowHeightPx(context) / 2);
        intent.putExtra("EXTRA_GRAVITY", Gravity.TOP);
        intent.putExtra("EXTRA_FULLSCREEN", false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (isIntentAvailable(context, intent)) {
            context.startActivity(intent);
        } else {
            XposedBridge.log("ColorDict intent is available. [" + COLORDICT_INTENT + "]");
        }
    }

    private int getWindowHeightPx(Context context) {
        Object systemService = context.getSystemService(Context.WINDOW_SERVICE);
        if (systemService == null) {
            return 1000;
        }
        Display defaultDisplay = ((WindowManager) systemService).getDefaultDisplay();
        if (defaultDisplay == null) {
            return 1000;
        }
        Point point = new Point();
        defaultDisplay.getSize(point);
        return point.y;
    }
}
