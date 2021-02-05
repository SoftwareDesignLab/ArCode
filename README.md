This is the home of ArCode, a tool that facilitates the use of application frameworks to implement tactics and patterns. 

# What is ArCode?
ArCode is a tool for analyzing programs written in Java, finding how APIs of a framework are being used in those programs, and providing recommendations on either how to correct possible API misuses or how to complete an incomplete program (w.r.t. APIs of that framework).
There are three main inputs to ArCode:

1. Framework jar file
2. The path to training projects' directory
3. The path to testing project's directory

ArCode leverages static analysis techniques to extract some facts about API dependencies and usage constraint from the framework. It then analyzes programs in the training repository to identify programs that are not violating API usage constraint. Mining these programs, it builds an API usage model, called Framework API Specification (FSpec) model. Finally, it analyzes testing projects to find how APIs are being used in them and provides recommendations on how to fix or complete (if needed) those programs.

If you are interested, you may find more details about ArCode from its [technical paper](https://a-shokri.github.io/assets/Ali%20Shokri-ArCode-ICSA2021-Accepted.pdf).
A link to the presentation of this paper will be uploaded shortly.

The source code and related documentation will be uploaded shortly.

# Cite the research paper
To use ArCode in your research papers, please cite it as: BibTex

# Obtaining ArCode tool
In this section, we provide a we provide a step-by-step instruction to walk you through the process of building and using ArCode.

## Use a released version
When we reach a stable version, we build the project and release it as a jar file. You can easily download the latest release from [here](releases/). 

## Building ArCode from source code
In order to build a runnable version of ArCode from the source code, you need to have Maven3 installed on your machine. The link to Maven can be found from [here](https://maven.apache.org/).
Please use below command to build the ArCode runnable jar file once you have installed maven:
```
mvn clean package
```

If the process finishes with success, ArCode jar file (e.g. arcode-1.0-SNAPSHOT.jar) is created and is ready to be used.

# Running the tool



# Tutorial
Link to the tutorial video will be uploaded shortly.

# Contact
Please do not hasitate to reach out to us should you have any question about ArCode. You may get in contact with us throught as8308 at rit dot edu.
