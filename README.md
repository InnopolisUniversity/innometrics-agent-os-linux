# Innometrics linux data collector

Innometrics data collector implementation for Linux OS. It is based on Javafx, JNA and X Window System.

#### -- Project Status: [Active]

## Requirements

1. GCC >7+

## Installation & Unistallation

Ubuntu:

1. Download the Linux Innometrics installer from [innometrics.ru](https://innopolis.ru).
2. If you receive an error that the App is from an unidentified developer, please follow [this](https://innopolis.ru) guide to resolve the issue.
3. Run the installation file <br>
` sudo apt install path_to_dataCollector_deb_file` or `sudo dpkg -i path_to_dataCollector_deb_file` <br> If you get a dependency error while installing the deb packages, you can use the following command to fix it: <br>
`sudo apt install -f`
4. To uninstall : <br>
`sudo apt remove dataCollector`
## Getting up & running

Ask your supervisor or your manager about your credentials. Once you run the app, it will pop open in the status bar (at the top). Clicking on the icon and then clicking on `Click to Log in` will open a window so you can input your credentials.

After clicking on `Log In`, the collector should automatically start working. Clicking on the app icon again will pop open a menu where you can see all the details about your session.

## Release & versions

* [Version 1.0.1]()
* [Version 1.0.2]()


## Features

* Retrives active window information (duration, process ID, etc..)


## Contributions
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change or improve.


## How to Run the project ?
#### On [IntelliJ IDEA](https://www.jetbrains.com/idea/)
1. File -> Project Structure -> Project
1. Set Project SDK to 11.x (better use AdoptOpenJDK 11) 
1. To be able to package get the following SDKs : 
    * <b>[AdoptOpenJDK](https://adoptopenjdk.net/) (`sudo apt install adoptopenjdk-11-hotspot`)</b>
    * <b>[openjdk 14](https://jdk.java.net/14/) </b> 
1. Apply setings.
1. <b>To Run the app</b> : Click open Gradle tab on the far right, navigate to build `InnoMatrixLinux -> Tasks -> build` then double click the `build` file. After successfull build, expand the `application` folder and double click the `run` file to start the application.
1. <b>To Run the test</b> : Click open Gradle tab on the far right, navigate to build `InnoMatrixLinux -> Tasks -> build` then double click the `build` file. After successfull build, expand the `verfication` folder and double click the `test` file to start the tests in the test directory.



## Repository directory layout

    .
    ├── gradle              # contains gradle wrapper properties
    ├── src                 # Source code files
    │   ├── main            # data collector source files
    │   ├── test            # tests files
    │   └── README.md           
    ├── .gitignore
    ├── build.gradle  
    ├── gradlew          
    ├── gradlew.bat      
    ├── settings.gradle   
    └── README.md

## Contact
If you would like to get in touch, please contact: <br/>
[Gcinizwe Dlamini](https://github.com/Gci04)

Copyright (c) 2020 Innopolis University - All rights reserved.
