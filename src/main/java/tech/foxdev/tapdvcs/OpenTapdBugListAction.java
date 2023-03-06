package tech.foxdev.tapdvcs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.Nullable;

public class OpenTapdBugListAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        // TODO: insert action logic here
        final CommitMessageI commitPanel = getCommitPanel(actionEvent);
        if (commitPanel == null)
            return;
        TapdBugListDialog.main(commitPanel);
    }
    @Nullable
    private static CommitMessageI getCommitPanel(@Nullable AnActionEvent e) {
        if (e == null) {
            return null;
        }
        Refreshable data = Refreshable.PANEL_KEY.getData(e.getDataContext());
        if (data instanceof CommitMessageI) {
            return (CommitMessageI) data;
        }
        return VcsDataKeys.COMMIT_MESSAGE_CONTROL.getData(e.getDataContext());
    }
}
