// src/main/java/com/eduvision/service/RScriptExecutor.java
package com.eduvision.service;

/**
 * RScriptExecutor — Cross-platform R script launcher.
 *
 * <p>This component is the only place in the Java codebase that actually invokes R.
 * It uses {@link ProcessBuilder} to spawn a child {@code Rscript} process, passes
 * the script path and arguments on the command line, streams stdout/stderr into
 * the Spring Boot log, and returns {@code true} if the process exits with code 0.
 *
 * <p><b>Finding Rscript on Windows:</b><br>
 * On Windows the R installer does not always add Rscript.exe to the system PATH,
 * so {@link #resolveRscript()} checks three locations in order:
 * <ol>
 *   <li>The {@code R_HOME} environment variable (set by the R installer when
 *       "Add to PATH" is checked during installation).</li>
 *   <li>A hard-coded list of common installation paths for R versions 4.3–4.5.</li>
 *   <li>A dynamic scan of {@code C:\Program Files\R\} for any installed version.</li>
 * </ol>
 * On Mac/Linux {@code "Rscript"} is used directly (assumed to be on PATH via
 * Homebrew or the system package manager).
 *
 * <p><b>PATH augmentation:</b><br>
 * On Windows, R's {@code bin\} directory is prepended to the child process PATH so
 * that R packages like {@code rmarkdown} can locate {@code pandoc.exe} and other
 * tools that live alongside {@code Rscript.exe}.<br>
 * On Mac, TinyTeX's binary directory and Homebrew's bin are prepended so that
 * {@code pdflatex} and other LaTeX tools are findable.
 *
 * <p><b>Working directory:</b><br>
 * The child process is started with its working directory set to the monorepo root
 * (the parent of the Spring Boot {@code user.dir}).  R scripts resolve the
 * {@code analytics-r/} path relative to this directory.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class RScriptExecutor {

    private static final Logger log = LoggerFactory.getLogger(RScriptExecutor.class);

    private static final boolean IS_WINDOWS =
            System.getProperty("os.name", "").toLowerCase().contains("win");

    /** Resolve the full path to Rscript on this machine. */
    private static final String RSCRIPT_PATH = resolveRscript();

    /**
     * Locates the {@code Rscript} / {@code Rscript.exe} binary at JVM startup.
     *
     * <p>The result is stored in a static final field so the filesystem search
     * only happens once per JVM lifetime.  See class-level Javadoc for the
     * three-step Windows search strategy.
     *
     * @return full path to {@code Rscript.exe} on Windows, or {@code "Rscript"} elsewhere
     */
    private static String resolveRscript() {
        if (!IS_WINDOWS) {
            return "Rscript";   // Mac/Linux: rely on PATH
        }

        // 1. R_HOME env var (set by R installer if "add to PATH" was checked)
        String rHome = System.getenv("R_HOME");
        if (rHome != null && !rHome.isBlank()) {
            String exe = rHome + "\\bin\\Rscript.exe";
            if (new File(exe).exists()) {
                return exe;
            }
        }

        // 2. Walk common Windows install locations
        String[] candidates = {
            "C:\\Program Files\\R\\R-4.5.2\\bin\\Rscript.exe",
            "C:\\Program Files\\R\\R-4.5.1\\bin\\Rscript.exe",
            "C:\\Program Files\\R\\R-4.4.2\\bin\\Rscript.exe",
            "C:\\Program Files\\R\\R-4.4.1\\bin\\Rscript.exe",
            "C:\\Program Files\\R\\R-4.3.3\\bin\\Rscript.exe",
        };
        for (String path : candidates) {
            if (new File(path).exists()) {
                log.info("Found Rscript at: {}", path);
                return path;
            }
        }

        // 3. Scan C:\Program Files\R\ for any installed version
        File rDir = new File("C:\\Program Files\\R");
        if (rDir.isDirectory()) {
            File[] versions = rDir.listFiles(File::isDirectory);
            if (versions != null) {
                for (File v : versions) {
                    File exe = new File(v, "bin\\Rscript.exe");
                    if (exe.exists()) {
                        log.info("Found Rscript at: {}", exe.getAbsolutePath());
                        return exe.getAbsolutePath();
                    }
                }
            }
        }

        log.warn("Rscript.exe not found — falling back to 'Rscript' (must be in PATH)");
        return "Rscript";
    }

    /**
     * Executes an R script in a child process via {@link ProcessBuilder}.
     *
     * <p>HOW IT WORKS:
     * <ol>
     *   <li>Builds the command list:
     *       {@code [RSCRIPT_PATH, "analytics-r/<scriptRelativePath>", arg1, arg2, ...]}</li>
     *   <li>Creates a {@link ProcessBuilder} with {@code redirectErrorStream(false)} so
     *       stdout and stderr can be read on separate threads (avoiding deadlock on large
     *       R output).</li>
     *   <li>Sets the child process working directory to the monorepo root so R scripts
     *       can use relative paths like {@code "../config.R"}.</li>
     *   <li>Augments the child PATH:
     *       <ul>
     *         <li>Windows: prepends R's {@code bin\} directory.</li>
     *         <li>Mac: prepends TinyTeX and Homebrew bin directories.</li>
     *       </ul></li>
     *   <li>Reads stdout line-by-line and logs each line at INFO level with the
     *       {@code [R]} prefix.  Reads stderr at WARN level with {@code [R err]}.</li>
     *   <li>Waits for the process to exit and returns {@code true} iff exit code == 0.</li>
     * </ol>
     *
     * @param scriptRelativePath relative path under {@code analytics-r/}
     *                           (e.g., {@code "scripts/compute_student_summaries.R"})
     * @param args               positional arguments available in R via
     *                           {@code commandArgs(trailingOnly=TRUE)}
     * @return {@code true} if the R process exited with code 0, {@code false} otherwise
     */
    public boolean execute(String scriptRelativePath, String... args) {
        List<String> command = new ArrayList<>();
        command.add(RSCRIPT_PATH);
        command.add("analytics-r/" + scriptRelativePath);
        for (String arg : args) {
            command.add(arg);
        }

        log.info("Executing R: {}", String.join(" ", command));

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(false);
            // Run from the monorepo root so R scripts can find config.R and output/ dirs
            pb.directory(Paths.get(System.getProperty("user.dir")).getParent().toFile());

            // On Windows add R's bin to PATH so rmarkdown can find pandoc etc.
            if (IS_WINDOWS) {
                String rBin = new File(RSCRIPT_PATH).getParent();
                pb.environment().merge("PATH", rBin,
                        (existing, extra) -> extra + ";" + existing);
            } else {
                // On Mac prepend TinyTeX (for pdflatex) and Homebrew bin dirs
                String tinyTexBin = System.getProperty("user.home") + "/Library/TinyTeX/bin/universal-darwin";
                pb.environment().merge("PATH",
                        tinyTexBin + ":/opt/homebrew/bin:/usr/local/bin:/usr/bin:/bin",
                        (existing, extra) -> extra + ":" + existing);
            }

            Process process = pb.start();

            // Stream R stdout → Spring Boot log at INFO level
            try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = stdOut.readLine()) != null) log.info("[R] {}", line);
            }
            // Stream R stderr → Spring Boot log at WARN level
            try (BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = stdErr.readLine()) != null) log.warn("[R err] {}", line);
            }

            int exit = process.waitFor();
            if (exit == 0) {
                log.info("R script OK: {}", scriptRelativePath);
                return true;
            } else {
                log.error("R script failed (exit {}): {}", exit, scriptRelativePath);
                return false;
            }
        } catch (Exception e) {
            log.error("Failed to run R script {}: {}", scriptRelativePath, e.getMessage(), e);
            return false;
        }
    }
}
