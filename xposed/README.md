# XRIDIPAPER4

Xposed module to enchance your RIDIng experience.

# Hooks

All hooks data should be stored in `XRIDIPAPER4/` in external storage directory (`/sdcard/` or `/storage/emulated/0/`).

## TextViewHook

Hook TextView to provide UI translation.

See also [/data/layout_string.psv](/data/layout_string.psv) and [/data/smali_string.psv](/data/smali_string.psv)

## FileManagementHook

- Hooks the file browser to allow loading ebooks from any directory on the external storage. Instead of being restricted to `Documents` directory.
- Store application files and metadata in external files directory (`Android/data/com.ridi.paper/files`) instead of internal files directory.

See [FileManagementHook.java](app/src/main/java/id/shivelight/xridipaper4/hooks/FileManagementHook.java) for more comments.

## DictionaryHook

**\* Require ColorDict to be installed**

Add dictionary button on selection popup. Available on EPUB viewer.

