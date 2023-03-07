package tech.foxdev.tapdvcs;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.openapi.vcs.VcsListener;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.VcsCheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.Refreshable;
import org.jetbrains.annotations.Nullable;

public class OpenTapdBugListAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        // TODO: insert action logic here

        final CommitMessageI commitPanel = getCommitPanel(actionEvent);
        if (commitPanel == null)
            return;
        SettingsState settings = SettingsState.getInstance();
        String id = settings.projectID;
        String cookie = settings.cookie;
        if (id.isEmpty()||cookie.isEmpty() ||id.length() == 0 || cookie.length() == 0)
        {
            new TipDialog(actionEvent.getProject(), "Please Set ProjectID and Cookie").show();
            return;
        }
        TapdBugListDialog.main(commitPanel, actionEvent.getProject());

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
