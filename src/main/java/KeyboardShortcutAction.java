import com.intellij.ide.ui.search.SearchUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurableGroup;
import com.intellij.openapi.options.ex.ConfigurableExtensionPointUtil;
import com.intellij.openapi.options.newEditor.SettingsDialogFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class KeyboardShortcutAction extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        ConfigurableGroup group = ConfigurableExtensionPointUtil
                                          .getConfigurableGroup(project, /* withIdeSettings = */ true);
        List<ConfigurableGroup> groups = group.getConfigurables().length == 0 ? Collections.emptyList() : Collections.singletonList(group);
        Configurable preselectedByHelpTopic = findPreselectedByHelpTopic("preferences.keymap", groups);
        DialogWrapper dialogWrapper = SettingsDialogFactory.getInstance()
                .create(project, groups, preselectedByHelpTopic, "");
        dialogWrapper.show();
        dialogWrapper.showAndGet();
       System.out.println("...");
        /*ShowSettingsUtil instance = ShowSettingsUtil.getInstance();
        // instance.showSettingsDialog(project, "Keymap");
        instance.showSettingsDialog(project, KeymapPanel.class, p -> {
            p.enableSearch("Action");
            // p.processCurrentKeymapChanged();
            // System.out.println(p);
        });*/
/*        ConfigurableGroup group = ConfigurableExtensionPointUtil
                .getConfigurableGroup(project, *//* withIdeSettings = *//* true);
        List<ConfigurableGroup> groups = group.getConfigurables().length == 0 ? Collections.emptyList() : Collections.singletonList(group);
        Configurable preselectedByHelpTopic = findPreselectedByHelpTopic("preferences.keymap", groups);
        System.out.println(preselectedByHelpTopic);*/
    }


    @Nullable
    private static Configurable findPreselectedByHelpTopic(@NotNull String key, @NotNull List<? extends ConfigurableGroup> groups) {
        for (ConfigurableGroup eachGroup : groups) {
            for (Configurable configurable : SearchUtil.expandGroup(eachGroup)) {
                if (key.equals(configurable.getHelpTopic())) {
                    return configurable;
                }
            }
        }
        return null;
    }
}