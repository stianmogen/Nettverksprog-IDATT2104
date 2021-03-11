package com.nettverksprog.docker;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.util.logging.Logger;

@RestController
@RequestMapping("/compile")
public class CompilerController {

    private static final String FOLDER = "./src/main/java/com/nettverksprog/docker";
    private static final String PATH = "main.cpp";
    private static final String IMAGE_NAME = "gcc";

    private static Logger logger = Logger.getLogger(CompilerController.class.getName());

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping
    public ResponseEntity<?> compile(@RequestBody SourceCode sourceCode) throws Exception {

        writeCodeToFile(sourceCode);

        int status = buildImage();
        logStatus(status);

        StringBuilder builder = runCode();
        deleteFile();

        return ResponseEntity.status(HttpStatus.OK).body(builder.toString());
    }

    private void deleteFile() {
        File myObj = new File(FOLDER + "/" + PATH);
        if (myObj.delete()) {
            logger.info("Deleted the file: " + myObj.getName());
        } else {
            logger.info("Failed to delete the file.");
        }
    }

    private void writeCodeToFile(SourceCode sourceCode) {
        try {
            BufferedWriter bf = new BufferedWriter(new FileWriter(FOLDER + "/" + PATH));
            bf.write(sourceCode.getCode());
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to compile");
        }
    }

    private int buildImage() throws InterruptedException, IOException {
        Process build = Runtime.getRuntime().exec("docker build " + FOLDER + " -t " + IMAGE_NAME);
        BufferedReader reader = new BufferedReader(new InputStreamReader(build.getErrorStream()));
        StringBuilder builder = new StringBuilder();

        String s;
        while ((s = reader.readLine()) != null)
            builder.append(s);
        logger.warning(builder.toString());
        return build.waitFor();
    }

    private void logStatus(int status) {
        if(status == 0)
            logger.info("Docker image has been built");
        else
            logger.info("Error while building image (Exit status: " + status +")");
    }

    private StringBuilder runCode() throws IOException, InterruptedException {
        int status;
        logger.info("Running the container");
        Process process = Runtime.getRuntime().exec("docker run --rm " + IMAGE_NAME);
        status = process.waitFor();
        logger.info("End of the execution of the container (Exited with status " + status + ")");

        return getOutput(process);
    }

    private StringBuilder getOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder builder = new StringBuilder();

        String s;
        while ((s = reader.readLine()) != null)
            builder.append(s);
        return builder;
    }

}
