package org.example;

import java.io.IOException;

public class CallAMD {

    private final String pythonPath;
    private final String amdPath;

    public CallAMD(String pythonPath, String amdPath) {
        this.pythonPath = pythonPath;
        this.amdPath = amdPath;
    }


    public void run(String src1, String src2, String out) throws IOException, InterruptedException {
        String sb = pythonPath + " " +
                amdPath + " " +
                "file://" + src1 + " " +
                "file://" + src2 + " " +
                out;
        ProcessBuilder processBuilder = new ProcessBuilder(sb.split(" "));
        Process start = processBuilder.start();
        start.waitFor();
    }
}
