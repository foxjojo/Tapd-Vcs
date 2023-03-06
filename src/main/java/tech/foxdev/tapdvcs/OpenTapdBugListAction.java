package tech.foxdev.tapdvcs;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class OpenTapdBugListAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        if(e!=null){
            TapdBugListDialog.main(null);
        }
    }
}
