package com.github.onsdigital.perkin.storage;

import com.github.onsdigital.perkin.helpers.Configuration;
import com.github.onsdigital.perkin.json.FtpInfo;
import org.apache.commons.fileupload.FileItem;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * FTP the FileItem.
 */
public class FtpPublisher implements Publisher {

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

    public void publish(final FileItem data, final String path) throws IOException {
        String filename = data.getName();
        ftpFile(data.getInputStream(), path, filename);
    }

    public FtpInfo list() throws IOException {

        FtpInfo.FtpInfoBuilder ftpInfo = FtpInfo.builder().command("list");

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        System.out.println("ftp connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        System.out.println("ftp login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                System.out.println("ftp remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                System.out.println("ftp current directory is " + ftp.printWorkingDirectory());

                FTPFile[] files = ftp.listFiles();
                for (FTPFile file : files) {
                    ftpInfo.filename(file.getName());
                }

                System.out.println("ftp logout");
                ftp.logout();
                System.out.println("ftp disconnect");
                ftp.disconnect();

            } else {
                System.out.println("ftp login to server was not positive completion. reply code: " + reply);
                ftp.disconnect();
            }
        } else {
            String msg = "ftp login to " + host + ":" + port + " failed for user: " + user;
            System.out.println(msg);
            ftp.logout();
            throw new IOException(msg);
        }

        return ftpInfo.build();
    }

    public String get(String filename) throws IOException {

        String data = "<no data>";

        FtpInfo.FtpInfoBuilder ftpInfo = FtpInfo.builder();

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        System.out.println("ftp connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        System.out.println("ftp login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                System.out.println("ftp remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                System.out.println("ftp current directory is " + ftp.printWorkingDirectory());

                System.out.println("ftp get file " + filename);
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ftp.retrieveFile(filename, out);
                data = out.toString();
                System.out.println("ftp " + filename + " contents: " + data);

                System.out.println("ftp logout");
                ftp.logout();
                System.out.println("ftp disconnect");
                ftp.disconnect();

            } else {
                System.out.println("ftp login to server was not positive completion. reply code: " + reply);
                ftp.disconnect();
            }
        } else {
            String msg = "ftp login to " + host + ":" + port + " failed for user: " + user;
            System.out.println(msg);
            ftp.logout();
            throw new IOException(msg);
        }

        return data;
    }

    public FtpInfo deleteAll() throws IOException {

        FtpInfo.FtpInfoBuilder ftpInfo = FtpInfo.builder().command("deleteAll");;

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        System.out.println("ftp connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        System.out.println("ftp login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                System.out.println("ftp remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                System.out.println("ftp current directory is " + ftp.printWorkingDirectory());

                FTPFile[] files = ftp.listFiles();
                for (FTPFile file : files) {
                    ftpInfo.filename(file.getName());
                    System.out.println("ftp delete file: " + file.getName());
                    ftp.deleteFile(file.getName());
                }

                System.out.println("ftp logout");
                ftp.logout();
                System.out.println("ftp disconnect");
                ftp.disconnect();

            } else {
                System.out.println("ftp login to server was not positive completion. reply code: " + reply);
                ftp.disconnect();
            }
        } else {
            String msg = "ftp login to " + host + ":" + port + " failed for user: " + user;
            System.out.println(msg);
            ftp.logout();
            throw new IOException(msg);
        }

        return ftpInfo.build();
    }

    private void ftpFile(InputStream inputStream, String path, String filename) throws IOException {

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        System.out.println("ftp connect to " + host + " " + port);
        ftp.connect(host, port);

        //login to server
        System.out.println("ftp login user: " + user);
        if (ftp.login(user, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                System.out.println("ftp remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(path);
                System.out.println("ftp current directory is " + ftp.printWorkingDirectory());

                //store the file in the remote server
                System.out.println("ftp storing file: " + filename);
                ftp.storeFile(filename, inputStream);
                //close the stream
                System.out.println("ftp closing stream");
                inputStream.close();
                System.out.println("ftp wrote file: " + filename);
                System.out.println("ftp logout");
                ftp.logout();
                System.out.println("ftp disconnect");
                ftp.disconnect();

            } else {
                System.out.println("ftp login to server was not positive completion. reply code: " + reply);
                ftp.disconnect();
            }
        } else {
            String msg = "ftp login to " + host + ":" + port + " failed for user: " + user;
            System.out.println(msg);
            ftp.logout();
            throw new IOException(msg);
        }
    }
}
