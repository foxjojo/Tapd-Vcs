package tech.foxdev.tapdvcs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.HttpCookie;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@State(
        name = "tech.foxdev.tapdvcs.SettingsState",
        storages = @Storage("TapdVcsSettingsPlugin.xml")
)
public class TapdVcsSettingsState implements PersistentStateComponent<TapdVcsSettingsState> {
    public String projectID;
    public String cookie;
    public String decToken;

    public static TapdVcsSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(TapdVcsSettingsState.class);
    }

    @Override
    public @Nullable TapdVcsSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull TapdVcsSettingsState state) {

        XmlSerializerUtil.copyBean(state, this);
        var t = Arrays.stream(cookie.split(";")).iterator();
        while (t.hasNext()) {
            var a = t.next();
            if (a.split("=")[0].trim().equals("dsc-token")) {
                decToken = a.split("=")[1].trim();
            }
        }

    }
}
