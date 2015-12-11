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

### Column width

By default, this plugin uses your configured global or language-specific right
margin setting as the column width to wrap at. However, you may provide a column
width that will override both of these settings.

This setting exists in Settings (Preferences on OS X) -> Tools -> Wrap to Column.

The setting is named "Column width override" and accepts an integer value.


### Tab width

Any lines that contain tabs (or are prefixed with tabs as an indent) will be
reflowed as if the tabs were characters spaced at your configured tab width.

This means that the text will look right to you if your tab width is 4, but not
to your co-maintainer whose tab width is 8. This seems to be the best trade-off.


## Menu item

A menu item should exist for the plugin in the Edit drop-down menu: Edit -> Wrap to Column


## A note about monospaced versus variable width fonts

This plugin reflows selected text by assuming that each character takes one
column's worth of space (except tabs, which are expanded to your tab width).

This works fine with monospaced fonts. However, if you use a variable-width
font, which seems to be common for some languages like Chinese (see issue #11),
then the individual glyphs of the font take up more than one column.

The plugin will still wrap your text to e.g. 80 characters wide, but the
position won't match Intellij's right margin guide.

Annnyway, I recommend that you use a monospaced font if you can.


## Roadmap
* Bug fixes 


## License

This plugin is licensed under the GPLv2 and Apache License 2.0 (I hear they're incompatible, but IANAL). See COPYING.txt and LICENSE.txt.
