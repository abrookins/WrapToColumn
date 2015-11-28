# About

This plugin wraps selected text or, if no text is selected, the current line
to the column width specified in the editor's "Right Margin (columns)" setting.

As of version 1.0, the plugin respects per-language right column settings (assuming
your version of Intellij-based editor supports that feature).

This is a replacement for the Fill Paragraph command, which doesn't work for me.


# Running

You can install this plugin directly from an Intellij editor inside the
Preferences -> Plugins -> Browse repositories... window.

To install the latest zip from GitHub, clone the repository, open your Intellij
editor of choice, go to the Preferences window -> Plugins -> Install plugin
from disk, then choose the WrapToColumn.zip file located in the source repo.


# Keyboard shortcut

The default keyboard shortcut is Command+Control+Shift+W. Feel free to change
that beast in your keymap.


# Menu item

A menu item should exist for the plugin in the Edit drop-down: Edit -> Wrap to Column


# Roadmap

Next on deck: Bug fixes and a new command (or maybe a dialog for this one) that takes
arbitrary column width. (I often like comments to use a different width than the code).

# License

This plugin is licensed under the GPLv2. See COPYING.
