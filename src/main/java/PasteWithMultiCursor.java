import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.EditorModificationUtil.getStringContent;

public class PasteWithMultiCursor extends AnAction {
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

    private void paste(Editor editor, Transferable clipboardContent) {
        String stringContent = getStringContent(clipboardContent);
        if (stringContent == null) {
            return;
        }
        int initialOffset = editor.getSelectionModel().getSelectionStart();
        EditorModificationUtil.insertStringAtCaret(editor, stringContent, false, false);
        String sepEscaped = Pattern.quote("\n");


        List<CaretState> carets = editor.getCaretModel().getCaretsAndSelections();

        int offset = initialOffset;
        String[] parts = stringContent.split(sepEscaped, -1);
        for (String part : parts) {
            int start = offset;
            int end = offset + part.length();
            offset = end + 1;
            carets.add(createCaretState(editor, start, start, end));
        }

        editor.getCaretModel().setCaretsAndSelections(carets);
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
