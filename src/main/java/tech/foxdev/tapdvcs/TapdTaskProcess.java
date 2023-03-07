package tech.foxdev.tapdvcs;

import com.intellij.openapi.diagnostic.Logger;

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
import java.util.Timer;

public class TapdTaskProcess extends JDialog {
    private enum Status {
        InProcess,
        Fixed
    }

    private static final Logger logger = Logger.getInstance(TapdTaskProcess.class);

    private JPanel contentPane;
    private JProgressBar progressBar;
    private JButton cancel;
    private JLabel statusDesc;
    private ArrayList<TapdBugData> bugIDs = new ArrayList<>();

    private Thread process;

    public TapdTaskProcess(ArrayList<TapdBugData> bugIDs) {
        this.bugIDs = bugIDs;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(cancel);

        cancel.addActionListener(new ActionListener() {
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
        if (bugIDs.size() > 0) {
            process = new Thread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMinimum(0);
                    progressBar.setMaximum(bugIDs.size() * 2);
                    for (int i = 0; i < bugIDs.size(); i++) {
                        String bugID = bugIDs.get(i).ID;
                        progressBar.setValue(i * 2);
                        statusDesc.setText("Change Bug ID:" + bugID + " To InProcess");
                        Send(BuildUrl(Status.InProcess, bugID));
                        statusDesc.setText("Change Bug ID:" + bugID + " To Fixed");
                        progressBar.setValue(i * 2 + 1);
                        Send(BuildUrl(Status.Fixed, bugID));
                        logger.debug("Bug ID:" + bugID);
                    }
                    dispose();

                }
            });
            process.start();
        }


    }

    private void onCancel() {
        // add your code here if necessary
        if (process != null) {
            //todo 这样不安全等后续修改
            // process.stop();
        }
        dispose();
    }

    public static void main(ArrayList<TapdBugData> bugIDs) {

        TapdTaskProcess dialog = new TapdTaskProcess(bugIDs);
        dialog.pack();
        dialog.setVisible(true);

    }

    private static HttpClient httpClient = HttpClient.newBuilder().build();

    private boolean Send(String url) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
//        SettingsState settings = SettingsState.getInstance();
//        String cookie = settings.cookie;
//        HttpRequest request = null;
//        try {
//            request = HttpRequest.newBuilder(new URI(url))
//                    // 设置Header:
//                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
//                    .header("cookie", cookie)
//                    // 设置超时:
//                    .timeout(Duration.ofSeconds(5))
//                    // 设置版本:
//                    .version(HttpClient.Version.HTTP_2).build();
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        HttpResponse<String> response = null;
//        try {
//            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        } catch (IOException e) {
//
//            return false;
//        } catch (InterruptedException e) {
//
//
//            //new TipDialog(project, "Network Error").show();
//
//            return false;
//        }
//        // HTTP允许重复的Header，因此一个Header可对应多个Value:
//        System.out.println(response.statusCode());
//        System.out.println(response.body());
//        return true;
    }


    private String BuildUrl(Status status, String bugID) {
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        String id = settings.projectID;
        switch (status) {
            case InProcess:
                return "https://www.tapd.cn/" + id + "/workflows/check_condition?entity_id=" + bugID + "&entity_type=bug&to_status=in_progress&current_status=new&no_cache=0&async=0";

            case Fixed:
                return "https://www.tapd.cn/" + id + "/workflows/check_condition?entity_id=" + bugID + "&entity_type=bug&to_status=in_progress&current_status=new&no_cache=0&async=0";

        }
        return "";
    }
}
