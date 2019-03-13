package org.superbiz.moviefun.albums;

import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import org.superbiz.moviefun.blobstore.*;
import org.superbiz.moviefun.s3.S3Store;
import sun.nio.ch.IOUtil;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    public static long albumId = -1;

    private final AlbumsBean albumsBean;

    @Autowired
    private  BlobStore blobStore;


    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

        Blob blob = new Blob("covers/"+albumId,uploadedFile.getInputStream(),uploadedFile.getContentType());

        blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        Blob coverFilePathBlob = getExistingCoverPath(albumId);

        byte[] imageBytes = IOUtils.toByteArray(coverFilePathBlob.inputStream);

        HttpHeaders headers = createImageHttpHeaders(coverFilePathBlob, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }


    private void saveUploadToFile1(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();


        //FileStore store = new FileStore();
        //store.put(b1);
    }

    private HttpHeaders createImageHttpHeaders(Blob coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath.inputStream);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }

    private File getCoverFile11(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
    }

    private File getCoverFile(@PathVariable long albumId) {
        //FileStore fs = new FileStore();

        String coverFileName = format("covers/%d", albumId);
        return new File(coverFileName);
        /*try {
            Optional<Blob> opt = blobStore.get(coverFileName);
            Blob b = opt.get();
            return new File(b.name);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;*/
    }

    private Blob getExistingCoverPath(long albumId) throws URISyntaxException, IOException {

        String coverFileName = format("covers/%d", albumId);

            Optional<Blob> opt = blobStore.get(coverFileName);
            if(opt.isPresent()){
                return opt.get();
            }else{
                Tika tika = new Tika();
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("default-cover.jpg");
                byte[] bytes = IOUtils.toByteArray(is);

                String contentType = tika.detect(bytes);
                return new Blob("default-cover",new ByteArrayInputStream(bytes),contentType);
            }

        }

}
