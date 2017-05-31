package bootsample.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    String store(MultipartFile file);


    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
