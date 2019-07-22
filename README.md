# Wrap to Column: an Intellij Plugin

This plugin wraps text to a maximum line width, as defined in the section
"Setting the maximum line width." It is intended as a replacement for the `gq`
command in Vim and `fill-paragraph` in Emacs.


## Provided actions

Two commands are provided:

* Wrap Line to Column: Wraps selected text or the current line if no text is
  selected. This is useful for IdeaVim users who wish to pair the command with
  motions like `vip` (select current paragraph).

* Wrap Paragraph to Column: Wraps the paragraph in which the cursor appears.
  A paragraph is defined as text offset by blank lines -- including lines that
  only start with what looks like comment syntax (e.g., `//   `). Selected text
  is ignored (no selection is needed).


## Setting the maximum line width

The maximum width of wrapped text is based on one of the following settings, in
this order of priority:
 
* The "Right margin override" setting found in the Wrap to Column settings
  panel

* The right column setting configured for the language of the currently active
  editor tab

* The editor's default right column setting


## Installing

Install the plugin from an Intellij editor (like Intellij Ultimate, PyCharm,
etc.) inside the Preferences -> Plugins window.


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

### Keyboard shortcuts

The commands to run Wrap Line to Column are:

* Mac: Command + Control + Shift + W
* PC: Control + Alt + Shift + W

The commands to run Wrap Paragraph to Column are:

* Mac: Command + Control + Shift + P
* PC: Control + Alt + Shift + P

Feel free to change these in your keymap (Preferences -> Keymap) or IeaVim
configuration file!

When using IdeaVim, you can invoke above commands using the following actions:
* Wrap Line to Column: `com.andrewbrookins.idea.wrap.WrapAction`
* Wrap Paragraph to Column: `com.andrewbrookins.idea.wrap.WrapParagraphAction`

For example, you can add the following line to `.ideavimrc` to emulate Vim's `gq` command:

```
nmap gq :action com.andrewbrookins.idea.wrap.WrapAction<BR>
```


### Menu item

Menu items should exist for both commands in the Edit drop-down menu:

* Edit -> Wrap Line to Column
* Edit -> Wrap Paragraph to Column


## Settings

### Right margin override

By default, this plugin uses your configured global or language-specific right
margin setting as the column width to wrap at. However, you may provide a column
width that will override both of these settings.

This setting exists in Settings (Preferences on OS X) -> Tools -> Wrap to Column.

The setting is named "Right margin override" and accepts an integer value.


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
