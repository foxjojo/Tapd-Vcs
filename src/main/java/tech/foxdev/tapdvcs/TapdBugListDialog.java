package tech.foxdev.tapdvcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CommitMessageI;
import org.apache.commons.lang.CharSet;
import org.jsoup.Jsoup;

import javax.swing.*;
import java.awt.event.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class TapdBugListDialog extends JDialog {
    public static String TapdComment;
    private JPanel contentPanel;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList bugList;
    private JTextArea comment;
    private JLabel loadTip;
    private JPanel main;

    private static CommitMessageI commitPanel;
    private static Project project;

    public TapdBugListDialog() {
        setContentPane(contentPanel);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        bugList.setCellRenderer(new CheckboxListCellRenderer());
        contentPanel.setSize(550, 150);
        contentPanel.updateUI();
        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        main.setVisible(false);
        loadTip.setVisible(true);
        GetAllBug();

    }

    private void onOK() {
        // add your code here
        VcsHandler.ClearData();
        var values = bugList.getSelectedValuesList();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            var data = (TapdBugData) values.get(i);
            VcsHandler.AddBug(data);
            stringBuilder.append('[');
            stringBuilder.append(data.DisplayName);
            stringBuilder.append(']');
            stringBuilder.append(data.Url);
            stringBuilder.append('\n');
        }
        commitPanel.setCommitMessage(stringBuilder.toString());
        TapdComment = comment.getText();
        dispose();
    }

    private void onCancel() {
        VcsHandler.ClearData();
        TapdComment = "";
        dispose();
    }

    public static void main(CommitMessageI commitPanel, Project project) {
        TapdBugListDialog.project = project;
        TapdBugListDialog.commitPanel = commitPanel;
        TapdBugListDialog dialog = new TapdBugListDialog();
        dialog.setTitle("Tapd-Vcs");
        dialog.pack();
        dialog.setVisible(true);

    }


    // 全局HttpClient:
    private static HttpClient httpClient = HttpClient.newBuilder().build();

    private void GetAllBug() {
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        String id = settings.projectID;
        String cookie = settings.cookie;
        String url = "https://www.tapd.cn/my_worktable/todo_all/todo_all/" + id + "/todo";
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder(new URI(url))
                    // 设置Header:
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                    .header("cookie", cookie)
                    // 设置超时:
                    .timeout(Duration.ofSeconds(5))
                    // 设置版本:
                    .version(HttpClient.Version.HTTP_2).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        CompletableFuture<HttpResponse<String>> response = null;
        response = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response.thenApply(HttpResponse::body).thenAccept((body) -> {
            // HTTP允许重复的Header，因此一个Header可对应多个Value:
            //System.out.println(response.body());
            var doc = Jsoup.parse(body);
            var allBugs = doc.getElementsByClass("tfl-editable");
            final DefaultListModel bugs = new DefaultListModel();

            var ownName = doc.getElementsByClass(" left-tree-brick nav-iconbtn dropdown user").first().getElementsByTag("a").first().attr("title");
            VcsHandler.ClearData();
            for (int i = 0; i < allBugs.size(); i++) {
                var itemID = allBugs.get(i).attr("data-item-id");
                String bugStatus = allBugs.get(i).getElementsByTag("td").get(3).text();
                var bug = allBugs.get(i).getElementsByClass("card-title content-cardtitle namecol preview-title J-worktablePreview").first();
                var data = new TapdBugData();
                data.DisplayName = bug.attr("title");
                data.Url = bug.attr("href");
                data.ID = bug.attr("data-entityid");
                String NewS = "\u65B0";
                String AccS = "\u63A5\u53D7/\u5904\u7406";
                String ReOpenS = "\u91CD\u65B0\u6253\u5F00";

                if (bugStatus.equals(NewS)) {
                    data.CurStatus = TapdBugData.Status.New;
                } else if (bugStatus.equals(AccS)) {
                    data.CurStatus = TapdBugData.Status.Accept;
                } else if (bugStatus.equals(ReOpenS)) {
                    data.CurStatus = TapdBugData.Status.ReOpen;
                }
                var nameItem = allBugs.get(i).getElementById("td_bug_reporter_" + data.ID);
                if (nameItem == null) {
                    continue;
                }
                var createName = nameItem.getElementsByTag("span").first().wholeText();
                data.CreateName = createName;
                data.OwnName = ownName;
                bugs.addElement(data);

            }

            bugList.setModel(bugs);
            main.setVisible(true);
            loadTip.setVisible(false);
        });


    }


}
