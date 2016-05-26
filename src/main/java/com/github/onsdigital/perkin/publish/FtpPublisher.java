package com.github.onsdigital.perkin.publish;

import com.github.onsdigital.ConfigurationManager;
import com.github.onsdigital.perkin.helper.Timer;
import com.github.onsdigital.perkin.transform.Audit;
import com.github.onsdigital.perkin.transform.DataFile;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.util.List;

@Slf4j
public class FtpPublisher {

    private String host;
    private int port;
    private String user;
    private String password;
    private FTPClient ftpClient;
    private boolean loggedIn = false;

    public FtpPublisher() {
        host = ConfigurationManager.get("FTP_HOST");
        port = ConfigurationManager.getInt("FTP_PORT");
        user = ConfigurationManager.get("FTP_USER");
        password = ConfigurationManager.get("FTP_PASS");
    }

    public void publish(List<DataFile> files) throws IOException {
        login();

        try {
            for (DataFile file : files) {
                Timer timer = new Timer("publish.");

                String filename = file.getFilename();


                InputStream inputStream = new ByteArrayInputStream(file.getBytes());

                String path = determinePath(file.getPath());
                put(inputStream, path, filename);

                timer.stopStatus(200);
                Audit.getInstance().increment(timer, file);
            }
        }
        catch(IOException e) {
            log.error("FTP|problem during ftp transfer: ", e);
        }
        finally {
            logout();
        }
    }

    protected static String determinePath(String path) {

        if (path == null) {
            return "/";
        }

        path = path.replace("\\", "/");
        int end = path.length();
        int pos = path.indexOf("EDC_");
        if (path.endsWith("/")) {
            end -= 1;
        }
        if (pos > -1) {
            return path.substring(pos, end);
        }

        return path;
    }

    private void login() throws IOException {
        //new ftp client
        ftpClient = new FTPClient();
        //try to login
        log.debug("FTP|login to " + host + " " + port);
        ftpClient.connect(host, port);

        //login to server
        log.debug("FTP|login user: " + user);

        boolean ftpLogin = ftpClient.login(user, password);

        if (!ftpLogin) {
            String msg = "FTP|login to " + host + ":" + port + " failed for user: " + user;
            log.error(msg);
            ftpClient.logout();
            throw new IOException(msg);
        }

        int reply = ftpClient.getReplyCode();

        //FTPReply stores a set of constants for FTP reply codes.
        if (!FTPReply.isPositiveCompletion(reply)) {
            log.warn("FTP|login to server was not positive completion. reply code: " + reply);
            ftpClient.disconnect();
        }

        loggedIn = FTPReply.isPositiveCompletion(reply) && ftpLogin;
    }

    private void logout() throws IOException{

        if (loggedIn) {
            log.debug("FTP|logout");
            ftpClient.logout();
            log.debug("FTP|disconnect");
            ftpClient.disconnect();
            loggedIn = false;
        } else {
            log.info("FTP|Can't logout, not connected");
        }
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void put(InputStream inputStream, String path, String filename) throws IOException {

        if (loggedIn) {
            //get system name
            log.debug("FTP|remote system is " + ftpClient.getSystemType());
            //change current directory
            log.debug("FTP|changing dir to: {}", path);
            ftpClient.changeWorkingDirectory(path);
            log.debug("FTP|current directory is " + ftpClient.printWorkingDirectory());

            log.debug("FTP|entering local passive mode (new)");
            ftpClient.enterLocalPassiveMode();

            //store the file in the remote server
            log.debug("FTP|storing binary file: " + filename);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean ok = ftpClient.storeFile(filename, inputStream);

            if (!ok) {
                String replyString = ftpClient.getReplyString();

                int reply = ftpClient.getReplyCode();

                if(!FTPReply.isPositiveCompletion(reply)) {
                    log.debug("FTP|failed to store file. reply string: {}", replyString);
                }
                throw new IOException("ftp failed to store file: " + path + "/" + filename);
            }

            //close the stream
            log.debug("FTP|closing stream");
            inputStream.close();
            log.info("FTP|PUT|wrote file: " + filename);

            ftpClient.changeWorkingDirectory("/");
        } else {
            log.info("FTP|Can't put files, not connected");
        }
    }

    public byte[] get(String filename) throws IOException {

        byte[] data = null;

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to login
        log.debug("FTP|login to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        log.debug("FTP|login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                log.debug("FTP|remote system is " + ftp.getSystemType());
                log.debug("FTP|current directory is " + ftp.printWorkingDirectory());

                log.info("FTP|GET|get binary file " + filename);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.retrieveFile(filename, out);
                data = out.toByteArray();
                log.info("FTP|GET|got file: " + filename + " size: " + data.length);

                log.debug("FTP|logout");
                ftp.logout();
                log.debug("FTP|logout");
                ftp.disconnect();

            } else {
                log.warn("FTP|login to server was not positive completion. reply code: " + reply);
                ftp.disconnect();
            }
        } else {
            String msg = "FTP|login to " + host + ":" + port + " failed for user: " + user;
            log.error(msg);
            ftp.logout();
            throw new IOException(msg);
        }

        return data;
    }
}
