package com.android.kk.redmik20tomi9t;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

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

    public static final String REDMI_K_20 = "'Redmi K20'";

    public enum State {
        BEFORE,
        INIT,
        ACTION,
        REBOOT,
        ERROR,
    }
    public class OutPutText {
        String text;
    }

    public static final String TAG = "kklog";
    public static final String MOUNT_RW = "mount -o remount -rw /";
    public static final String MOUNT_VENDOR_RW = "mount -o remount -rw /vendor";
    public static final String MOUNT_RO = "mount -o remount -r /";
    public static final String MOUNT_VENDOR_RO = "mount -o remount -r /vendor";

    public static final String BOOTLOGO = "/dev/block/sde46";
    public static final String SYSTEM_MEDIA = "/system/media";
    public static final String BOOTANIMATION_ZIP = "/bootanimation.zip";
    public static final String BOOTANIM_PATH = SYSTEM_MEDIA + BOOTANIMATION_ZIP;
    public static final String SAVE = ".save";

    public static final String HTMLB = "<html><body>\n";
    public static final String HTMLE = "</body></html>";
    public static final String MIME = "text/html";
    public static final String ENCODING = "utf-8";

    public static final int BUFFER = 2048;

//    public static final String PREF = "/sdcard/redmik20tomi9t"; //////////////// csak tesztelésre !!!!!!!!!!!!!!!!!!!!!!!!!!!
    public static final String PREF = "";

    private LinearLayout checkBoxes;
    private CheckBox buildProp;
    private CheckBox bootLogo;
    private CheckBox bootAnimation;
    private CheckBox watermark;
    private CheckBox bin;
    private LinearLayout ll;
    private LinearLayout rw;
    private LinearLayout ro;
    private OutPutText outPutText = new OutPutText();
    private WebView outPut;
    private State state = State.BEFORE;
    private String filesDir;
    private String bootLogoDir;
    private String logFilePath;
    private FileWriter logWriter;
    private String errorMsg;
    private String newBootAnimPath;
    private String sha256sumOrig;
    private String davinciWebp;
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
            return;
        }
//        this.outPut.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                //use the param "view", and call getContentHeight in scrollTo
//                view.scrollTo(0, Math.round(view.getContentHeight() * getResources().getDisplayMetrics().density));
//            }
//        });
//        final WebView wv = this.outPut;
//        this.outPut.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageCommitVisible(WebView view, String url) {
//                view.pageDown(true);
//            }
//        });
    }

    private void init() throws IOException, InterruptedException {
        setContentView(R.layout.activity_main);
        this.bootLogo = this.findViewById(R.id.checkbox_bootlogo);
        this.bootAnimation = this.findViewById(R.id.checkbox_bootanimation);
        this.buildProp = this.findViewById(R.id.checkbox_buildprop);
        this.watermark = this.findViewById(R.id.checkbox_watermark);
        this.bin = this.findViewById(R.id.checkbox_bin);
        this.ll = this.findViewById(R.id.ll);
        this.rw = this.findViewById(R.id.rw);
        this.ro = this.findViewById(R.id.ro);
        this.outPut = this.findViewById(R.id.output);
        this.checkBoxes = this.findViewById(R.id.checkBocxes);
        this.checkBoxes.removeAllViews();

        this.filesDir = getFilesDir ().getPath();
        this.outPutText.text = "";
        final String logDir = filesDir + "/log/";
        try {
            this.user = cmd("sh", "whoami").replace("\n", "");
            cmd("mkdir -m 777 " + logDir);
            cmd("chown " + this.user + ":" + this.user + " " + logDir);
        } catch (Exception e) {
            error(e);
            return;
        }
        Format formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String date = formatter.format(new Date());
        logFilePath = logDir + date + ".html";
        File logFile = new File (logFilePath);
        this.logWriter = new FileWriter(logFile);
        logWriter.write(HTMLB);
        try  {
            String suRet = cmd("whoami");
        }
        catch (Exception e) {
            error(e);
            return;
        }
        this.cameraDir = this.filesDir + "/Camera";
        cmd("mkdir -m 777 " + cameraDir);
        cmd("chown " + this.user + ":" + this.user + " " + cameraDir);
        state = State.INIT;
        File toZip = new File(this.cameraDir);
        final File zipfile = zipFolder(toZip);
        Log.d(TAG, "zipfile: " +zipfile.getAbsolutePath());
        InputStream rawBootLogo = getResources().openRawResource(R.raw.bootlogo9t);
        unZip(rawBootLogo, filesDir);
        bootLogoDir = filesDir + "/LogoOrig/";
        String bootLogosha2 = bootLogoDir + "logo.emmc.win.sha2";
        String sha256sumPart = cmd("sha256sum " + BOOTLOGO).split(" ")[0];
        this.sha256sumOrig = cmd("cat " + bootLogosha2).split(" ")[0];

        this.newBootAnimPath = filesDir + BOOTANIMATION_ZIP;
        String sha256sumOld = cmd("sha256sum " + BOOTANIM_PATH).split(" ")[0];
        String sha256sumNew = cmd("sha256sum " + this.newBootAnimPath).split(" ")[0];

        this.cameraApk = "/system/priv-app/MiuiCamera/MiuiCamera.apk";
        String cameraApkTemp = this.filesDir + "/MiuiCamera.apk";
        cmd("cp " + cameraApk + " " + cameraApkTemp);
        cmd("chown " + this.user + ":" + this.user + " " + cameraApkTemp);
        InputStream is = new FileInputStream(new File(cameraApkTemp));
        unZip(is, cameraDir);
        this.davinciWebp = cameraDir + "/assets/watermarks/davinci.webp ";
        this.davinciGlobalWebp = cameraDir + "/assets/watermarks/davinci_global.webp ";
        String sha256sumDavinciWebp = cmd("sha256sum " + davinciWebp).split(" ")[0];
        String sha256sumDavinciGlobalWebp = cmd("sha256sum " + davinciGlobalWebp).split(" ")[0];

        this.checkBoxes.addView(this.buildProp);
        if (!sha256sumOld.equals(sha256sumNew)) {   // nem a jó bootanimáció van
            doSetChecked(this.bootAnimation, true);
        }
        if (!sha256sumPart.equals(sha256sumOrig)) { // a partíció nem az új logo-t tartalmazza
            doSetChecked(this.bootLogo, true);
        }
        if (!sha256sumDavinciWebp.equals(sha256sumDavinciGlobalWebp)) {
            doSetChecked(this.watermark, true);
        }
        try {
            final String ll = cmd("ls /bin/ll");
        } catch (Exception e) {
            this.checkBoxes.addView(this.bin);
            this.checkBoxes.addView(this.ll);
            this.checkBoxes.addView(this.rw);
            this.checkBoxes.addView(this.ro);
        }
    }

    private void error(Exception e) {
        e.printStackTrace();
        errorMsg = e.getMessage();
        appendOutPut(errorMsg, "red");
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
                logWriter.write(HTMLE);
                logWriter.close();
                cmd("reboot");
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
        boolean unzip = false;
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
                String sha256sum = cmd("sha256sum " + bootLogoPath).split(" ")[0];
                if (sha256sum.equals(sha256sumOrig)) {
                    String fileLength = cmd("ls -l " + bootLogoPath).split(" ")[4];
                    String blockLength = cmd("blockdev --getsize64 " + BOOTLOGO).replace("\n", "");
                    if (fileLength.equals(blockLength)) {
                        ret = cmd("dd if=" + bootLogoPath + " of=" + PREF + BOOTLOGO);
                        reboot = true;
                    }
                    else {
                        errorMsg = "bootlogo fájl hossza " + fileLength + " és " + BOOTLOGO + " hossza " + blockLength + " nem egyezik";
                        appendOutPut(errorMsg, "red");
                        throw new RuntimeException(errorMsg);
                    }
                }
                else {
                    errorMsg = "hibás bootlogo";
                    appendOutPut(errorMsg, "red");
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
                ret = cmd("cp " + davinciWebp + davinciGlobalWebp);
                File cameraApkMod = zipFolder(new File(cameraDir));
                ret = cmd("cp " + cameraApkMod + " " + cameraApk);

                InputStream rawWatermark = getResources().openRawResource(R.raw.davinci_main_space_custom_watermark);
                String watermarkFileName = "davinci_main_space_custom_watermark.png";
                String watermarkTempFilePath = this.filesDir + "/" + watermarkFileName;
                String watermarkFilePath = "/data/data/com.android.camera/files/" + watermarkFileName;
                writeFile(rawWatermark, watermarkTempFilePath);
                ret = cmd("cp " + watermarkTempFilePath + " " + watermarkFilePath);
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
//            Toast.makeText(this, "Porszem került a gépezetbe: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        finally {
            if (rw) {
                ret = cmd(MOUNT_VENDOR_RO);
                ret = cmd(MOUNT_RO);
            }
        }

        switch (state) {
            case REBOOT:
                appendOutPut("<br>hibát nem találtam, " + (reboot? "jöhet egy reboot": "kiléphetsz"), "green");
                if (reboot) {
                    doSetVisibility(view, View.VISIBLE);
                    doSetText((Button) view, "reboot");
                }
                break;

            case ERROR:
                appendOutPut("<br>Porszem került a gépezetbe: " + errorMsg, "red");
                break;
        }
        doSetVisibility(cancel, View.VISIBLE);
        doSetText(cancel, "kilép");
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
        logWriter.write(HTMLE);
        logWriter.close();
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
        return cmd("su", command);
    }

    private String cmd(String sh, String command) throws IOException, InterruptedException {
        Log.d(TAG, command);
        if (state != State.BEFORE) {
            appendOutPut(command, "black");
        }
        Process process = Runtime.getRuntime().exec(new String[]{sh, "-c", command});
        process.waitFor();
        String errStream = readFullyAsString(process.getErrorStream(), Charset.defaultCharset().name());
        String outStream = readFullyAsString(process.getInputStream(), Charset.defaultCharset().name());
        Log.d(TAG, outStream);
        Log.d(TAG, errStream);
        if (process.exitValue() != 0 && state != State.BEFORE) {
            appendOutPut(errStream, "red");
            throw new RuntimeException("hiba a következő parancsban: " + command + ", hibaüzenet: " + errStream);
        }
        final String ret = outStream + errStream;
        if (state != State.BEFORE) {
            appendOutPut(ret, "blue");
        }
        return ret;
    }

    private void appendOutPut(final String text, final String color) {
        final OutPutText mainOutPutText = this.outPutText;
        final WebView mainOutPut = this.outPut;
        this.runOnUiThread(new Runnable() {
            private OutPutText outPutText = mainOutPutText;
            private WebView outPut = mainOutPut;

            @Override
            public void run() {
                final String textHtml = "<nobr><font color=\"" + color + "\">" + text.replace("\n", "<br>") + "</font></nobr><br>";
                this.outPutText.text += textHtml;
                final String data = HTMLB + this.outPutText.text + HTMLE;
                this.outPut.loadDataWithBaseURL(null, data, MIME, ENCODING, null);
//                this.outPut.scrollTo(0, Math.round(this.outPut.getContentHeight() * getResources().getDisplayMetrics().density));
                try {
                    try  {
                        MainActivity.this.logWriter.write(textHtml + "\n");
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
//                    Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", "echo '" + data + "' > " + logFilePath});
//                    process.waitFor();
                }
                catch (Exception e) {
                    error(e);
                }
            }
        });
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
        BufferedInputStream origin = null;
        for (File file : fileList) {
            if (file.isDirectory()) {
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];

                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength + 1);

                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);

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
