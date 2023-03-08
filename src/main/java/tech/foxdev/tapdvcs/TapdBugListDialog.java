package tech.foxdev.tapdvcs;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.ui.popup.PopupComponent;
import com.intellij.util.messages.MessageBus;
import org.jsoup.Jsoup;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TapdBugListDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JList bugList;

    private static CommitMessageI commitPanel;
    private static Project project;

    public TapdBugListDialog() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        bugList.setCellRenderer(new CheckboxListCellRenderer());

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
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

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
        dispose();
    }

    private void onCancel() {
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
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            new TipDialog(project, "Network Error").show();
            dispose();
            throw new RuntimeException(e);
        }
        // HTTP允许重复的Header，因此一个Header可对应多个Value:
        //System.out.println(response.body());
        var doc = Jsoup.parse(response.body());
        var allBugs = doc.getElementsByClass("tfl-editable");
        final DefaultListModel bugs = new DefaultListModel();

        var ownName = doc.getElementsByClass(" left-tree-brick nav-iconbtn dropdown user").first().getElementsByTag("a").first().attr("title");
        VcsHandler.ClearData();
        for (int i = 0; i < allBugs.size(); i++) {
            var bug = allBugs.get(i).getElementsByClass("card-title content-cardtitle namecol preview-title J-worktablePreview").first();
            var data = new TapdBugData();
            data.DisplayName = bug.attr("title");
            data.Url = bug.attr("href");
            data.ID = bug.attr("data-entityid");
            var createName = allBugs.get(i).getElementById("td_bug_reporter_" + data.ID).getElementsByTag("span").first().wholeText();
            data.CreateName = createName;
            data.OwnName = ownName;
            bugs.addElement(data);

        }

        bugList.setModel(bugs);
    }


}
