package com.github.onsdigital.perkin.storage;

import com.github.onsdigital.perkin.helpers.Configuration;
import org.apache.commons.fileupload.FileItem;

import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 * FTP the FileItem.
 */
public class FtpPublisher implements Publisher {

    private String host;
    private int port;
    private String user;
    private String password;
    private String path;

    public FtpPublisher() {
        host = Configuration.get(Configuration.FTP_HOST, "192.168.99.100");
        port = Configuration.getInt(Configuration.FTP_PORT, 21);
        user = Configuration.get(Configuration.FTP_USER, "ons");
        password = Configuration.get(Configuration.FTP_PASSWORD, "ons");
        path = Configuration.get(Configuration.FTP_PATH, "/");
    }

    public void publish(final FileItem data, final String path) throws IOException {
        String filename = data.getName();
        ftpFile(data.getInputStream(), path, filename);
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
