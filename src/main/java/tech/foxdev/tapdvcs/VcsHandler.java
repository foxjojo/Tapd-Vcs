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
            logger.debug(strArray[strArray.length - 1]);
            if (bugIDs.size() > 0)
                TapdTaskProcess.main(bugIDs, strArray[strArray.length - 1]);
        }

    }

    public static void ClearData() {
        bugIDs.clear();
        logger.info("ClearData");
    }

    public static void AddBug(TapdBugData data) {

        bugIDs.add(data);
        logger.info("AddData"+data.DisplayName);
    }


}
