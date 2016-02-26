package com.github.onsdigital.perkin.storage;

import org.apache.commons.fileupload.FileItem;

import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;

/**
 * FTP the FileItem.
 */
public class FtpPublisher implements Publisher {

    public void publish(final FileItem data, final String path) throws IOException {
        String filename = data.getName();
        ftpFile(data.getInputStream(), path, filename);
    }

    private void ftpFile(InputStream inputStream, String path, String filename) throws IOException {

        String serverAddress = "192.168.99.100";
        String userId = "ons";
        String password = "ons";
        //TODO: ignoring path parameter for now
        String remoteDirectory = "/";

        //new ftp client
        FTPClient ftp = new FTPClient();
        //try to connect
        System.out.println("ftp connect to server: " + serverAddress);
        ftp.connect(serverAddress);

        //login to server
        System.out.println("ftp login userId: " + userId);
        if (ftp.login(userId, password)) {

            int reply = ftp.getReplyCode();
            //FTPReply stores a set of constants for FTP reply codes.
            if (FTPReply.isPositiveCompletion(reply)) {

                //get system name
                System.out.println("ftp remote system is " + ftp.getSystemType());
                //change current directory
                ftp.changeWorkingDirectory(remoteDirectory);
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
            System.out.println("ftp login failed");
            ftp.logout();
        }
    }
}
