import com.intellij.codeInsight.hint.HintManager;
import com.intellij.diff.DiffContentFactory;
import com.intellij.diff.DiffManager;
import com.intellij.diff.contents.DocumentContent;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MulticursorDiffAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        final Project project = e.getProject();
        List<Caret> carets = editor.getCaretModel().getAllCarets();
        if (carets.size() < 2) {
            HintManager.getInstance().showInformationHint(editor, "Not enough carets");
        }
        Caret firstCaret = carets.get(0);
        Caret secondCaret = carets.get(1);
        String firstSelectedText = firstCaret.getSelectedText();
        String secondSelectedText = secondCaret.getSelectedText();
        if (firstSelectedText == null || secondSelectedText == null) {
            return;
        }
        DocumentContent content1 = DiffContentFactory.getInstance().create(firstSelectedText);
        DocumentContent content2 = DiffContentFactory.getInstance().create(secondSelectedText);
        SimpleDiffRequest request = new SimpleDiffRequest("Window Title", content1, content2, "First caret", "Second caret");
        DiffManager.getInstance().showDiff(project, request);
    }
}
