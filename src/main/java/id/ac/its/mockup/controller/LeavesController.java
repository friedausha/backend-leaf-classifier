package id.ac.its.mockup.controller;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
public class LeavesController {
    private final String imageTypePrefix = "data:image/jpg;base64,";
    private final Path trainPath = new File("/home/frieda/Repository/tugas-akhir/RGB").toPath();
    private final Path testPath = new File("/home/frieda/Repository/tugas-akhir/test").toPath();

    @PostMapping("/upload")
    public ResponseEntity<HashMap> uploadPicture(@RequestParam String image, @RequestParam String name) throws IOException, JSONException {
        String encodedImage = image.replace(imageTypePrefix, "");
        byte[] data = Base64.decodeBase64(encodedImage);
        Path leavePath = trainPath.resolve(name);
        String imageId = UUID.randomUUID().toString();
        Path imagePath = leavePath.resolve(imageId + ".JPG");

        Files.createFile(imagePath);
        Files.write(imagePath, data);


        HashMap<String, String> entity = new HashMap<>();
        entity.put("msg", "Uploaded");
        return ResponseEntity.status(200).body(entity);
    }

    @PostMapping("/predict")
    public ResponseEntity<HashMap> predictPicture(@RequestParam String image) throws IOException, JSONException {
        String encodedImage = image.replace(imageTypePrefix, "");
        byte[] data = Base64.decodeBase64(encodedImage);
        String imageId = UUID.randomUUID().toString();
        Path imagePath;
        imagePath = testPath.resolve(imageId + ".JPG");

        Files.createFile(imagePath);
        Files.write(imagePath, data);

        ProcessBuilder pb_extract = new ProcessBuilder("python3",
                "/home/frieda/Repository/tugas-akhir/classifier_keras/test.py",
                imagePath.toString());
        Process test = pb_extract.start();

        BufferedReader bri = new BufferedReader(new InputStreamReader(test.getInputStream()));
        String line, lastLine="";
        while ((line = bri.readLine()) != null) {
            lastLine = line;
            System.out.println(line);
        }

        HashMap<String, String> entity = new HashMap<>();
        entity.put("msg", lastLine);
        return ResponseEntity.status(200).body(entity);
    }

    @PostMapping("/train")
    public ResponseEntity<HashMap> trainModel() throws IOException, InterruptedException, JSONException {
        System.out.println("masok");
        ProcessBuilder pb_extract = new ProcessBuilder("python3", "/home/frieda/Repository/tugas-akhir/classifier_keras/extract_features.py");
        Process extract = pb_extract.start();

        BufferedReader bri = new BufferedReader(new InputStreamReader(extract.getInputStream()));
        String line;
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        ProcessBuilder pb_train = new ProcessBuilder("python3",
                "/home/frieda/Repository/tugas-akhir/classifier_keras/train.py");

        Process train = pb_train.start();
        bri = new BufferedReader(new InputStreamReader(train.getInputStream()));
        while ((line = bri.readLine()) != null) {
            System.out.println(line);
        }
        bri.close();
        HashMap<String, String> entity = new HashMap<>();
        entity.put("msg", "Trained");
        return ResponseEntity.status(200).body(entity);
    }
}
