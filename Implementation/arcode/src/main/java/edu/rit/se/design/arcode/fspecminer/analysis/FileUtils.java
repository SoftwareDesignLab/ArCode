/*
 * Copyright (c) 2021 - Present. Rochester Institute of Technology
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
