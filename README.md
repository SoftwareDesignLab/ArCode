This is the home of **ArCode**, a tool that facilitates the use of application frameworks to implement tactics and patterns. 

You may cite ArCode software using its DOI as: [![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.4581117.svg)](https://doi.org/10.5281/zenodo.4581117) 

For instance, if you use Bibtex citation formating, you may cite ArCode software as:

```
@software{shokri_ali_2021_4581117,
  author       = {Shokri, Ali and
                  Santos, Joanna C. S. and
                  Mirakhorli, Mehdi},
  title        = {ArCode-1.2.1},
  month        = mar,
  year         = 2021,
  publisher    = {Zenodo},
  version      = {1.2.1},
  doi          = {10.5281/zenodo.4581117},
  url          = {https://doi.org/10.5281/zenodo.4581117}
}
```

For other citation formats please follow this [link](https://doi.org/10.5281/zenodo.4581116).

Moreover, you may find more technical details about ArCode from its [technical paper](https://arxiv.org/abs/2102.08372).

# What is ArCode?
ArCode is a tool for analyzing programs written in Java, finding how APIs of a framework are being used in those programs, and providing recommendations on either how to correct possible API misuses or how to complete an incomplete program (w.r.t. APIs of that framework).
There are three main inputs to ArCode:

1. Framework jar file
2. The path to training projects' directory
3. The path to testing project's directory

ArCode leverages static analysis techniques to extract some facts about API dependencies and usage constraint from the framework. It then analyzes programs in the training repository to identify programs that are not violating API usage constraint. Mining these programs, it builds an API usage model, called Framework API Specification (FSpec) model. Finally, it analyzes testing projects to find how APIs are being used in them and provides recommendations on how to fix or complete (if needed) those programs.

If you are interested, you may find more details about ArCode from its [technical paper](https://arxiv.org/abs/2102.08372).

# Cite the research paper
To use ArCode in your research papers, please cite it as: BibTex

# Obtaining ArCode tool
In this section, we provide a step-by-step instruction to walk you through the process of building and using ArCode.

## Use a released version
When we reach a stable version, we build the project and release it as a jar file. You can easily download the latest release from [here](../../releases/). 

## Building ArCode from source code
In order to build a runnable version of ArCode from the source code, you need to have Maven3 installed on your machine. The link to Maven can be found from [here](https://maven.apache.org/).
Please use below command to build the ArCode runnable jar file once you have installed maven:
```
mvn clean package
```

If the process finishes with success, ArCode jar file (e.g. arcode-1.0-SNAPSHOT.jar) is created and is ready to be used.

# Running the tool
For running ArCode, you may use the following template in the command line. In the next section, a tutorial of runing the tool is provided.
```
java -jar ARCODE_JAR_FILE -framework FRAMEWORK_OF_INTEREST -fspecOutputPath PATH_TO_FSPEC_OUTPUT -trainProjectsPath PATH_TO_TRAINING_PROJECTS -testProjectsPath PATH_TO_TESTING_PROJECTS -exclusionFilePath PATH_TO_EXCLUSION_FILE -frameworkJarPath PATH_TO_FRAMEWORK_JAR_FILE -frameworkPackage PACKAGE_NAME_OF_FRAMEWORK
```
- **ARCODE_JAR_FILE:** Path to ArCode jar file that you downloaded from [releases](../../releases/) or built from ArCode source code using Maven.
- **FRAMEWORK_OF_INTEREST:** The framework that you would like to extract and create a model of its API usages in training projects. This model is called Framework API Specification model (FSpec). ArCode also performs static analysis to find dependencies between APIs inside the framework jar file and creates an Inter-framewrok Dependency model (IFD). You may find more details about the approach from ArCode [paper](https://arxiv.org/abs/2102.08372).
- **PATH_TO_FSPEC_OUTPUT:** Created FSpec will be serialized and saved in this directory. ArCode will later restore this FSpec and use it in the recommendation phase.
- **PATH_TO_TRAINING_PROJECTS:** Path to the bytecode (jar files) of the training projects that are incorporating the framework of interest. ArCode analyzes these programs to create a Graph-based API Usage Model (GRAAM) for each program. Then, performing graph-based operations, it creates the framework's FSpec. 
- **PATH_TO_TESTING_PROJECTS:** Path to the bytecode (jar files) of the testing projects that are incorporating the framework of interest. ArCode analyzes these programs to create a Graph-based API Usage Model (GRAAM) for each program. The, leveraging the previously created FSpec, it identifies API misuses or APIs that are needed to be added to the program to make the program a correct implementation of an architectural tactic or a pattern.
- **PATH_TO_EXCLUSION_FILE:** This is a file entitled as "JAASJavaExclusions.txt" in the config folder of ArCode source code. In the current version of ArCode, you need to have that file on your system and introduce its path to ArCode. This file specifies packages to be excluded from program analysis process. We need to introduce these packages to avoid explosion of call-graph creation in that process.
- **PATH_TO_FRAMEWORK_JAR_FILE:** Path to jar file of the framework of interest.
- **PACKAGE_NAME_OF_FRAMEWORK:** Since there might be more than one framework in a jar file, we introduce the packaging of the framework through this property. For instance, in case of JAAS framework, this package would be javax/security/auth. Also, framework package for RMI would be java/rmi.

## Running the tool on a Windows-based machine
Assuming that you are using a Windows-based machine, here are the steps you need to follow to be able to run the tool for JAAS framework:
1. Cloning the source code in a path (e.g. on drive D):
```
D:\>git clone https://github.com/SoftwareDesignLab/ArCode.git
```
2. Building the tool:
```
D:\ArCode\Implementation\arcode\>mvn clean package
```
3. Running the tool:
```
D:\ArCode\Implementation\arcode\target>java -jar arcode-1.0-SNAPSHOT-jar-with-dependencies.jar -framework JAAS -fspecOutputPath D:\ArCode\DataRepository\JAAS -trainProjectsPath D:\ArCode\DataRepository\JAAS\Train -testProjectsPath D:\ArCode\DataRepository\JAAS\Test -exclusionFilePath D:\ArCode\Implementation\arcode\config\JAASJavaExclusions.txt -frameworkJarPath D:\ArCode\Frameworks\JAAS.jar -frameworkPackage javax/security/auth
```
Also, in case that you want to run the tool for RMI framework, you can easily run the followig command:
```
D:\ArCode\Implementation\arcode\target>java -jar arcode-1.0-SNAPSHOT-jar-with-dependencies.jar -framework RMI -fspecOutputPath D:\ArCode\DataRepository\RMI -trainProjectsPath D:\ArCode\DataRepository\RMI\Train -testProjectsPath D:\ArCode\DataRepository\RMI\Test -exclusionFilePath D:\ArCode\Implementation\arcode\config\JAASJavaExclusions.txt -frameworkJarPath D:\ArCode\Frameworks\RMI.jar -frameworkPackage java/rmi
```

## Running the tool on a Mac OS:
Assuming that you are using a Mac machine, here are the steps you need to follow to be able to run the tool for JAAS framework:
1. Cloning the source code in a path (e.g. on drive D):
```
/Users/YOUR_USERNAME/Desktop/ArCode$ git clone https://github.com/SoftwareDesignLab/ArCode.git
```
2. Building the tool:
```
/Users/YOUR_USERNAME/Desktop/ArCode/Implementation/arcode$ mvn clean package
```
3. Running the tool:
```
/Users/YOUR_USERNAME/Desktop/ArCode/Implementation/arcode/target$ java -jar arcode-1.0-SNAPSHOT-jar-with-dependencies.jar -framework JAAS -fspecOutputPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/JAAS -trainProjectsPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/JAAS/Train -testProjectsPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/JAAS/Test -exclusionFilePath /Users/YOUR_USERNAME/Desktop/ArCode/Implementation/arcode/config/JAASJavaExclusions.txt -frameworkJarPath /Users/YOUR_USERNAME/Desktop/ArCode/Frameworks/JAAS.jar -frameworkPackage javax/security/auth
```
Also, in case that you want to run the tool for RMI framework, you can easily run the followig command:
```
/Users/YOUR_USERNAME/Desktop/ArCode/Implementation/arcode/target$ java -jar arcode-1.0-SNAPSHOT-jar-with-dependencies.jar -framework RMI -fspecOutputPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/RMI -trainProjectsPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/RMI/Train -testProjectsPath /Users/YOUR_USERNAME/Desktop/ArCode/DataRepository/RMI/Test -exclusionFilePath /Users/YOUR_USERNAME/Desktop/ArCode/Implementation/arcode/config/JAASJavaExclusions.txt -frameworkJarPath /Users/YOUR_USERNAME/Desktop/ArCode/Frameworks/RMI.jar -frameworkPackage java/rmi
```

# 
If you faced out of memory or heap-related exceptiones, you may need to increase the run-time memory by adding the following option to the above command:
```
-Xms512M -Xmx512M
```

While running the tool, there might be some WARNING logs in the console. As long as you are not receiving SEVERE, ERROR, or Exception messages you should be fine.

After running ArCode, four folders in training as well as testing projects' directories will be created.
* *GRAAMsDotGraph:* Contains generated GRAAMs for each project in the format of [dot graph](https://en.wikipedia.org/wiki/DOT_(graph_description_language)). To visualize each GRAAM, you can use a dot graph visualizer tool. For instance, one can copy-paste content of a file in *GRAAMsDotGraph* folder in the textbox area in [Webgraphviz](http://www.webgraphviz.com/) website and click the "Generate Graph" button.
* *PAUGsDotGraph:* Contains generated Primary API Usage Graphs for each project in the format of [dot graph](https://en.wikipedia.org/wiki/DOT_(graph_description_language)). To visualize each Primary API Usage Graph, you can use a dot graph visualizer tool as mentioned before.
* *SerializedGRAAMs:* ArCode saves generated GRAAMs in the format of serialized objects in this folder and restores them later.
* *SerializedPAUGs:* ArCode saves generated Primary API Usage Graphs in the format of serialized objects in this folder and restores them later.

ArCode uses GRAAMs generated for training programs to create the FSpec of JAAS. Then, it generates API misuses from GRAAMs of test data set. Finally, it provides recommendations for generated test cases and computes the accuracy of top-K recommendations. 

# Tutorial
Please click [here](https://youtu.be/f1AmjF3uIVw) to watch a tutorial on ArCode.

# Contact
Please do not hasitate to reach out to us should you have any question about ArCode. You may get in contact with us throught as8308 at rit dot edu.
