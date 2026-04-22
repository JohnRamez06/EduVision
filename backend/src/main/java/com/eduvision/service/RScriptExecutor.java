package com.eduvision.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RScriptExecutor {

    private final String rscriptCommand;

    public RScriptExecutor(@Value("${eduvision.rscript.command:Rscript}") String rscriptCommand) {
        this.rscriptCommand = rscriptCommand;
    }

    public String execute(String scriptPath, List<String> args) {
        List<String> command = new ArrayList<>();
        command.add(rscriptCommand);
        command.add(scriptPath);
        if (args != null && !args.isEmpty()) {
            command.addAll(args);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                output = reader.lines().reduce("", (acc, line) -> acc.isEmpty() ? line : acc + "\n" + line);
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IllegalStateException("R script failed with exit code " + exitCode + ": " + output);
            }
            return output;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("R script execution interrupted", ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to execute R script", ex);
        }
    }
}
