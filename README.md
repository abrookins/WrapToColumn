# Wrap to Column: An IntelliJ Plugin That Wraps Text

This plugin wraps text to a maximum line width. It is intended as a replacement
for the `gq` command in Vim and `fill-paragraph` in Emacs, which are both dear
to my heart.

## Supported IntelliJ Editors

This plugin should work in any IntelliJ editor, including IntelliJ IDEA Community,
PyCharm, RubyMine, WebStorm, etc.

## Editor Actions

This plugin adds two new _actions_ to IntelliJ editors that you can use to wrap text:

- Wrap Line to Column: **Wraps selected text or the current line if no text is
  selected**. This is useful for IdeaVim users who wish to pair the command with
  motions like `vip` (select current paragraph).

- Wrap Paragraph to Column: **Wraps all lines in the current paragraph**. A
  paragraph is defined as text offset by blank lines -- including lines that
  only start with what looks like comment syntax (e.g., `//   `). Selected text
  is ignored (no selection is needed).
  
To learn how to you call these actions, read the _How To Use the Plugin_ section 
in this readme.

## Installing

Install the plugin from an IntelliJ editor (like IntelliJ Ultimate, PyCharm,
etc.) inside the Preferences -> Plugins window.

### From Within an IntelliJ Editor

Follow these steps to install this plugin from within an IntelliJ editor:
 
* Open Preferences -> Plugins and click _Marketplace_
* Search for "Wrap to column"
* Choose Wrap to Column
* Click Install
* Restart the editor when prompted

### From GitHub

To install the latest zip from GitHub:

* Either clone the repository or download the latest release
  (https://github.com/abrookins/WrapToColumn/releases)
* Open your IntelliJ editor of choice
* Choose Preferences -> Plugins -> Install plugin from disk
* Choose the **WrapToColumn.zip** (not the .jar file) file in the source
  checkout or your Downloads folder

## How To Use the Plugin

To use this plugin, you trigger one of its actions (Wrap Line to Column,
Wrap Paragraph to Column) with a keyboard shortcut, menu item, IdeaVim
command, or using [Search Everywhere](https://blog.jetbrains.com/idea/2020/05/when-the-shift-hits-the-fan-search-everywhere/).

Example usage:

**To wrap the line you're currently editing**, run the Wrap Line to Column action.

**To wrap all of the lines in the _paragraph_ that you are editing**, run the Wrap Paragraph to Column action.

**To wrap multiple lines and paragraphs in a file**, select the text to wrap, then run the Wrap Line to Column action (this plugin wraps selected text).

### Keyboard Shortcuts

The keyboard shortcuts for the **Wrap Line to Column** action are:

* Mac: Command + Control + Shift + W
* PC: Control + Alt + Shift + W

The keyboard shortcuts for the **Wrap Paragraph to Column** action are:

* Mac: Command + Control + Shift + P
* PC: Control + Alt + Shift + P

Feel free to change these in your keymap (Preferences -> Keymap) or IeaVim
configuration file!

### Menu Items

Menu items should exist for both commands in the Edit drop-down menu:

* Edit -> Wrap Line to Column
* Edit -> Wrap Paragraph to Column

### IdeaVim

When using IdeaVim, you can invoke the above commands using the following
actions:
* Wrap Line to Column: `com.andrewbrookins.idea.wrap.WrapAction`
* Wrap Paragraph to Column: `com.andrewbrookins.idea.wrap.WrapParagraphAction`

For example, you can add the following line to `.ideavimrc` to emulate Vim's
`gq` command:

```
nmap gq :action com.andrewbrookins.idea.wrap.WrapAction<CR>
```

## Settings

### How Does WrapToColumn Determine the Line Length?

The maximum width of wrapped text is based on one of the following settings, in
this order of priority:
 
1. The "Right margin override" setting found in the Wrap to Column settings panel

2. The right column setting configured for the language of the currently active
  editor tab

3. The editor's default right column setting

Read _Overriding the maximum line length_ in this README to learn how to set the
**right margin override** setting for the WrapToColumn plugin.

### Overriding the Maximum Line Length

By default, this plugin uses your configured global or language-specific right
margin setting as the column width to wrap at. However, you may provide a column
width that will override both of these settings.

This setting exists in Settings (Preferences on OS X) -> Tools -> Wrap to Column.

The setting is named **Right margin override**. This should be an integer that
represents the column at which the plugin will wrap text, similar to IntelliJ's
"right margin" setting.

### Minimum Raggedness (Alpha!)

By default, text is wrapped using a greedy line-breaking algorithm. This can
result in some lines having more whitespace than others.

You can turn on an alternative "minimum raggedness" algorithm in Settings ->
Tools -> Wrap to Column. When this setting is on, the plugin will reconfigure
text in a paragraph to produce the least amount of whitespace possible.

This feature is (still!) an **alpha** and may go away. Try it and see if
you like it!

### Tab Width Setting

Any lines that contain tabs (or are prefixed with tabs as an indent) will be
reflowed as if the tabs were characters spaced using the **tab size** you have
set in IntelliJ for the language you are editing.

This setting exists in the _Code Style_ section of the IntelliJ settings page.

As a result of this behavior, text will look right to you if your tab width is 4,
but not to your co-maintainer whose tab width is 8. This seems to be the best
trade-off.

## Monospaced Versus Variable Width Fonts

This plugin reflows selected text by assuming that each character takes one
column's worth of space (except tabs, which are expanded to your tab width).

This works fine with monospaced fonts. However, if you use a variable-width
font, which seems to be common for some languages like Chinese (see issue #11),
then the individual glyphs of the font take up more than one column.

The plugin will still wrap your text to e.g. 80 characters wide, but the
position won't match IntelliJ's right margin guide.

Anyway, I recommend that you use a monospaced font if you can.


## Roadmap

* Bug fixes

## Developing

This project uses gradle.

To run tests, use the `tests` gradle command.

To build the plugin zip, use the `buildPlugin` gradle command.


## License

This plugin is licensed under the GPLv2 and Apache License 2.0. See COPYING.txt
and LICENSE.txt.
