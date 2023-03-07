package tech.foxdev.tapdvcs;

import com.intellij.openapi.vcs.changes.committed.CommittedChangeListsListener;
import com.intellij.openapi.vcs.changes.committed.CommittedChangesListener;
import com.intellij.openapi.vcs.versionBrowser.CommittedChangeList;
import org.jetbrains.annotations.NotNull;

public class CommittedListener implements CommittedChangeListsListener {

    @Override
    public void onBeforeStartReport() {

    }

    @Override
    public boolean report(@NotNull CommittedChangeList list) {
        return false;
    }

    @Override
    public void onAfterEndReport() {

    }
}
