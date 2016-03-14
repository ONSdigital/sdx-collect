package com.github.onsdigital.perkin.publish;

import com.github.onsdigital.Configuration;
import com.github.onsdigital.perkin.transform.idbr.IdbrReceipt;
import com.github.onsdigital.perkin.transform.DataFile;
import com.github.onsdigital.perkin.transform.jpg.Image;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.FileItem;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class FtpPublisher {

    protected static final String FTP_HOST = "ftp.host";
    protected static final String FTP_PORT = "ftp.port";
    protected static final String FTP_USER = "ftp.user";
    protected static final String FTP_PASSWORD = "ftp.password";
    protected static final String FTP_PATH = "ftp.path";

    private String host;
    private int port;
    private String user;
    private String password;
    private String path;

    public FtpPublisher() {
        host = Configuration.get(FTP_HOST, "pure-ftpd");
        port = Configuration.getInt(FTP_PORT, 21);
        user = Configuration.get(FTP_USER, "ons");
        password = Configuration.get(FTP_PASSWORD, "ons");
        path = Configuration.get(FTP_PATH, "/");
    }

    public void publish(List<DataFile> files) throws IOException {
        for (DataFile data : files) {
            String filename = data.getFilename();
            InputStream inputStream = new ByteArrayInputStream(data.getBytes());
            put(inputStream, path, filename);
        }
    }

    private void put(InputStream inputStream, String path, String filename) throws IOException {

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        log.debug("FTP|connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        log.debug("FTP|login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                log.debug("FTP|remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                log.debug("FTP|current directory is " + ftp.printWorkingDirectory());

                //store the file in the remote server
                log.debug("FTP|storing binary file: " + filename);
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.storeFile(filename, inputStream);
                //close the stream
                log.debug("FTP|closing stream");
                inputStream.close();
                log.info("FTP|PUT|wrote file: " + filename);
                log.debug("FTP|logout");
                ftp.logout();
                log.debug("FTP|disconnect");
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
    }

    public byte[] get(String filename) throws IOException {

        byte[] data = null;

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        log.debug("FTP|connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        log.debug("FTP|login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                log.debug("FTP|remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                log.debug("FTP|current directory is " + ftp.printWorkingDirectory());

                log.info("FTP|GET|get binary file " + filename);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ftp.setFileType(FTP.BINARY_FILE_TYPE);
                ftp.retrieveFile(filename, out);
                data = out.toByteArray();
                log.info("FTP|GET|got file: " + filename + " size: " + data.length);

                log.debug("FTP|logout");
                ftp.logout();
                log.debug("FTP|disconnect");
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
