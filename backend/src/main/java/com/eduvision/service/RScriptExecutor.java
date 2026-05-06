// src/main/java/com/eduvision/service/RScriptExecutor.java
package com.eduvision.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Component
public class RScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(RScriptExecutor.class);

    /**
     * Execute an R script via ProcessBuilder.
     *
     * @param scriptRelativePath  relative path under analytics-r/, e.g. "generators/report.R"
     * @param args                positional arguments passed to commandArgs(trailingOnly=TRUE)
     * @return true if exit code == 0
     */
    public boolean execute(String scriptRelativePath, String... args) {
        List<String> command = new ArrayList<>();
        command.add("Rscript");
        command.add("analytics-r/" + scriptRelativePath);
        for (String arg : args) {
            command.add(arg);
        }

        log.info("Executing R script: {}", String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            // Run from project root (parent of backend/) so analytics-r/ paths resolve correctly
            pb.directory(java.nio.file.Paths.get(System.getProperty("user.dir")).getParent().toFile());
            // Ensure TinyTeX (PDF) and Rscript are reachable regardless of how the JVM was launched
            String tinyTexBin = System.getProperty("user.home") + "/Library/TinyTeX/bin/universal-darwin";
            pb.environment().merge("PATH",
                tinyTexBin + ":/opt/homebrew/bin:/opt/homebrew/sbin:/usr/local/bin:/usr/bin:/bin",
                (existing, extra) -> extra + ":" + existing);
            Process process = pb.start();

            // Log stdout
            try (BufferedReader stdOut = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdOut.readLine()) != null) {
                    log.info("[R stdout] {}", line);
                }
            }

            // Log stderr
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