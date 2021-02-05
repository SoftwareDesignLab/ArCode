package edu.rit.se.design.arcode.fspecminer.analysis.dependencyresolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Peter Mastropaolo, Ali Shokri (as8308@rit.edu)
 */
public class MavenProjectDependencyResolver implements ProjectDependencyResolver {
    /**
     * When given a directory, it checks all folders inside it for a pom file to detect maven projects, and performs the dependency check on all projects
     * File MainDir: The directory to check for projects in
     */
    public List<String> findDependencies(String projectFolderPath) throws Throwable {
        File projectFolder = new File( projectFolderPath );
        if (projectFolder.isDirectory()){
            String findPom = projectFolder + "/pom.xml"; //String version of a potential Location for a pom.xml file
            //System.out.println(findPom); //Used for testing
            File posPom = new File(findPom); //Potential Location for a pom.xml file
            return findDependencies(posPom); //Gets the project dependencies
        }
        throw new Throwable("POM file not found!");
    }

    /**
     * Takes a pom.xml file and locates all maven jar dependencies, putting them in working-directory/output/programname-out.txt
     * String project: The name of the project being analysed
     * File XMLFile: The pom.xml being analysed
     */
    static List<String> findDependencies(File XMLFile){
        List<String> dependencies = new ArrayList<>();
        try{
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(XMLFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("dependency"); //Checks for the tag dependency in pom.xml

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String userHome = System.getProperty("user.home"); //User home location
                    String localRepo = userHome + "/.m2/repository/"; //The default maven local repository
                    String dependency = eElement.getElementsByTagName("artifactId").item(0).getTextContent(); //Name of the jar dependency
                    String dependLoc = eElement.getElementsByTagName("groupId").item(0).getTextContent().replace('.','/') + "/" + dependency; //The parent directory of the jar dependency when no version exists
                    //System.out.println("Dependency: " + dependency); //Used for testing

                    try{
                        String version = eElement.getElementsByTagName("version").item(0).getTextContent(); //Jar Version details
                        String verLoc = localRepo + dependLoc + "/" + version + "/" + dependency + "-" + version + ".jar"; //The parent directory of the jar dependency when version exists
                        //System.out.println("File Location: " + verLoc + "\n"); //Used for testing
                        dependencies.add(verLoc);
                    }
                    catch(NullPointerException e){
                        String location = localRepo + dependLoc + "/" + dependency + ".jar";
                        //System.out.println("File Location: " + location + "\n"); //Used for testing
                        dependencies.add(location);
                    }
                }
            }
        }
        catch (FileNotFoundException e){
            System.out.println("No File Found"); //Used for testing
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return dependencies;
    }
}
