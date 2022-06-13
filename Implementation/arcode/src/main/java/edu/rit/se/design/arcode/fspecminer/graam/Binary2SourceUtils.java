/*
 * Copyright (c) 2022 - Present. Rochester Institute of Technology
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

package edu.rit.se.design.arcode.fspecminer.graam;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Ali Shokri (as8308@rit.edu)
 */

public class Binary2SourceUtils {

    public static void main(String[] args) throws IOException {
        String jarFilePath = "/Users/as8308/Downloads/fernflower/fernflower/build/libs/fernflower.jar";
        String javaClassPath = "org.jetbrains.java.decompiler.main.Fernflower";
        String methodSignature = "public String getClassContent(StructClass)";
//        String decompiledDir = "/var/folders/y1/8tzmh6w95w12bb0039xb5pkjr53byk/T/decompiler_test_11883239906282682932_dir/decompiled";//decompilationUtil.decompileJarFile( jarFilePath);
//        StringBuilder foundMethod = decompilationUtil.findMethod( decompiledDir, javaFilePath, methodSignature );
        StringBuilder foundMethod = retrieveMethodBody( jarFilePath, javaClassPath, methodSignature );
        return;
    }

    public static StringBuilder retrieveMethodBody( String jarFilePath, String javaClassPath, String methodSignature ) throws IOException {
        Binary2SourceUtils decompilationUtil = new Binary2SourceUtils();
        String decompiledDir = decompilationUtil.decompileJarFile( jarFilePath);
        String javaFilePath = javaClassPath.replaceAll("\\.", "/") + ".java";
        StringBuilder foundMethod = decompilationUtil.findMethod( decompiledDir, javaFilePath, methodSignature );
        return foundMethod;
    }

    public static StringBuilder findMethod( String sourceDir, IMethod iMethod ) throws FileNotFoundException {
        String originalClassPackage = iMethod.getDeclaringClass().getName().getPackage() != null ? iMethod.getDeclaringClass().getName().getPackage().toString() : null;
        String originalClassSimpleName = iMethod.getDeclaringClass().getName().getClassName().toString();
        String originalClassFullName = (originalClassPackage != null ? originalClassPackage + "/" : "") + originalClassSimpleName;

        String methodName = iMethod.getSelector().getName().toString();
        StringBuilder methodParameters = new StringBuilder();
        for (TypeName parameter : iMethod.getSelector().getDescriptor().getParameters()) {
            String paramClassName = parameter.getClassName().toString();
            if( parameter.toString().startsWith("[") )
                paramClassName += "[]";
            methodParameters.append((methodParameters.length() > 0 ? ", " : "") + paramClassName);
        }

        String methodSignature = methodName + "(" + methodParameters.toString() + ")";
        return findMethod( sourceDir, originalClassFullName + ".java",  methodSignature);
    }

    public static StringBuilder findMethod( String sourceDir, String javaFilePath, String methodSignature ) throws FileNotFoundException {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLanguageLevel( ParserConfiguration.LanguageLevel.JAVA_11 );

        JavaParser javaParser = new JavaParser( parserConfiguration );

        ParseResult<CompilationUnit> parseResult = javaParser.parse( new File(sourceDir + File.separator + javaFilePath) );
        CompilationUnit compilationUnit = parseResult.getResult().get();

        StringBuilder foundMethod = new StringBuilder();

        compilationUnit.getChildNodesByType(MethodDeclaration.class).stream().filter(
                methodDeclaration -> methodSignature.contains( methodDeclaration.getSignature().asString() )
        ).forEach(md->{
            if( foundMethod.length() == 0 )
                foundMethod.append( md.toString() );
        });

        return foundMethod;
    }

    public static String decompileJarFile( String jarFilePath) throws IOException {
        File tempDir = File.createTempFile("decompiler_test_", "_dir");
        tempDir.delete();

        File targetDir = new File(tempDir, "decompiled");
        targetDir.mkdirs();

        File classes = new File(tempDir, "classes");
        classes.mkdirs();
        unpack(new File(jarFilePath), classes);

        TestConsoleDecompiler decompiler = setUp(targetDir);
        decompiler.addSource( classes );
        decompiler.decompileContext();

        return targetDir.getAbsolutePath();
    }

    private static TestConsoleDecompiler setUp(File decompilationFolder) throws IOException {

        Map<String, Object> options = new HashMap<>();
        options.put(IFernflowerPreferences.LOG_LEVEL, "warn");
        options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
        options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "1");
        options.put(IFernflowerPreferences.REMOVE_BRIDGE, "1");
        options.put(IFernflowerPreferences.LITERALS_AS_IS, "1");
        options.put(IFernflowerPreferences.UNIT_TEST_MODE, "1");

        return new TestConsoleDecompiler(decompilationFolder, options);
    }

    private static class TestConsoleDecompiler extends ConsoleDecompiler {
        private final HashMap<String, ZipFile> zipFiles = new HashMap<String, ZipFile>();

        TestConsoleDecompiler(File destination, Map<String, Object> options) {
            super(destination, options, new PrintStreamLogger(System.out));
        }

        @Override
        public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
            File file = new File(externalPath);
            if (internalPath == null) {
                return InterpreterUtil.getBytes(file);
            }
            else {
                ZipFile archive = zipFiles.get(file.getName());
                if (archive == null) {
                    archive = new ZipFile(file);
                    zipFiles.put(file.getName(), archive);
                }
                ZipEntry entry = archive.getEntry(internalPath);
                if (entry == null) throw new IOException("Entry not found: " + internalPath);
                return InterpreterUtil.getBytes(archive, entry);
            }
        }

        void close() {
            for (ZipFile file : zipFiles.values()) {
                try {
                    file.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            zipFiles.clear();
        }
    }

    private static void unpack(File archive, File targetDir) {
        try (ZipFile zip = new ZipFile(archive)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    File file = new File(targetDir, entry.getName());
                    file.getParentFile().mkdirs();
                    try (InputStream in = zip.getInputStream(entry); OutputStream out = new FileOutputStream(file)) {
                        InterpreterUtil.copyStream(in, out);
                    }
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
