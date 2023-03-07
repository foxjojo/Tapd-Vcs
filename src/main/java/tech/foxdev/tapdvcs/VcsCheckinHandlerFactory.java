package tech.foxdev.tapdvcs;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

public class VcsCheckinHandlerFactory extends CheckinHandlerFactory {
    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        var check = new VcsCheckinHandler();
        check.panel = panel;
        check.commitContext = commitContext;
        return check;
        //return CheckinHandler.DUMMY;
    }
}
