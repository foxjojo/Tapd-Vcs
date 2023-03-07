package tech.foxdev.tapdvcs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.util.NlsContexts;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TapdVcsSettingsConfigurable implements Configurable {

    private static final Logger LOG = Logger.getInstance(TapdVcsSettingsConfigurable.class);

    private JPanel myMainComponent;
    private JTextField myProjectIDField;
    private JTextArea myCookieField;


    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Tapd-Vcs";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (myMainComponent == null) {
            myMainComponent = new JPanel();
            var setData = TapdVcsSettingsState.getInstance();

            myMainComponent.setLayout(new GridLayout(4, 1));

            var myProjectIDText = new JLabel("Project ID:");
            myProjectIDField = new JTextField(setData.projectID);
            var myCookieText = new JLabel("Cookie:");
            myCookieField = new JTextArea(setData.cookie);
            myCookieField.setColumns(6);
            myCookieField.setLineWrap(true);
            myMainComponent.add(myProjectIDText);
            myMainComponent.add(myProjectIDField);
            myMainComponent.add(myCookieText);
            myMainComponent.add(myCookieField);


        }

        return myMainComponent;
    }

    @Override
    public boolean isModified() {
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        boolean modified = !myProjectIDField.getText().equals(settings.projectID);
        modified |= !myCookieField.getText().equals(settings.cookie);
        return modified;
    }

    @Override
    public void apply()  {
        var setData = TapdVcsSettingsState.getInstance();
        setData.projectID = myProjectIDField.getText();
        setData.cookie = myCookieField.getText();
    }
}
