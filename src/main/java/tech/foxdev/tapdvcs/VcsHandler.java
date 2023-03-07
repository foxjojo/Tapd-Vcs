package tech.foxdev.tapdvcs;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;

public class VcsHandler implements Notifications {


    private static final Logger logger = Logger.getInstance(VcsHandler.class);

    private static ArrayList<TapdBugData> bugIDs = new ArrayList<>();

    @Override
    public void notify(@NotNull final Notification notification) {

        if ("Vcs Messages".equals(notification.getGroupId()) && "vcs.commit.finished".equals(notification.getDisplayId())) {

            var strArray = notification.getContent().split("<br/>");
            System.out.println(strArray[1]);
            logger.debug(strArray[1]);

            TapdTaskProcess.main(bugIDs);
        }

    }

    public static void ClearData() {
        bugIDs.clear();
    }

    public static void AddBug(TapdBugData data) {
        bugIDs.add(data);
    }


}
