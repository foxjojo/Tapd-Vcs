package tech.foxdev.tapdvcs;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.jr.ob.JSON;
import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.httpclient.Cookie;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.regex.Matcher;

public class TapdTaskProcess extends JDialog {
    private static String comment;

    private enum Status {
        InProcess,
        FixedAndAddComment
    }

    private static final Logger logger = Logger.getInstance(TapdTaskProcess.class);

    private JPanel contentPane;
    private JProgressBar progressBar;
    private JButton cancel;
    private JLabel statusDesc;
    private ArrayList<TapdBugData> bugIDs = new ArrayList<>();

    private Thread process;
    private boolean stop = false;

    public TapdTaskProcess(ArrayList<TapdBugData> bugIDs) {
        this.bugIDs = bugIDs;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancel);

        cancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        if (bugIDs.size() > 0) {
            process = new Thread(() -> {
                progressBar.setMinimum(0);
                progressBar.setMaximum(bugIDs.size() * 2);
                for (int i = 0; i < bugIDs.size(); i++) {
                    if (stop)
                        break;

                    String displayName = bugIDs.get(i).DisplayName;
                    progressBar.setValue(i * 2);
                    statusDesc.setText("Change Bug ID:" + displayName + " To InProcess");
                    BuildUrl(Status.InProcess, bugIDs.get(i));
                    statusDesc.setText("Change Bug ID:" + displayName + "To Fixed And Add Comment");
                    progressBar.setValue(i * 2 + 1);
                    BuildUrl(Status.FixedAndAddComment, bugIDs.get(i));

                    logger.debug("Bug ID:" + displayName);
                }
                dispose();

            });
            stop = false;
            process.start();
        }


    }

    private void onCancel() {
        // add your code here if necessary
        if (process != null) {
            stop = true;
        } else {
            dispose();
        }

    }

    public static void main(ArrayList<TapdBugData> bugIDs, String comment) {
        TapdTaskProcess.comment = comment;
        TapdTaskProcess dialog = new TapdTaskProcess(bugIDs);
        dialog.pack();
        dialog.setVisible(true);

    }

    private static final HttpClient httpClient = HttpClient.newBuilder().build();

    private boolean Send(String url, String postData) {
        System.out.println(url);
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        String cookie = settings.cookie;

        HttpRequest request = null;
        try {
            if (postData.isEmpty()) {
                request = HttpRequest.newBuilder(new URI(url))
                        // 设置Header:
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                        .header("cookie", cookie)
                        // 设置超时:
                        .timeout(Duration.ofSeconds(5))
                        // 设置版本:
                        .version(HttpClient.Version.HTTP_2).build();

            } else {
                request = HttpRequest.newBuilder(new URI(url))
                        // 设置Header:
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                        .header("cookie", cookie)
                        .POST(HttpRequest.BodyPublishers.ofString(postData))
                        // 设置超时:
                        .timeout(Duration.ofSeconds(5))
                        // 设置版本:
                        .version(HttpClient.Version.HTTP_2).build();
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {

            //new TipDialog(project, "Network Error").show();
            return false;
        }
        // HTTP允许重复的Header，因此一个Header可对应多个Value:
        System.out.println(response.statusCode());
        System.out.println(response.body());
        return true;
    }

    private String BuildPostData(TapdBugData tapdBugData, String curStatus, String newStatus, String own, String create, String comment) {
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        String projectID = settings.projectID;

        String json = "";
        try {
            json = JSON.std
                    .with(JSON.Feature.PRETTY_PRINT_OUTPUT)
                    .composeString()
                    .startObject()
                    .put("workspace_id", projectID)
                    .startObjectField("data")
                    .startObjectField("Bug")
                    .put("current_status", curStatus)
                    .put("id", tapdBugData.ID)
                    .put("complete_effort", false)
                    .end()
                    .put("new_status", newStatus)
                    .startObjectField("Comment")
                    .put("description", comment)
                    .end()
                    .startObjectField("branch")
                    .end()
                    .startObjectField("STATUS_" + curStatus + "-" + newStatus)
                    .put("current_owner", own)
                    .put("te", create)
                    .put("remarks", "")
                    .end()
                    .end()
                    .put("dsc_token", settings.decToken)
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }


    private void BuildUrl(Status status, TapdBugData tapdBugData) {

        String url = "https://www.tapd.cn/api/entity/workflow/change_bug_status";
        switch (status) {
            case InProcess:

                Send(url, BuildPostData(tapdBugData, "new", "in_progress", tapdBugData.OwnName, tapdBugData.OwnName, ""));
                break;
            case FixedAndAddComment:
                String data = BuildPostData(tapdBugData,"in_progress", "resolved", tapdBugData.CreateName, tapdBugData.CreateName, comment);
                Send(url, data);
                break;
        }

    }
}
