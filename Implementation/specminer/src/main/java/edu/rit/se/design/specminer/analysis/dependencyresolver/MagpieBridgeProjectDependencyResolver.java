/**
 * @author Peter Mastropaolo
 * This program relies on MagpieBridge to resolve the dependency locations and download them
 * https://github.com/MagpieBridge/MagpieBridge
 */
package edu.rit.se.design.specminer.analysis.dependencyresolver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import magpiebridge.projectservice.java.JavaProjectService;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MagpieBridgeProjectDependencyResolver implements ProjectDependencyResolver{

    /**
     * Finds the dependencies for a project and downloads them, and lists the
     * dependencies in dependencies.spm
     * 
     * @param projectFolderPath The location of the root directory for the project
     * @return the list of dependency locations
     */
    public List<String> findDependencies(String projectFolderPath) throws Throwable{
        List<String> output = new ArrayList<String>(); 
        JavaProjectService test = new JavaProjectService();
        Path root = Paths.get(projectFolderPath).toAbsolutePath();
        String projectName = root.getFileName().toString();
        test.setRootPath(root);
        try {
            System.out.print("\tProcessing " + test.getProjectType() + " Project: " + projectName + " -> ");
            for (Path line : test.getClassPath()) {
                if (line.toString().endsWith(".jar")) {
                    output.add(line.toString());
                }
            }
            System.out.println("Success!");
            return output;
        } catch (NullPointerException e) {
            System.out.println("Skipping " + projectName + " - Not a valid project type");
        } catch (InvalidPathException f) {
            if (Files.exists(root.resolve("pom.xml"))) {
                System.out.println("InvalidPathException: " + projectName + " - Falling back on alternative download method");
                        List<String> fallbackOutput = DependIdentify(projectFolderPath);
                        System.out.println("Fallback-Success! - Not all dependencies may have been resolved!!");
                        return fallbackOutput;
            } else {
                System.out.println("InvalidPathException: Skipping " + projectName);
                return output;
            }
        } catch (Exception g) {
            g.printStackTrace();
            return output;
        }
        return output;
    }

    /**
     * Takes a pom.xml file and locates all maven jar dependencies, putting them in
     * working-directory/output/programname-out.txt
     * 
     * @param projectLoc The directory of the project
     */
    public List<String> DependIdentify(String projectLoc) {
        File XMLFile = new File(projectLoc, "pom.xml");
        List<String> output = new ArrayList<String>(); 
        try { // Creates the doc of the XML file to be viewed

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile); // Creates doc to parse through
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("dependency"); // Checks for the tag dependency in pom.xml

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String localRepo = System.getProperty("user.home") + "\\.m2\\repository\\"; // The default maven
                                                                                                // local repository
                    String dependency = eElement.getElementsByTagName("artifactId").item(0).getTextContent(); // Name of
                                                                                                              // the jar
                                                                                                              // dependency
                    String groupId = eElement.getElementsByTagName("groupId").item(0).getTextContent(); // The groupId
                    String dependLoc = groupId.replace('.', '\\') + "\\" + dependency; // The parent directory of the
                                                                                       // jar dependency when no version
                                                                                       // exists
                    try {
                        String type = eElement.getElementsByTagName("type").item(0).getTextContent();
                        // System.out.println("Skipping " + type + " file"); //Print statement for non
                        // jar files
                    } catch (NullPointerException f) {
                        // Prevents variable dependencies from being processed
                        if (groupId.substring(0, 1).compareTo("$") != 0) {
                            try {
                                String version = eElement.getElementsByTagName("version").item(0).getTextContent(); // Jar
                                                                                                                    // Version
                                                                                                                    // details
                                if (version.substring(0, 1).compareTo("$") == 0) { // If the version is a variable it
                                                                                   // instead locates the latest version
                                    version = null;
                                    String locationNoJar = localRepo + dependLoc + "\\";
                                    String result = DownloadDepend(dependency, groupId, version, locationNoJar);
                                    if (result != null) {
                                        output.add(result);
                                    }
                                } else { // Downloads the dependency with the specified version
                                    String verNoJar = localRepo + dependLoc + "\\" + version + "\\";
                                    String result = DownloadDepend(dependency, groupId, version, verNoJar);
                                    if (result != null) {
                                        output.add(result);
                                    }
                                }

                            } catch (NullPointerException e) {
                                // Downloads the dependency with its latest version
                                String locationNoJar = localRepo + dependLoc + "\\";
                                String version = null;
                                String result = DownloadDepend(dependency, groupId, version, locationNoJar);
                                if (result != null) {
                                    output.add(result);
                                }
                            }

                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return output;
        }
        return output;
    }

    /**
     * Downloads the dependency specified with its specified version. When no
     * version is given it locates the latest version
     * 
     * @param dependency The name of the dependency
     * @param groupId    The groupId
     * @param version    The version (null or specified)
     * @param noJar      The potential path of the file
     * @return The location of the downloaded file or null
     */
    public static String DownloadDepend(String dependency, String groupId, String version, String noJar) {
        if (version == null) { // When there is no version specified
            // System.out.println("No version found: " + dependency + ". Finding the latest
            // version...");
            String mavenScrape = "https://mvnrepository.com/artifact/" + groupId + "/" + dependency; // Web scraping url
                                                                                                     // to find latest
                                                                                                     // version

            WebClient client = new WebClient(); // Scraping setup
            client.getOptions().setJavaScriptEnabled(false);
            client.getOptions().setCssEnabled(false);

            try {
                HtmlPage page = client.getPage(mavenScrape); // The page to look for the latest release
                HtmlAnchor mostRecent = page.getFirstByXPath(".//a[@class='vbtn release']"); // Finds most recent
                                                                                             // version class
                if (mostRecent == null) {
                    mostRecent = page.getFirstByXPath(".//a[@class='vbtn beta']");
                    if (mostRecent == null) {
                        mostRecent = page.getFirstByXPath(".//a[@class='vbtn alpha']");
                        if (mostRecent == null) {
                            client.close();
                            return null;
                        }
                    }
                }
                String newVersion = mostRecent.getAttribute("href")
                        .substring(mostRecent.getAttribute("href").lastIndexOf("/") + 1); // The version
                client.close();
                return DownloadDepend(dependency, groupId, newVersion, noJar + newVersion + "\\"); // Donwloads the
                                                                                                   // latest version
            } catch (IOException e) {
                e.printStackTrace();
                client.close();
                return null;
            } catch (FailingHttpStatusCodeException e) {
                return null;
            }
        } else { // When a version is specified
            String mavenScrape = "https://mvnrepository.com/artifact/" + groupId + "/" + dependency + "/" + version; // Web
                                                                                                                     // scraping
                                                                                                                     // url
            String dependCheck = dependency + "-" + version + ".jar"; // dependency-version.jar
            String repo = ""; // Where to download the jar from
            String fileLoc = noJar + dependCheck; // Full path to the jar file
            File existCheck = new File(fileLoc); // File to check for already downloaded jar

            if (!existCheck.exists()) { // Only download the file when it does not already exist locally
                WebClient client = new WebClient(); // Scraping setup
                client.getOptions().setJavaScriptEnabled(false);
                client.getOptions().setCssEnabled(false);
                try {
                    HtmlPage page = client.getPage(mavenScrape); // Where to find the link for the jar file
                    List<HtmlAnchor> mostRecent = page.getByXPath(".//a[@class='vbtn']"); // Looks at the links in a
                                                                                          // list
                    for (HtmlAnchor fileItem : mostRecent) {
                        String file = fileItem.getAttribute("href")
                                .substring(fileItem.getAttribute("href").lastIndexOf("/") + 1); // The file to check
                        if (file.compareTo(dependCheck) == 0) { // Checks if the link leads to the specified file to be
                                                                // downloaded
                            repo = fileItem.getAttribute("href"); // Sets repo to the link of the file
                            client.close();
                        }
                    }
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    client.close();
                    return null;
                } catch (Exception f) {
                    client.close();
                    return null;
                }

                File dependLoc = new File(noJar); // The location where the jar will be located
                dependLoc.mkdirs(); // Creates the path to store the jar file
                try { // Downlaods the file
                    //System.out.println(dependCheck + " downloading...");
                    //System.out.println("Repo: " + repo);
                    URL url = new URL(repo); // The URL to download
                    ReadableByteChannel readable = Channels.newChannel(url.openStream());
                    FileOutputStream fileOut = new FileOutputStream(fileLoc);
                    fileOut.getChannel().transferFrom(readable, 0, Long.MAX_VALUE);
                    fileOut.close();
                } catch (FileNotFoundException e) {
                    System.out.println("Dependency repo not found: " + dependency + " Version: " + version
                            + ": download unsuccessful\n");
                    return fileLoc;
                } catch (Exception e) {
                    e.printStackTrace();
                    return fileLoc;
                }
            } else {
                //System.out.println(dependCheck + " already exists, skipping download");
                return fileLoc;
            }
            return fileLoc;
        }
    }
}
