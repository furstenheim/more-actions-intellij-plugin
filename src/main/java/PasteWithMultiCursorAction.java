import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.EditorModificationUtil.getStringContent;

public class PasteWithMultiCursorAction extends AnAction {
    private static final Logger LOG = Logger.getInstance(PasteWithMultiCursorAction.class);
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        Application application = ApplicationManager.getApplication();
        if (application == null) {
            return;
        }

        Transferable clipboardContent = getClipboardContent();
        if (clipboardContent == null) {
            return;
        }

        Runnable r = ()-> paste(editor, clipboardContent);

        application.runWriteAction(getRunnableWrapper(r, editor));
    }

    // Check com/intellij/codeInsight/editorActions/PasteHandler.java
    // For native code for pasting
    private void paste(Editor editor, Transferable clipboardContent) {
        String stringContent = getStringContent(clipboardContent);
        if (stringContent == null) {
            return;
        }
        int initialOffset = editor.getSelectionModel().getSelectionStart();

        final Project project = editor.getProject();
        final Document document = editor.getDocument();

        // Paste all content from clipboard
        ApplicationManager.getApplication().runWriteAction(
                () -> {
                    EditorModificationUtil.insertStringAtCaret(editor, stringContent, false, false);
                }
        );

        String sepEscaped = Pattern.quote("\n");

        List<CaretState> carets = editor.getCaretModel().getCaretsAndSelections();

        int offset = initialOffset;
        String[] parts = stringContent.split(sepEscaped, -1);
        // For every part of the pasted string select
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            // Lots of times we copy several lines, but we do not want a caret on the last empty line
            if (i == parts.length - 1 && part.length() == 0) {
                continue;
            }
            int start = offset;
            int end = offset + part.length();
            offset = end + 1;
            carets.add(createCaretState(editor, start, start, end));
        }

        editor.getCaretModel().setCaretsAndSelections(carets);

        // Indent lines to context
        ApplicationManager.getApplication().runWriteAction(
                () -> {
                    int length = stringContent.length();
                    final RangeMarker bounds = document.createRangeMarker(initialOffset, initialOffset + length);
                    indentEachLine(project, editor, bounds.getStartOffset(), bounds.getEndOffset());
                }
        );
    }

    private static void indentEachLine(Project project, Editor editor, int startOffset, int endOffset) {
        PsiDocumentManager.getInstance(project).commitAllDocuments();
        PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());

        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        final CharSequence text = editor.getDocument().getCharsSequence();
        if (startOffset > 0 && endOffset > startOffset + 1 && text.charAt(endOffset - 1) == '\n' && text.charAt(startOffset - 1) == '\n') {
            // There is a possible situation that pasted text ends by a line feed. We don't want to proceed it when a text is
            // pasted at the first line column.
            // Example:
            //    text to paste:
            //'if (true) {
            //'
            //    source:
            // if (true) {
            //     int i = 1;
            //     int j = 1;
            // }
            //
            //
            // We get the following on paste then:
            // if (true) {
            //     if (true) {
            //         int i = 1;
            //     int j = 1;
            // }
            //
            // We don't want line 'int i = 1;' to be indented here.
            endOffset--;
        }
        try {
            codeStyleManager.adjustLineIndent(file, new TextRange(startOffset, endOffset));
        }
        catch (IncorrectOperationException e) {
            LOG.error(e);
        }
    }

    // Based on https://github.com/danielkurecka/intellij-extra-actions/blob/master/src/cz/daku/intellij/extraActions/selectionSplit/SelectionSplitter.java
    private static CaretState createCaretState(Editor editor, int offset, int selStar, int selEnd) {
        LogicalPosition position = editor.offsetToLogicalPosition(offset);
        LogicalPosition start = editor.offsetToLogicalPosition(selStar);
        LogicalPosition end = editor.offsetToLogicalPosition(selEnd);
        return new CaretState(position, start, end);
    }

    private Transferable getClipboardContent() {
        CopyPasteManager instance = CopyPasteManager.getInstance();
        if (instance == null) {
            return null;
        }
        if (instance.areDataFlavorsAvailable(DataFlavor.stringFlavor)) {
            return instance.getContents();
        }
        return null;
    }

    // From https://stackoverflow.com/a/14472457/1536133
    protected Runnable getRunnableWrapper(final Runnable runnable, Editor editor) {
        return new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(editor.getProject(), runnable, "cut", ActionGroup.EMPTY_GROUP);
            }
        };
    }
}
