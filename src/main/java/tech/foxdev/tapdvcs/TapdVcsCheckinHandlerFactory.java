package tech.foxdev.tapdvcs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import org.jetbrains.annotations.NotNull;

public class TapdVcsCheckinHandlerFactory extends CheckinHandlerFactory {
    private static final Logger logger = Logger.getInstance(TapdVcsCheckinHandlerFactory.class);

    @Override
    public @NotNull CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {
        //打开提交面板时清空上一次遗留的数据
        VcsHandler.ClearData();
        TapdBugListDialog.TapdComment = "";
        return CheckinHandler.DUMMY;
    }
}
