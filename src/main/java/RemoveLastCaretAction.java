import com.intellij.codeInsight.hint.HintManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.CaretState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveLastCaretAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        final Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return;
        }
        final Project project = e.getProject();
        List<CaretState> carets = editor.getCaretModel().getCaretsAndSelections();

        if (carets.size() < 2) {
            HintManager.getInstance().showInformationHint(editor, "Not enough carets");
            return;
        }
        carets.remove(carets.size()  - 1);
        editor.getCaretModel().setCaretsAndSelections(carets);
    }
}
