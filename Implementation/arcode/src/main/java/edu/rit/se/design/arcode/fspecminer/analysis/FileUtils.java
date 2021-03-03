package edu.rit.se.design.arcode.fspecminer.analysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class FileUtils {
    public static List<String> getAllJarFilesInDirectory(String folder ) throws IOException {
        List<String> foundFiles = new ArrayList<>();
        getAllFilesInDirectory(new File(folder), ".jar", foundFiles);
        return foundFiles;
    }

    static void getAllFilesInDirectory(File currentFolder, String extension, List<String> foundFiles ) throws IOException {
        for (File file : currentFolder.listFiles()) {
            if( file.isDirectory() )
                getAllFilesInDirectory( file, extension, foundFiles );
            else
                if( file.getName().endsWith( extension ) )
                    foundFiles.add( file.getPath() );
        }
    }

}
