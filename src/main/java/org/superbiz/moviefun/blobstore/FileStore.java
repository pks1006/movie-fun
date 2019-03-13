package org.superbiz.moviefun.blobstore;

import javassist.bytecode.ByteArray;
import org.hibernate.engine.jdbc.BinaryStream;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.albums.AlbumsController;

import java.io.*;
import java.net.URLConnection;
import java.util.Optional;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        // ...

        File targetFile = new File(String.format("covers/%d", AlbumsController.albumId));

        try (
                FileOutputStream outputStream = new FileOutputStream(targetFile)) {

                InputStream is = blob.inputStream;
                int length = is.available();

                byte[] bytes = new byte[length];

                is.read(bytes);

                outputStream.write(bytes);
        }
    }



    @Override
    public Optional<Blob> get(String name) throws IOException {
        // ...
        File file = new File(name);
        InputStream is = new FileInputStream(file);

        Blob blob = new Blob(file.getPath(),is,"");

        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {
        // ...
    }
}