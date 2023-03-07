package tech.foxdev.tapdvcs;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.GridBag;
import com.intellij.util.ui.JBDimension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.InputMethodListener;

public class SettingsConfigurable implements Configurable {

    private static final Logger LOG = Logger.getInstance(SettingsConfigurable.class);

    private JPanel myMainComponent;
    private JTextField myProjectIDField;
    private JTextArea myCookieField;


    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return null;
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (myMainComponent == null) {
            myMainComponent = new JPanel();
            var setData = SettingsState.getInstance();

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
        SettingsState settings = SettingsState.getInstance();
        boolean modified = !myProjectIDField.getText().equals(settings.projectID);
        modified |= myCookieField.getText() != settings.cookie;
        return modified;
    }

    @Override
    public void apply() throws ConfigurationException {
        var setData = SettingsState.getInstance();
        setData.projectID = myProjectIDField.getText();
        setData.cookie = myCookieField.getText();

    }
}
