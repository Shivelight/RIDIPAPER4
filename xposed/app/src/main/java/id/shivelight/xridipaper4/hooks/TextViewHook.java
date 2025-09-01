package id.shivelight.xridipaper4.hooks;

import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import id.shivelight.xridipaper4.utils.PsvParser;

public class TextViewHook extends XC_MethodHook {

    private static final String TAG = "TextViewHook";
    private final Map<String, String> stringMap = new HashMap<>();

    public TextViewHook() {
        File layoutFile = new File(Environment.getExternalStorageDirectory().getPath() + "/XRIDIPAPER4/layout_string.psv");
        if (layoutFile.isFile()) {
            Log.d(TAG, "Layout strings file found");
            try (Stream<String> stream = Files.lines(layoutFile.toPath())) {
                stream.forEach(s -> {
                    List<String> columns = PsvParser.parse(s);
                    stringMap.put(columns.get(0), columns.get(1));
                });
            } catch (IOException e) {
                XposedBridge.log("Error reading layout strings file: " + e.getMessage());
            }
        } else {
            XposedBridge.log("Layout strings file not found in " + layoutFile.getPath());
        }

        File smaliFile = new File(Environment.getExternalStorageDirectory().getPath() + "/XRIDIPAPER4/smali_string.psv");
        if (smaliFile.isFile()) {
            XposedBridge.log("Smali strings file found");
            try (Stream<String> stream = Files.lines(smaliFile.toPath())) {
                stream.forEach(s -> {
                    List<String> columns = PsvParser.parse(s);
                    stringMap.put(columns.get(0), columns.get(1));
                });
            } catch (IOException e) {
                XposedBridge.log("Error reading smali strings file: " + e.getMessage());
            }
        } else {
            XposedBridge.log("Smali strings file not found in " + smaliFile.getPath());
        }

    }

    public String getTranslation(String stringArg) {
        String key = stringArg.replace("\n", "\\n");
        String translatedString = stringMap.get(key);
        if (translatedString == null) {
            return null;
        } else {
            return translatedString.replace("\\n", "\n");
        }
    }

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (param.args[0] == null || stringMap.isEmpty()) return;

        Method method = (Method) param.method;
        CharSequence originalText = (CharSequence) param.args[0];

        String stringArgs = originalText.toString();
        if (stringArgs.isBlank()) return;

        String translatedString = getTranslation(stringArgs);
        if (translatedString == null) {
            XposedBridge.log(method.getName() + " - Translation not found: " + originalText);
            return;
        }

        param.args[0] = translatedString;
    }

    public void hook(XC_LoadPackage.LoadPackageParam llpparam) {
        Log.d(TAG, "Hooking TextView.setText");
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, TextView.BufferType.class, this);

        // -----------------------
        // The result of methods bellow will be passed to TextView.setText. Which mean some strings will be "translated" twice.
        // -----------------------

        Log.d(TAG, "Hooking R.style.k4");
        // Spanned, before processed by Html. This allow matching strings with format tags like "변경한 <b>독서상태</b>는<br>여기서 필터링해서 볼 수 있어요!"
        XposedHelpers.findAndHookMethod("com.google.android.material.R.style", llpparam.classLoader, "k4", String.class, this);

        Log.d(TAG, "Hooking i.s.b.o (Intrinsics)");
        // TODO: This is basically string concatenation method, maybe concat first before getting translation?
        //       or translate each part separately?
        XposedHelpers.findAndHookMethod("i.s.b.o", llpparam.classLoader, "l", String.class, Object.class, this);
    }
}
