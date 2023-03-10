package tech.foxdev.tapdvcs;

import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.VcsNotificationIdsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class VcsHandler implements Notifications {


    private static final Logger logger = Logger.getInstance(VcsHandler.class);

    private static ArrayList<TapdBugData> bugIDs = new ArrayList<>();
    public static String CommitVersion;
    @Override
    public void notify(@NotNull final Notification notification) {

        if (VcsNotificationIdsHolder.COMMIT_FINISHED.equals(notification.getDisplayId())) {

            var strArray = notification.getContent().split("<br/>");
            CommitVersion=strArray[strArray.length - 1];
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
