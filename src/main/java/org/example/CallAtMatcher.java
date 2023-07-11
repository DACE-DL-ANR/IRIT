package org.example;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

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




    public void start() throws IOException {
        process = processBuilder.start();

        Scanner s = new Scanner(process.getInputStream());

        while (s.hasNextLine()) {
            String x = s.nextLine();
            if (x.contains("- Started @")) {
                break;
            }
        }
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
