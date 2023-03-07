package tech.foxdev.tapdvcs;

import com.intellij.diff.DiffManager;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.*;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.history.CurrentRevision;
import com.intellij.openapi.vcs.history.VcsFileRevision;

import java.lang.reflect.Array;
import java.util.List;
import java.util.function.Consumer;

public class VcsCheckinHandler extends CheckinHandler {
    public CheckinProjectPanel panel;
    public CommitContext commitContext;
    @Override
    public void checkinSuccessful() {


     var t=  ProjectLevelVcsManager.getInstance(panel.getProject()).findVersioningVcs(panel.getProject().getProjectFile()).getRevisionPattern();
        System.out.println(panel.getCommitMessage());
    }
}
