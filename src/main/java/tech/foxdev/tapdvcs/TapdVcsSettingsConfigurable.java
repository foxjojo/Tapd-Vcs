package tech.foxdev.tapdvcs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.jr.ob.JSON;
import com.google.gson.JsonObject;
import com.intellij.json.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBPanel;
import groovy.json.JsonBuilder;
import kotlinx.serialization.json.Json;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.io.JsonUtil;
import org.jsoup.Jsoup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TapdVcsSettingsConfigurable implements Configurable {

    private static final Logger LOG = Logger.getInstance(TapdVcsSettingsConfigurable.class);

    private JPanel myMainComponent;

    private JPanel loginPanel;
    private JLabel qr;
    private JLabel qrStatus;
    private JPanel projectPanel;
    private JLabel loginUserName;
    private JComboBox projects;
    private JButton logout;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return "Tapd-Vcs";
    }

    @Override
    public @Nullable JComponent createComponent() {
        if (myMainComponent == null) {

            myMainComponent = new JPanel();
            var setData = TapdVcsSettingsState.getInstance();
            projectPanel = new JPanel(new GridLayout(5, 1));
            loginUserName = new JLabel(setData.userName, JLabel.CENTER);
            projects = new ComboBox();
            logout = new JButton("Logout");
            logout.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginPanel.setVisible(true);
                    projectPanel.setVisible(false);
                    try {
                        GetQR();
                    } catch (JsonProcessingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            projectPanel.add(new JLabel("CurUser", JLabel.CENTER));
            projectPanel.add(loginUserName);
            projectPanel.add(new JLabel("CurProject", JLabel.CENTER));
            projectPanel.add(projects);
            projectPanel.add(logout);
            myMainComponent.add(projectPanel);


            loginPanel = new JPanel(new GridLayout(5, 1));
            qrStatus = new JLabel("Please use the WeCom (WeChat Work) mobile app to scan the QR code to login", JLabel.CENTER);
            qr = new JLabel();
            loginPanel.add(qr);
            loginPanel.add(qrStatus);

            myMainComponent.add(loginPanel);
            if (setData.cookieExpirationDate == 0 || setData.cookieExpirationDate < System.currentTimeMillis()) {
                loginPanel.setVisible(true);
                projectPanel.setVisible(false);
                try {
                    GetQR();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            } else {
                loginPanel.setVisible(false);
                projectPanel.setVisible(true);
                InitProjectPanel();
            }


        }

        return myMainComponent;
    }

    class Project {
        public String name;
        public String id;

        public Project(String name, String id) {
            this.name = name;
            this.id = id;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private void InitProjectPanel() {


        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
        String nowSelectID = settings.projectID;
        String cookie = settings.cookie;

        String url = "https://www.tapd.cn/company/participant_projects";
        Get(url, HttpResponse.BodyHandlers.ofString(), cookie).thenApply(HttpResponse::body).thenAccept((body) -> {
            int index = 0;
            DefaultComboBoxModel<Project> projectIDs = new DefaultComboBoxModel<Project>();
            var doc = Jsoup.parse((String) body);
            String userName = doc.getElementsByClass("dropdown-toggle").first().attr("title");
            loginUserName.setText(userName);
            var allProject = doc.getElementsByClass("project-list opened-project-list");
            for (int i = 0; i < allProject.size(); i++) {
                var id = allProject.get(i).getElementsByTag("a").attr("href").substring(20);
                var name = allProject.get(i).text();
                Project project = new Project(name, id);
                if (id.equals(nowSelectID)) {
                    index = i;
                }
                projectIDs.addElement(project);
            }
            projects.setModel(projectIDs);
            projects.setSelectedIndex(index);
        });

    }

    private static HttpClient httpClient = HttpClient.newBuilder().build();
    private String key;
    private String appid;

    private void GetQR() throws JsonProcessingException {
        String url = "https://open.work.weixin.qq.com/wwopen/sso/3rd_qrConnect?appid=wx4658ebb3e0764a89&redirect_uri=https%3A%2F%2Fwww.tapd.cn%2Fcloud_logins%2Fqy_login%3Fref%3Dhttps%253A%252F%252Fwww.tapd.cn%252Fmy_worktable&state=TAPD&usertype=member";
        Get(url, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept((body) -> {
            var doc = Jsoup.parse((String) body);
            var script = doc.getElementsByTag("script").get(3).data();
            var param = script.split("\n")[1].replaceAll("window.settings = ", "");
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = null;
            try {
                jsonNode = mapper.readTree(param);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            key = jsonNode.get("key").asText();
            appid = jsonNode.get("appid").asText();
            String qrUrl = "https:" + jsonNode.get("qrUrl").asText();
            Get(qrUrl, HttpResponse.BodyHandlers.ofByteArray()).thenApply(HttpResponse::body).thenAccept((imageData) -> {
                var img = new ImageIcon((byte[]) imageData);
                qr.setIcon(img);
            });

            CheckQrCallback(appid, key, false);
        });
    }

    private void CheckQrCallback(String appid, String key, boolean isScan) {
        String url = "https://open.work.weixin.qq.com/wwopen/sso/l/qrConnect?callback=jsonpCallback&key=" + key + "&redirect_uri=https%3A%2F%2Fwww.tapd.cn%2Fcloud_logins%2Fqy_login%3Fref%3Dhttps%253A%252F%252Fwww.tapd.cn%252Fmy_worktable&appid=" + appid + "&_=" + System.currentTimeMillis();
        if (isScan) {
            url += "&lastStatus=QRCODE_SCAN_ING";
        }
        Get(url, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept((body) -> {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = null;
            try {
                jsonNode = mapper.readTree(((String) body).substring(14).replace(')', ' '));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            String status = jsonNode.get("status").asText();
            if (isScan) {
                String auth_code = jsonNode.get("auth_code").asText();
                String reUrl = "https://www.tapd.cn/cloud_logins/qy_login?ref=https%3A%2F%2Fwww.tapd.cn%2Fmy_worktable&auth_code=" + auth_code + "&state=TAPD&appid=" + appid;
                Get(reUrl, HttpResponse.BodyHandlers.ofString()).thenAcceptAsync((response) -> {
                    var cookie = response.headers().allValues("set-cookie");
                    var setData = TapdVcsSettingsState.getInstance();
                    //tapdsession=168242677222a7e38377dcd8b8113a69b9f281d0688c4cf37f2ec156e07bb3956c5da82b90; expires=Tue, 02-May-2023 12:46:12 GMT; Max-Age=604800; path=/; domain=.tapd.cn; HttpOnly
                    var time = cookie.get(0).split(";")[1].substring(9).replace("GMT", "");

                    var date = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss ");
                    date.setDateFormatSymbols(new DateFormatSymbols(new Locale("en-US")));
                    String str = cookie.stream().collect(Collectors.joining(";"));
                    setData.cookie = str;
                    try {
                        setData.cookieExpirationDate = date.parse(time).getTime();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    loginPanel.setVisible(false);
                    projectPanel.setVisible(true);
                    InitProjectPanel();

                });
                return;
            }

            if (status.equals("QRCODE_SCAN_ING")) {
                CheckQrCallback(appid, key, true);
                qrStatus.setText("Scan Successful!  Please Confirm");
            } else {
                CheckQrCallback(appid, key, false);
            }
        });
    }

    private CompletableFuture<HttpResponse> Get(String url, HttpResponse.BodyHandler responseBodyHandler) {
        return Get(url, responseBodyHandler, "");
    }

    private CompletableFuture<HttpResponse> Get(String url, HttpResponse.BodyHandler responseBodyHandler, String cookie) {
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder(new URI(url))
                    // 设置Header:
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0 Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                    .header("referer", url)
                    .header("authority", "open.work.weixin.qq.com")
                    .header("cookie", cookie)
                    // 设置超时:
                    .timeout(Duration.ofSeconds(21))
                    // 设置版本:
                    .version(HttpClient.Version.HTTP_2).build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        CompletableFuture<HttpResponse> response = null;

        response = httpClient.sendAsync(request, responseBodyHandler);
        return response;
    }


    @Override
    public boolean isModified() {
        TapdVcsSettingsState settings = TapdVcsSettingsState.getInstance();
//        boolean modified = !myProjectIDField.getText().equals(settings.projectID);
//        modified |= !myCookieField.getText().equals(settings.cookie);
        return true;
    }

    @Override
    public void apply() {
        var setData = TapdVcsSettingsState.getInstance();
        setData.projectID = ((Project) projects.getSelectedItem()).id;
//        setData.projectID = myProjectIDField.getText();
//        setData.cookie = myCookieField.getText();
    }
}
