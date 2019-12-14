package com.android.kk.redmik20tomi9t;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.textclassifier.TextClassifier;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {

    public enum State {
        INIT,
        ACTION,
        REBOOT,
        ERROR,
    }
    public class OutPutText {
        String text;
    }

    public static final String TAG = "kklog";

    public static final String REDMI_K_20 = "'Redmi K20'";
    public static final String MOUNT_RW = "mount -o remount -rw /";
    public static final String MOUNT_VENDOR_RW = "mount -o remount -rw /vendor";
    public static final String MOUNT_RO = "mount -o remount -r /";
    public static final String MOUNT_VENDOR_RO = "mount -o remount -r /vendor";

    public static final String BOOTLOGO = "/dev/block/sde46";
    public static final String SYSTEM_MEDIA = "/system/media/";
    public static final String BOOTANIMATION_ZIP = "bootanimation.zip";
    public static final String BOOTANIM_PATH = SYSTEM_MEDIA + BOOTANIMATION_ZIP;
    public static final String SAVE = ".save";

    public static final String HTMLB = "<html><body>\n";
    public static final String HTMLE = "</body></html>";
    public static final String MIME = "text/html";
    public static final String ENCODING = "utf-8";

    public static final String BLUE_SPACE = " ";    // üres sor a parancsok kimenete után, logfájlba nem kell

    public static final int BUFFER = 2048;

//    public static final String PREF = "/sdcard/redmik20tomi9t"; //////////////// csak tesztelésre !!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static final String PREF = "";

    private LinearLayout screen;
    private LinearLayout checkBoxes;
    private CheckBox buildProp;
    private CheckBox bootLogo;
    private CheckBox bootAnimation;
    private CheckBox watermark;
    private CheckBox bin;
    private LinearLayout ll;
    private LinearLayout rw;
    private LinearLayout ro;
    private ScrollView scroll;
    private OutPutText outPutText = new OutPutText();
    private LinearLayout outPut;
    private State state = State.INIT;
    private String filesDir;
    private String bootLogoDir;
    private String logFilePath;
    private FileWriter logWriter;
    private String errorMsg;
    private String newBootAnimPath;
    private String sha256sumOrig;
    private String mi9t;
    private String davinciWebp;
    private String davinciInWebp;
    private String davinciGlobalWebp;
    private String cameraDir;
    private String cameraApk;
    private String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            init();
        } catch (Exception e) {
            error(e);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        scrollDown();
    }

    private void init() throws IOException, InterruptedException {
        setContentView(R.layout.activity_main);
        this.screen = this.findViewById(R.id.screen);
        this.bootLogo = this.findViewById(R.id.checkbox_bootlogo);
        this.bootAnimation = this.findViewById(R.id.checkbox_bootanimation);
        this.buildProp = this.findViewById(R.id.checkbox_buildprop);
        this.watermark = this.findViewById(R.id.checkbox_watermark);
        this.bin = this.findViewById(R.id.checkbox_bin);
        this.ll = this.findViewById(R.id.ll);
        this.rw = this.findViewById(R.id.rw);
        this.ro = this.findViewById(R.id.ro);
        this.checkBoxes = this.findViewById(R.id.checkBoxes);
        this.checkBoxes.removeAllViews();
        this.outPut = this.findViewById(R.id.output);
        this.scroll = this.findViewById(R.id.scroll_view);

        this.filesDir = getFilesDir ().getPath() + "/";
        this.outPutText.text = "";
        final String logDir = filesDir + "log/";
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String date = formatter.format(new Date());
        logFilePath = logDir + date + ".html";
        File logFile = new File (logFilePath);
        this.logWriter = new FileWriter(logFile);
        logWriter.write(HTMLB);
        try {
            this.user = cmd("sh", "whoami", false, false).replace("\n", "");
            cmd("mkdir -m 777 " + logDir, true);
            cmd("chown " + this.user + ":" + this.user + " " + logDir);
        } catch (Exception e) {
            error(e);
            return;
        }
        try  {
            String suRet = cmd("whoami");
        }
        catch (Exception e) {
            error(e);
            return;
        }
        this.cameraDir = this.filesDir + "Camera";
        cmd("mkdir -m 777 " + cameraDir, true);
        cmd("chown " + this.user + ":" + this.user + " " + cameraDir);
        File toZip = new File(this.cameraDir);
        final File zipfile = zipFolder(toZip);
        Log.d(TAG, "zipfile: " +zipfile.getAbsolutePath());
        InputStream rawBootLogo = getResources().openRawResource(R.raw.bootlogo9t);
        unZip(rawBootLogo, filesDir);
        bootLogoDir = filesDir + "LogoOrig/";
        String bootLogosha2 = bootLogoDir + "logo.emmc.win.sha2";
        String sha256sumPart = getSha256sum(BOOTLOGO);
        this.sha256sumOrig = cmd("cat " + bootLogosha2).split(" ")[0];

        this.newBootAnimPath = this.filesDir + BOOTANIMATION_ZIP;
        String sha256sumOld = getSha256sum(BOOTANIM_PATH);
        String sha256sumNew = getSha256sum(this.newBootAnimPath);

        this.cameraApk = "/system/priv-app/MiuiCamera/MiuiCamera.apk";
        String cameraApkTemp = this.filesDir + "MiuiCamera.apk";
        cmd("cp " + cameraApk + " " + cameraApkTemp);
        cmd("chown " + this.user + ":" + this.user + " " + cameraApkTemp);
        InputStream is = new FileInputStream(new File(cameraApkTemp));
        unZip(is, cameraDir);
        this.mi9t = getRes(R.raw.mi9t, ".webp");
        this.davinciWebp = cameraDir + "/assets/watermarks/davinci.webp ";
        this.davinciInWebp = cameraDir + "/assets/watermarks/davinciin.webp ";
        this.davinciGlobalWebp = cameraDir + "/assets/watermarks/davinci_global.webp ";
        String sha256sumMi9tWebp = getSha256sum(this.filesDir + this.mi9t);
        String sha256sumDavinciWebp = getSha256sum(this.davinciWebp);
        String sha256sumDavinciInWebp = getSha256sum(this.davinciInWebp);
        String sha256sumDavinciGlobalWebp = getSha256sum(davinciGlobalWebp);

        this.checkBoxes.addView(this.buildProp);
        if (!sha256sumOld.equals(sha256sumNew)) {   // nem a jó bootanimáció van
            doSetChecked(this.bootAnimation, true);
        }
        if (!sha256sumPart.equals(sha256sumOrig)) { // a partíció nem az új logo-t tartalmazza
            doSetChecked(this.bootLogo, true);
        }
        if (!sha256sumMi9tWebp.equals(sha256sumDavinciGlobalWebp)
         || !sha256sumMi9tWebp.equals(sha256sumDavinciInWebp)
         || !sha256sumMi9tWebp.equals(sha256sumDavinciWebp)) {
            doSetChecked(this.watermark, true);
        }
        String binLl = "/bin/ll";
        final String lsLl = cmd("ls " + binLl, true);
        if (!lsLl.equals(binLl + "\n")) {
            this.checkBoxes.addView(this.bin);
            this.checkBoxes.addView(this.ll);
            this.checkBoxes.addView(this.rw);
            this.checkBoxes.addView(this.ro);
        }
    }

    private String getSha256sum(String name) throws IOException, InterruptedException {
        return cmd("sha256sum " + name).split(" ")[0];
    }

    private void error(Exception e) {
        e.printStackTrace();
        errorMsg = e.getMessage();
        appendOutPut(errorMsg, Color.RED);
        state = State.ERROR;
        Button cancel = findViewById(R.id.btnCancel);
        doSetVisibility(findViewById(R.id.btnSubmit), View.INVISIBLE);
        doSetVisibility(cancel, View.VISIBLE);
        doSetText(cancel, "kilép");
    }

    public void onClickSubmit(final View view) throws Exception {
        Log.d(TAG, "onClickSubmit: " + view.getTransitionName());
        switch (state) {
            case ACTION:
                return;

            case REBOOT:
                cmd("reboot", true, true);
                return;
        }
        new Thread() {  // külön thread-ben a feldolgozás, hogy az UI azonnal frissülhessen
            @Override
            public void run() {
                try {
                    doChange(view);
                } catch (Exception e) {
                    error(e);
                }
            }
        }.start();
    }

    private void doChange(View view) throws IOException, InterruptedException {
        state = State.ACTION;
        doSetVisibility(view, View.INVISIBLE);
        Button cancel = this.findViewById(R.id.btnCancel);
        doSetVisibility(cancel, View.INVISIBLE);
        String ret;
        boolean reboot = false;
        boolean rw = false;
        try {
            cmd(MOUNT_RW);
            cmd(MOUNT_VENDOR_RW);
            rw = true;

            if (buildProp.isChecked()){
                EditText newTextField = findViewById(R.id.newText);
                String newText = newTextField.getText().toString();
//                findReplace(newText, "", "*");
                findReplace(newText, "-r", "system/");
                reboot = true;
            }

            if (bootLogo.isChecked()){
                String bootLogoPath = bootLogoDir + "logo.emmc.win";
                String sha256sum = getSha256sum(bootLogoPath);
                if (sha256sum.equals(sha256sumOrig)) {
                    String fileLength = cmd("ls -l " + bootLogoPath).split(" ")[4];
                    String blockLength = cmd("blockdev --getsize64 " + BOOTLOGO).replace("\n", "");
                    if (fileLength.equals(blockLength)) {
                        ret = cmd("dd if=" + bootLogoPath + " of=" + PREF + BOOTLOGO);
                        reboot = true;
                    }
                    else {
                        errorMsg = "bootlogo fájl hossza " + fileLength + " és " + BOOTLOGO + " hossza " + blockLength + " nem egyezik";
                        appendOutPut(errorMsg, Color.RED);
                        throw new RuntimeException(errorMsg);
                    }
                }
                else {
                    errorMsg = "hibás bootlogo";
                    appendOutPut(errorMsg, Color.RED);
                    throw new RuntimeException(errorMsg);
                }
            }
            if (state == State.ERROR) {
                return;
            }

            if (bootAnimation.isChecked()){
                saveFile(BOOTANIM_PATH);
                ret = cmd("cp " + newBootAnimPath + " " + BOOTANIM_PATH);
                reboot = true;
            }

            if (watermark.isChecked()){
                saveFile(cameraApk);
                ret = cmd("cp " + this.filesDir + this.mi9t + " " + this.davinciGlobalWebp); //a kicsomagolt MiuiCamera.apk-ban davinci_global.webp felülírása
                ret = cmd("cp " + this.filesDir + this.mi9t + " " + this.davinciWebp); //a kicsomagolt MiuiCamera.apk-ban davinci.webp felülírása
                ret = cmd("cp " + this.filesDir + this.mi9t + " " + this.davinciInWebp); //a kicsomagolt MiuiCamera.apk-ban davinciin.webp felülírása
                File cameraApkMod = zipFolder(new File(cameraDir));
                ret = cmd("cp " + cameraApkMod + " " + cameraApk);  // a MiuiCamera.apk felülírása

                String watermarkFileName = getRes(R.raw.davinci_main_space_custom_watermark, ".png");
                String watermarkPath = "/data/data/com.android.camera/files/" + watermarkFileName;
                saveFile(watermarkPath);
                ret = cmd("cp " + this.filesDir + watermarkFileName + " " + watermarkPath);
            }

            if (bin.isChecked()){
                addCmd("ls -l  \"$@\"", "ll");
                addCmd(MOUNT_RW, "rw");
                addCmd(MOUNT_RO, "ro");
            }

            state = State.REBOOT;
        }
        catch (Exception e) {
            error(e);
        }
        finally {
            if (rw) {
                Thread.sleep(3000);
                ret = cmd(MOUNT_VENDOR_RO);
                ret = cmd(MOUNT_RO);
            }
        }

        switch (state) {
            case REBOOT:
                appendOutPut("\nhibát nem találtam, " + (reboot? "jöhet egy reboot": "kiléphetsz"), Color.GREEN);
                appendOutPut(" ", Color.GREEN);
                if (reboot) {
                    doSetVisibility(view, View.VISIBLE);
                    doSetText((Button) view, "reboot");
                }
                break;

            case ERROR:
                appendOutPut("\nPorszem került a gépezetbe: " + errorMsg, Color.RED);
                break;
        }
        doSetVisibility(cancel, View.VISIBLE);
        doSetText(cancel, "kilép");
    }

    private String getRes(int res, String typ) throws IOException {
        String ret;
        InputStream rawWatermark = getResources().openRawResource(res);
        String watermarkFileName = getResources().getResourceEntryName(res) + typ;
        String watermarkTempFilePath = this.filesDir + watermarkFileName;
        writeFile(rawWatermark, watermarkTempFilePath);
        return watermarkFileName;
    }

    private void addCmd(String oldCmd, String newCmd) throws IOException, InterruptedException {
        String ret;
        final String complCmd = "/bin/" + newCmd;
        ret = cmd("echo '#!/bin/sh\n" +
                oldCmd + "' > " + complCmd);
        ret = cmd("chown root:shell " + complCmd);
        ret = cmd("chmod 755  " + complCmd);
    }

    private boolean saveFile(String file) throws IOException, InterruptedException {
        String ret = cmd("ls " + file + "*");
        String fileSave = file + SAVE;
        final boolean saved = ret.contains(fileSave);
        if (!saved) { // a mentést nem írjuk felül
            ret = cmd("cp " + file + " " + fileSave);
        }
        return saved;
    }

    private void findReplace(String newText, String rekursiv, String system) throws IOException, InterruptedException {
        String ret = cmd("grep " + rekursiv + " " + REDMI_K_20 + " /" + system + " 2>/dev/null|grep ^/");
        final String[] split = ret.split("\n");
        for (String line: split) {
            String buildProp = line.split(":")[0];
            if (buildProp.endsWith(SAVE)) {
                buildProp = buildProp.replace(SAVE, "");
            }
            else {
                final boolean saved = saveFile(buildProp);
            }
            ret = cmd("cat " + buildProp + SAVE +
                    "|sed s#" + REDMI_K_20 + "#'" + newText + "'# > " + buildProp);
        }
    }

    private void doSetText(final Button view, final String text) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(text);
            }
        });
    }

    private void doSetVisibility(final View view, final int invisible) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(invisible);
            }
        });
    }

    private void doSetChecked(final CheckBox checkBox, final boolean checked) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            checkBoxes.addView(checkBox);
            checkBox.setChecked(checked);
            }
        });
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox)view).isChecked();

        switch(view.getId()) {
            case R.id.checkbox_bootanimation:  // ha több bootanimation.zip lehetőséget is kínálunk
                if (checked) {

                }
                else {

                }
                break;
        }
    }

    public void onClickCancel(View view) throws IOException {
        Log.d(TAG, "onClickCancel: " + view.getTag());
        appendOutPut(((Button)view).getText().toString(), Color.GREEN, true);
        finish();
    }

    private void writeFile(InputStream is, String pathFileName) throws IOException {
        byte[] buffer = new byte[1024];
        int count;
        FileOutputStream fout = new FileOutputStream(pathFileName);

        while ((count = is.read(buffer)) != -1)
        {
            fout.write(buffer, 0, count);
        }

        fout.close();
    }

    private String cmd(String command) throws IOException, InterruptedException {
        return cmd("su", command, false, false);
    }

    private String cmd(String command, boolean ignoreError) throws IOException, InterruptedException {
        return cmd("su", command, ignoreError, false);
    }

    private String cmd(String command, boolean ignoreError, final boolean closeLog) throws IOException, InterruptedException {
        return cmd("su", command, ignoreError, closeLog);
    }

    private String cmd(String sh, String command, boolean ignoreError, final boolean closeLog) throws IOException, InterruptedException {
        Log.d(TAG, command);
        appendOutPut(command, Color.BLACK, closeLog);
        Process process = Runtime.getRuntime().exec(new String[]{sh, "-c", command});
        process.waitFor();
        String errStream = readFullyAsString(process.getErrorStream(), Charset.defaultCharset().name());
        String outStream = readFullyAsString(process.getInputStream(), Charset.defaultCharset().name());
        Log.d(TAG, outStream);
        Log.d(TAG, errStream);
        if (process.exitValue() != 0 && !ignoreError) {
            appendOutPut(errStream, Color.RED, closeLog);
            throw new RuntimeException("hiba a következő parancsban: " + command + ", hibaüzenet: " + errStream);
        }
        String ret = outStream + errStream;
        appendOutPut(ret, Color.BLUE, closeLog);
        if (!ret.replace("\n","").isEmpty()) {
        appendOutPut(" ", Color.BLUE, closeLog);
        }
        return ret;
    }

    private void appendOutPut(final String text, final int color) {
        appendOutPut(text, color, false);
    }

    private void appendOutPut(final String text, final int color, final boolean closeLog) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView tv = new TextView(MainActivity.this);
                tv.setTextColor(color);
                tv.setText(text);
                tv.setTextSize((float) 12);
                ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                tv.setLayoutParams(layoutParams);
                tv.setSingleLine();
                tv.setHorizontallyScrolling(true);
                tv.setSelected(true);
                tv.setMovementMethod(new ScrollingMovementMethod());
                outPut.addView(tv);
                scrollDown();
                if (logWriter != null && !text.equals(BLUE_SPACE)) {
                    final String textHtml = "<nobr><font color=\"" + htmlColor(color) + "\">" + text.replace("\n", "<br>") + "</font></nobr><br>";
                    try {
                        MainActivity.this.logWriter.write(textHtml + "\n");
                    }
                    catch (Exception e) {
                        error(e);
                    }
                }
                if (closeLog) {
                    try {
                        logWriter.write(HTMLE);
                        logWriter.close();
                        logWriter = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private void scrollDown() {
        scroll.postDelayed(new Runnable() {
            @Override
            public void run() { // csak késleltetve megy a legvégére
                scroll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },200);
    }

    public static String htmlColor(int color) {
        switch (color) {
            case Color.BLACK:
                return "black";
            case Color.BLUE:
                return "blue";
            case Color.RED:
                return "red";
            case Color.GREEN:
                return "green";
        }
        return "";
    }

    public static String readFullyAsString(InputStream inputStream, String encoding) throws IOException {
        return readFully(inputStream).toString(encoding);
    }

    public static byte[] readFullyAsBytes(InputStream inputStream) throws IOException {
        return readFully(inputStream).toByteArray();
    }

    public static ByteArrayOutputStream readFully(InputStream inputStream)  throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos;
    }

    /**
     * @param is
     * @param outputPathDir
     * @throws IOException
     * @author Vasily Sochinsky, zapl
     * @see https://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
     */
    public static void unZip(InputStream is, String outputPathDir) throws IOException {
        File targetDirectory = new File (outputPathDir);
        try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " + dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                try (FileOutputStream fout = new FileOutputStream(file)) {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                }

            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        }
    }

    /**
     * Zips a Folder to "[Folder].zip"
     * @param toZipFolder Folder to be zipped
     * @return the resulting ZipFile
     * @author: HailZeon, save_jeff
     * @see  https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android
     */
    public static File zipFolder(File toZipFolder) {
        File ZipFile = new File(toZipFolder.getParent(), String.format("%s.zip", toZipFolder.getName()));
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(ZipFile));
            zipSubFolder(out, toZipFolder, toZipFolder.getPath().length());
            out.close();
            return ZipFile;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Main zip Function
     * @param out Target ZipStream
     * @param folder Folder to be zipped
     * @param basePathLength Length of original Folder Path (for recursion)
     */
    private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {

        File[] fileList = folder.listFiles();
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];

                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength + 1);

                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // to keep modification time after unzipping
                out.putNextEntry(entry);

                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
                out.closeEntry();
            }
        }
    }
}
