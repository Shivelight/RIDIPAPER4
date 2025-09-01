# XRIDIPAPER4

Xposed module to enchance your RIDIng experience.

# Hooks

All hooks data should be stored in `XRIDIPAPER4/` in external storage directory (`/sdcard/` or `/storage/emulated/0/`).

## TextViewHook

Hook TextView to provide UI translation.

See also [/data/layout_string.psv](/data/layout_string.psv) and [/data/smali_string.psv](/data/smali_string.psv)

## FileBrowserHook

Hook file browser to add ebooks from any directory instead of being restricted to  `/Documents` directory.
