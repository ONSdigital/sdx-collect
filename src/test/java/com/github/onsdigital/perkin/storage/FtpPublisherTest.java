package com.github.onsdigital.perkin.storage;

import com.github.onsdigital.perkin.helpers.Configuration;
import com.github.onsdigital.perkin.helpers.Json;
import com.github.onsdigital.perkin.json.Survey;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockftpserver.fake.FakeFtpServer;
import org.mockftpserver.fake.UserAccount;
import org.mockftpserver.fake.filesystem.DirectoryEntry;
import org.mockftpserver.fake.filesystem.FileSystem;
import org.mockftpserver.fake.filesystem.UnixFakeFileSystem;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.when;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(MockitoJUnitRunner.class)
public class FtpPublisherTest {

    @Mock
    private FileItem data;

    private FtpPublisher classUnderTest;

    private FakeFtpServer fakeFtpServer;
    private int port;

    @Before
    public void setUp() throws IOException {
        //example json file
        String json = Json.format(Survey.builder().id("id").respondentId("respondentId").build());
        InputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        when(data.getInputStream()).thenReturn(in);
        when(data.getName()).thenReturn("test.json");


        fakeFtpServer = new FakeFtpServer();
        fakeFtpServer.setServerControlPort(0);  // use any free port
        fakeFtpServer.addUserAccount(new UserAccount("ons", "ons", "/"));

        FileSystem fileSystem = new UnixFakeFileSystem();
        fileSystem.add(new DirectoryEntry("/"));
        fakeFtpServer.setFileSystem(fileSystem);

        fakeFtpServer.start();
        port = fakeFtpServer.getServerControlPort();

        //create publisher
        Configuration.set(FtpPublisher.FTP_HOST, "localhost");
        Configuration.set(FtpPublisher.FTP_PORT, port);
        classUnderTest = new FtpPublisher();

        System.out.println("FakeFtpServer running on port " + port);
    }

    @After
    public void tearDown() {
        fakeFtpServer.stop();
    }

    @Test(expected = IOException.class)
    public void shouldErrorConnectionRefused() throws IOException {
        //given
        Configuration.set(FtpPublisher.FTP_PORT, 8888);
        classUnderTest = new FtpPublisher();

        //when
        classUnderTest.publish(data, "");
    }

    @Test(expected = IOException.class)
    public void shouldErrorInvalidCredentials() throws IOException {
        //given
        Configuration.set(FtpPublisher.FTP_USER, "invalid");
        classUnderTest = new FtpPublisher();

        //when
        classUnderTest.publish(data, "");
    }

    @Test
    public void shouldPublishFile() throws IOException {
        //given

        //when
        classUnderTest.publish(data, "");

        //then
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect("localhost", port);
        ftpClient.login("ons", "ons");
        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile file : files) {
            System.out.println("FtpPublisherTest - ftp list. file: " + file.getName());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ftpClient.retrieveFile(files[0].getName(), out);
        String contents = out.toString("UTF-8");
        System.out.println("test.json: " + contents);
        ftpClient.quit();
        ftpClient.disconnect();

        assertThat(files.length, is(1));
        assertThat(files[0].getName(), is("test.json"));
    }

    //TODO: add test for file writing fails
}
