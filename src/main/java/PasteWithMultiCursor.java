import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

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

        Runnable r = ()-> EditorModificationUtil.pasteTransferableAsBlock(editor, () -> getClipboardContent());

        // application.runWriteAction(r);
        WriteCommandAction.runWriteCommandAction(project, r);

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
