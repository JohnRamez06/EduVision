// src/main/java/com/eduvision/service/RScriptExecutor.java
package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(RScriptExecutor.class);

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    /**
     * Execute an R script via ProcessBuilder (cross-platform: Windows + Mac/Linux).
     *
     * @param scriptRelativePath  relative path under analytics-r/, e.g. "generators/report.R"
     * @param args                positional arguments passed to commandArgs(trailingOnly=TRUE)
     * @return true if exit code == 0
     */
    public boolean execute(String scriptRelativePath, String... args) {
        List<String> command = new ArrayList<>();
        command.add(IS_WINDOWS ? "Rscript.exe" : "Rscript");
        command.add("analytics-r/" + scriptRelativePath);
        for (String arg : args) {
            command.add(arg);
        }

        log.info("Executing R script: {}", String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            // Run from project root (parent of backend/) so analytics-r/ paths resolve
            pb.directory(Paths.get(System.getProperty("user.dir")).getParent().toFile());

            if (IS_WINDOWS) {
                // On Windows, ensure R's bin directory is in PATH so Rscript.exe is found.
                // R typically installs to C:\Program Files\R\R-x.x.x\bin
                String rHome = System.getenv("R_HOME");
                if (rHome != null && !rHome.isBlank()) {
                    String rBin = rHome + "\\bin";
                    pb.environment().merge("PATH", rBin,
                            (existing, extra) -> extra + ";" + existing);
                }
                // TinyTeX on Windows (if installed via tinytex::install_tinytex())
                String appData = System.getenv("APPDATA");
                if (appData != null) {
                    String winTex = appData + "\\TinyTeX\\bin\\windows";
                    pb.environment().merge("PATH", winTex,
                            (existing, extra) -> extra + ";" + existing);
                }
            } else {
                // Mac / Linux — add TinyTeX and Homebrew to PATH
                String tinyTexBin = System.getProperty("user.home") + "/Library/TinyTeX/bin/universal-darwin";
                pb.environment().merge("PATH",
                        tinyTexBin + ":/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin",
                        (existing, extra) -> extra + ":" + existing);
            }

            Process process = pb.start();

            // Stream stdout (captures the output file path printed by generators)
            StringBuilder stdout = new StringBuilder();
            try (BufferedReader stdOut = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdOut.readLine()) != null) {
                    log.info("[R stdout] {}", line);
                    stdout.append(line).append("\n");
                }
            }

            // Stream stderr
            try (BufferedReader stdErr = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = stdErr.readLine()) != null) {
                    log.warn("[R stderr] {}", line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("R script completed successfully: {}", scriptRelativePath);
                return true;
            } else {
                log.error("R script exited with code {}: {}", exitCode, scriptRelativePath);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to execute R script {}: {}", scriptRelativePath, e.getMessage(), e);
            return false;
        }
    }
}
