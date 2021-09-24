import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
        Project project = ProjectManager.getInstance().getDefaultProject();

        Transferable clipboardContent = getClipboardContent();
        if (clipboardContent == null) {
            return;
        }


        Runnable r = ()-> paste(editor, clipboardContent);

        WriteCommandAction.runWriteCommandAction(project, r);
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
}
