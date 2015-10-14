package com.andrewbrookins.idea.wrap;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.actionSystem.EditorAction;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;


public class WrapAction extends EditorAction {
    private static final Logger log = Logger.getInstance(WrapAction.class);

    public WrapAction() {
        super(new WrapHandler());
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (ActionPlaces.isPopupPlace(e.getPlace())) {
            e.getPresentation().setVisible(e.getPresentation().isEnabled());
        }
    }

    private static class WrapHandler extends EditorActionHandler {
        @Override
        public void execute(final Editor editor, final DataContext dataContext) {
            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                public void run() {
                    final Project project = LangDataKeys.PROJECT.getData(dataContext);
                    final Document document = editor.getDocument();
                    final SelectionModel selectionModel = editor.getSelectionModel();
                    final int codeStyleRightMargin = editor.getSettings().getRightMargin(project);


                    if (!selectionModel.hasSelection()) {
                        selectionModel.selectLineAtCaret();
                    }

                    final String text = selectionModel.getSelectedText();
                    CodeWrapper wrapper = new CodeWrapper(codeStyleRightMargin);
                    String wrappedText = wrapper.wrap(text);

                    document.replaceString(selectionModel.getSelectionStart(),
                        selectionModel.getSelectionEnd(), wrappedText);
                }
            });
        }
    }
}
