package edu.duke.adtg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

public class ScriptExecutor {
    private  String inputPath;
    private  String scriptPath; // Replace with your script path
    private  String outputPath; // Output file for the script output (Optional)


    public ScriptExecutor(String inputPath, String scriptPath) {
        this(inputPath, scriptPath, "./script_output.txt");
    }
    /**
     * Executes a script using the provided input, script, and output paths.
     *
     * @param inputPath  the path to the input file
     * @param scriptPath the path to the script file
     * @param outputPath the path to the output file
     */
    public ScriptExecutor(String inputPath, String scriptPath, String outputPath) {
        this.inputPath = inputPath;
        this.scriptPath = scriptPath;
        this.outputPath = outputPath;
    }
    /**
     * Executes a script file and captures the output.
     * 
     * @return The output file containing the captured output, or null if an error occurred.
     */
    public File executeFile() {

        File scriptFile = new File(inputPath, scriptPath);

        if (!scriptFile.exists()) {
            System.err.println("Script file does not exist: " + inputPath +"/"+ scriptPath);
            return null;
        }

        // Set executable permission on script file
        if (!scriptFile.setExecutable(true)) {
            System.err.println("Failed to set executable permission on script file: " + inputPath +"/" + scriptPath);
            return null;
        }
        // Create a ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder();

        // Set the command to execute the script
        processBuilder.command("bash", scriptPath);
        // processBuilder.command(scriptPath);
        // Redirect error stream to standard output
        processBuilder.redirectErrorStream(true);

        // Set the working directory (optional)
        String workingDirectory = System.getProperty("user.dir") + File.separator + inputPath;
        processBuilder.directory(new File(workingDirectory));

        try {
            // Start the process
            Process process = processBuilder.start();

            // Capture the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to complete
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);

            // Write the output to a file
            File outputFile = new File(outputPath);
            writeFile(outputFile, output.toString());

            System.out.println("Script executed successfully. Output saved to " + outputPath);
            return outputFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void setExecutablePermission(String filePath) {
        try {
            File scriptFile = new File(filePath);
            if (!scriptFile.canExecute()) {
                // Set executable permission using Java NIO Files API
                Files.setPosixFilePermissions(Paths.get(scriptFile.getAbsolutePath()), 
                        new java.util.HashSet<>(Arrays.asList(
                                java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                                java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE,
                                java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the specified content to the given file.
     *
     * @param file    the file to write to
     * @param content the content to write
     */
    public static void writeFile(File file, String content) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
