# Wrap to Column: an Intellij Plugin

This plugin wraps selected text or, if no text is selected, the current line
to the column width specified in the editor's "Right Margin (columns)" setting.

**Versions newer than 1.0** respect language-specific right column settings
(assuming your version of Intellij-based editor supports that feature).

**Versions newer than 1.1.0** allow you to specify a column width that
overrides the current language setting and global column setting. See the
"Settings" section for more details.

This is a replacement for the Fill Paragraph command, which doesn't work for me.


## Installing

Install the plugin from an Intellij editor (like Intellij
Ultimate, PyCharm, etc.) inside the Preferences -> Plugins window.

### From the plugin repository

To install from the Plugin Repository:
 
* Open Preferences -> Plugins and click the Browse repositories ... button
* Search for "Wrap to column"
* Choose Wrap to Column
* Click Install
* Restart the editor when prompted

### To install from GitHub

To install the latest zip from GitHub:

* Either clone the repository or download the latest release (https://github.com/abrookins/WrapToColumn/releases)
* Open your Intellij editor of choice
* Choose Preferences -> Plugins -> Install plugin from disk
* Choose the **WrapToColumn.zip** (not the .jar file) file in the source checkout or your Downloads folder


## Running

### Keyboard shortcut

The default keyboard shortcut is Command+Control+Shift+W. It's not the best
shortcut, so feel free to change it in your keymap (Preferences -> Keymap) or
IeaVim configuration file.

**Note**: Commands invoked with `:action` using IdeaVim cannot yet accept a
visual range. So I suggest sticking to the IDE's keyboard shortcuts (using the
Keymap setting) rather than IdeaVim's configuration file.

### Menu item

A menu item should exist for the plugin in the Edit drop-down menu: Edit -> Wrap
to Column. If you like using a mouse, you can click on that.


## Settings

### Column width

By default, this plugin uses your configured global or language-specific right
margin setting as the column width to wrap at. However, you may provide a column
width that will override both of these settings.

This setting exists in Settings (Preferences on OS X) -> Tools -> Wrap to Column.

The setting is named "Column width override" and accepts an integer value.

### Minimum raggedness

By default, text is wrapped using a greedy line-breaking algorithm. This can
result in some lines having more whitespace than others.

You can turn on an alternative "minimum raggedness" algorithm in Settings ->
Tools -> Wrap to Column. When this setting is on, the plugin will reconfigure
text in a paragraph to produce the least amount of whitespace possible. Try it
and see if you like it!


### Tab width

Any lines that contain tabs (or are prefixed with tabs as an indent) will be
reflowed as if the tabs were characters spaced at your configured tab width.

This means that the text will look right to you if your tab width is 4, but not
to your co-maintainer whose tab width is 8. This seems to be the best trade-off.


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

This plugin is licensed under the GPLv2 and Apache License 2.0. See COPYING.txt
and LICENSE.txt.
