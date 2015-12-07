# Wrap to Column: an Intellij Plugin

This plugin wraps selected text or, if no text is selected, the current line
to the column width specified in the editor's "Right Margin (columns)" setting.

As of version 1.0, the plugin respects per-language right column settings (assuming
your version of Intellij-based editor supports that feature).

As of version 1.1.0, the plugin allows you to specify a column width that
overrides the current language setting and global column setting. See the
"Settings" section for more details.

This is a replacement for the Fill Paragraph command, which doesn't work for me.


## Running

You can install this plugin directly from an Intellij editor (like Intellij
Ultimate, PyCharm, etc.) inside the Preferences -> Plugins -> Browse
repositories... window.

To install the latest zip from GitHub, clone the repository, open your Intellij
editor of choice, go to the Preferences window -> Plugins -> Install plugin
from disk, then choose the WrapToColumn.zip file located in the source repo.


## Keyboard shortcut

The default keyboard shortcut is Command+Control+Shift+W. Feel free to change
that beast in your keymap.


## Settings

You may provide an column width that will override both the language-specific
column setting and global column setting.

This setting exists in Settings (Preferences on OS X) -> Tools -> Wrap to Column.

The setting is named "Column width override" and accepts an integer value.


## Menu item

A menu item should exist for the plugin in the Edit drop-down menu: Edit -> Wrap to Column


## Roadmap

Next on deck: Bug fixes and a new command (or maybe a dialog for this one) that takes
arbitrary column width. (I often like comments to use a different width than the code).

## License

This plugin is licensed under the GPLv2. See COPYING.
