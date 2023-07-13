package org.example;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CallAtMatcher {

    private Process process;
    private ProcessBuilder processBuilder;

    public CallAtMatcher(String java, String path){
        Path path1 = Paths.get(path);
        processBuilder = new ProcessBuilder();
        processBuilder.directory(path1.getParent().toFile());

        processBuilder.command(java,
                "-cp",
                path1.getFileName() + ":lib/*",
                "de.uni_mannheim.informatik.dws.melt.receiver_http.Main");
    }


    public void start() throws IOException, InterruptedException {
        process = processBuilder.start();

        InputStream inputStream = process.getInputStream();
        InputStream errorStream = process.getErrorStream();
        AtomicBoolean started = new AtomicBoolean(false);

        Thread t1 = new Thread(() -> {

            for (int i = 0; i < 10; i++) {

                try {
                    if (inputStream.available() > 0) {
                        started.set(true);
                        break;
                    }
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (started.get()) {
                    return;
                }
            }

            Scanner s = new Scanner(inputStream);
            while (s.hasNextLine()) {
                String x = s.nextLine();
                if (x.contains("- Started @")) {
                    break;
                }
            }
        });

        Thread t2 = new Thread(() -> {

            for (int i = 0; i < 10; i++) {

                try {
                    if (errorStream.available() > 0) {
                        started.set(true);
                        break;
                    }
                    Thread.sleep(1000);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }

                if (started.get()) {
                    return;
                }
            }

            Scanner s = new Scanner(errorStream);
            while (s.hasNextLine()) {
                String x = s.nextLine();
                if (x.contains("- Started @")) {
                    break;
                }
            }
        });



        t1.start();
        t2.start();
        t1.join();
        t2.join();



        System.out.println("Started.");
    }

    public void run(String source, String target, String output) throws IOException, InterruptedException {

        String endpointUrl = "http://127.0.0.1:8080/match";

        ProcessBuilder processBuilder2 = new ProcessBuilder(
                "curl",
                "-F",
                "source=@" + source,
                "-F",
                "target=@" + target,
                endpointUrl);
        processBuilder2.redirectOutput(ProcessBuilder.Redirect.to(new File(output)));

        Process process = processBuilder2.start();
        int exitCode = process.waitFor();
    }

    public void close(){
        process.destroy();
    }
}
