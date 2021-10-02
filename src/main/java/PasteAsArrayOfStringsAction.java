import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.ide.CopyPasteManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.regex.Pattern;

import static com.intellij.openapi.editor.EditorModificationUtil.getStringContent;

public class PasteAsArrayOfStringsAction extends AnAction {
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
        String sepEscaped = Pattern.quote("\n");
        String[] parts = stringContent.split(sepEscaped, -1);
        StringBuilder stringBuilder = new StringBuilder();

        // Ignore last element if it is empty
        int adjustedLength = parts.length;
        if (parts.length >= 1 && parts[parts.length - 1].length() == 0) {
            adjustedLength -= 1;
        }

        for (int i = 0, partsLength = adjustedLength; i < partsLength - 1; i++) {
            String part = parts[i];
            stringBuilder.append("'");
            stringBuilder.append(part);
            stringBuilder.append("',");
        }
        if (adjustedLength >= 1) {
            stringBuilder.append("'");
            stringBuilder.append(parts[adjustedLength - 1]);
            stringBuilder.append("'");
        }

        EditorModificationUtil.insertStringAtCaret(editor, stringBuilder.toString(), false, false);
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
