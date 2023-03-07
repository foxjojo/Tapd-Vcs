package tech.foxdev.tapdvcs;

import com.intellij.openapi.vcs.CommitMessageI;
import com.intellij.openapi.vcs.VcsDataKeys;
import com.intellij.ui.dsl.builder.impl.Context;
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
        var values = bugList.getSelectedValuesList();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            var data = (TapdBugData) values.get(i);
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
        // add your code here if necessary
        dispose();
    }

    public static void main(CommitMessageI commitPanel) {

        TapdBugListDialog.commitPanel = commitPanel;
        TapdBugListDialog dialog = new TapdBugListDialog();
        dialog.pack();
        dialog.setVisible(true);

    }


    // 全局HttpClient:
    private static HttpClient httpClient = HttpClient.newBuilder().build();

    private void GetAllBug() {
        String url = "https://www.tapd.cn/my_worktable/todo_all/todo_all/61222066/todo";
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder(new URI(url))
                    // 设置Header:
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                    .header("cookie", "selected_workspace_tabexpiration_date=61222066; worktable_last_visit=bug; __root_domain_v=.tapd.cn; _qddaz=QD.406877066402714; lastSE=bing; tapdsession=1677991669f6be32ec9b95113f8983a999a6e8db4a89d6258adfed9ba507a611e76dab0336; _qdda=3-1.1ny1nc; _qddab=3-rk9opf.lev7dcjn; register_feed_uin=5reWuSlV6sBpisvDF3XqJnAswvT0Gfvj1678009390272; locale=zh_CN; sso-login-token=eda800de882b3e5088907c368c845cde; t_u=2a07cc0d1645147f637076645a1c764b98af0f5d300b194589c0e1a5c4d9a8e46f0eafaf44da1d1b2cbc57886f3e067eff52f37a84d8783984d7eafb9af6a2860647a332e0e5f08b|1; _t_uid=1170987776; _wt=eyJ1aWQiOiIxMTcwOTg3Nzc2IiwiY29tcGFueV9pZCI6IjIwNDkwMzkxIiwiZXhwIjoxNjc4MDA5NzkwfQ==.d3546cf8711456f36b2ccd0c0e5b5bce0b175ad962164b50266b0d90e92c1aaa; _t_crop=20490391; tapd_div=101_7; dsc-token=CQyw9StNC0cMdcAp; new_worktable=todo|61222066||normal_list")
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
            dispose();
            throw new RuntimeException(e);
        }
        // HTTP允许重复的Header，因此一个Header可对应多个Value:
        System.out.println(response.body());
        var doc = Jsoup.parse(response.body());
        var allBugs = doc.getElementsByClass("tfl-editable");
        final DefaultListModel bugs = new DefaultListModel();

        for (int i = 0; i < allBugs.size(); i++) {
            var bug = allBugs.get(i).getElementsByClass("card-title content-cardtitle namecol preview-title J-worktablePreview").first();
            var data = new TapdBugData();
            data.DisplayName = bug.attr("title");
            data.Url = bug.attr("href");
            bugs.addElement(data);
        }
        bugList.setModel(bugs);
    }


}
