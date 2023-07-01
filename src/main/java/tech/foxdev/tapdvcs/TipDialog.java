package tech.foxdev.tapdvcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class TipDialog extends DialogWrapper {
    private String desc;
    public TipDialog(Project project,String tip){
        super(project);
        desc = tip;
        init();
        setTitle("Tapd-Vcs");
    }
    @Override
    protected @Nullable JComponent createCenterPanel() {
        return new JBLabel(desc);
    }
}
