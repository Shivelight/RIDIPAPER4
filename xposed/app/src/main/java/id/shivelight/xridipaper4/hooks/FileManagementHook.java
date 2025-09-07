package id.shivelight.xridipaper4.hooks;

import static id.shivelight.xridipaper4.MainHook.appContext;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Environment;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class FileManagementHook {

    public static final String CONTEXT_TAG_KEY = "id.shivelight.xridipaper4/FileBrowserHook.Context";

    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        hookFileBrowser(lpparam);
        hookInternalFilesDirToExternal(lpparam);
    }

    /**
     * Hooks the file browser to allow loading ebooks from any directory on the external storage.
     * By default, the Ridibooks app only allows loading ebooks from the `/Documents` directory.
     * This hook changes the root directory to the external storage directory, allowing users to
     * load ebooks from any directory on their device.
     */
    private static void hookFileBrowser(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> RidibooksApp = XposedHelpers.findClass("com.ridi.books.viewer.RidibooksApp", lpparam.classLoader);
        XposedHelpers.setStaticObjectField(RidibooksApp, "b", new File(Environment.getExternalStorageDirectory(), "/"));
        XposedHelpers.findAndHookMethod("i.s.b.o", lpparam.classLoader, "l", String.class, Object.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (param.args[1].equals("/Documents")) {
                    param.setResult(new File(Environment.getExternalStorageDirectory(), "/").toString());
                }
            }
        });
    }

    /**
     * This hook moves the app files and directory to external files storage. E.g. `/data/data/com.ridi.paper/files/books/`
     * to `/storage/emulated/0/Android/data/com.ridi.paper/files/books/`
     * So that it can be easily accessed from MTP. Easier to backup or browse.
     */
    private static void hookInternalFilesDirToExternal(XC_LoadPackage.LoadPackageParam lpparam) {
        // Move `books` directory to the external files storage.
        XposedHelpers.findAndHookMethod("com.ridi.books.viewer.RidibooksApp", lpparam.classLoader, "f", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                File booksDir = new File(appContext.getExternalFilesDir(null), "books");
                param.setResult(booksDir);
            }
        });

        // Return `Context.getExternalFilesDir()` instead of `Context.getFilesDir()`.
        // This moves affect important files and directories:
        // - covers/
        // - custom_fonts/
        // - default_reader_settings_v2.dat
        XposedHelpers.findAndHookMethod("com.ridi.books.viewer.RidibooksApp$a", lpparam.classLoader, "l", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                File file = appContext.getExternalFilesDir(null);
                param.setResult(file);
            }
        });

        // Move book's `metadata` directory to the external files storage.
        // Previously each book metadata is stored in `<files dir>/<book-uuid>`. Now it is stored in `<files dir>/metadata/<book-uuid>`.
        // Cleaner 😀👍
        XposedHelpers.findAndHookMethod("com.ridi.books.viewer.common.library.models.Book", lpparam.classLoader,
                "getMetadataDir", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        String bookId = (String) XposedHelpers.callMethod(param.thisObject, "getBookId");
                        File file = new File(appContext.getExternalFilesDir(null), "metadata/" + bookId);
                        if ((boolean) param.args[0] && !file.exists()) {
                            file.mkdirs();
                        }
                        param.setResult(file);
                    }
                });

        // --------------------
        // Move Realm database location to external files storage.
        // --------------------
        // We can't simply hook every `Context.getFilesDir()` call since it will crash the app.
        // Even if it didn't, we should be concerned about potential side effects.
        // So, we are only hooking it temporarily. Despite this, you'll still see some lock and json
        // files related to firebase.

        XC_MethodHook contextHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Context taggedContext = (Context) param.args[0];
                if (taggedContext != null) {
                    XposedHelpers.setAdditionalInstanceField(taggedContext, CONTEXT_TAG_KEY, true);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Context taggedContext = (Context) param.args[0];
                if (taggedContext != null) {
                    XposedHelpers.setAdditionalInstanceField(taggedContext, CONTEXT_TAG_KEY, null);
                }
            }
        };
        XposedHelpers.findAndHookConstructor("f.b.c2$a", lpparam.classLoader, Context.class, contextHook);
        XposedHelpers.findAndHookMethod("f.b.t1", lpparam.classLoader, "p0", Context.class, String.class, contextHook);

        XposedHelpers.findAndHookMethod(
                ContextWrapper.class,
                "getFilesDir",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object tag = XposedHelpers.getAdditionalInstanceField(param.thisObject, CONTEXT_TAG_KEY);

                        if (tag != null && (Boolean) tag) {
                            File file = appContext.getExternalFilesDir(null);
                            param.setResult(file);
                        }
                    }
                }
        );
    }

}
