import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import java.awt.datatransfer.StringSelection;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CopyPath extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null) {
            return;
        }
        String canonicalPath = virtualFile.getCanonicalPath();
        if (canonicalPath == null) {
            return;
        }
        @SystemIndependent String basePath = e.getProject().getBasePath();
        if (basePath == null) {
            return;
        }
        Path base = Paths.get(basePath);
        Path file = Paths.get(canonicalPath);
        Path relativePath = base.relativize(file);
        CopyPasteManager instance = CopyPasteManager.getInstance();
        if (instance == null) {
            return;
        }
        instance.setContents(new StringSelection(relativePath.toString()));
    }
}
