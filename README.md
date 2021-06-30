# Innometrics linux data collector

Innometrics data collector implementation for Linux OS. It is based on Javafx, JNA and X Window System.

#### -- Project Status: [Active]

## Requirements

1. `glibc:` version 2.12 or higher
1. `GCC: ` 7 or greater
1. Debian-based or Arch-based linux distribution (64 bits)
1. `Processor:` 2 gigahertz (GHz) or faster
1. `RAM:` 2 GB (64-bit)
1. `Hard disk space:` 16 GB (32-bit) or 20 GB (64-bit)
1. `Windowing system for bitmap displays:` X11

## Installation & Unistallation

Ubuntu (64 bits):

1. Download the Linux Innometrics installer from [innometrics.ru](https://innometrics.ru/index.html).
1. If you receive an error that the App is from an unidentified developer, please follow [this](https://innopolis.ru) guide to resolve the issue.
1. Unpack the downloaded installer `.zip` file.
1. Run the installation using one of the following ways: by navigating to the unpacked directory using one of the ways listed below.
  1. <strong>Using terminal</strong> : navigate to the unpacked directory. Execute the following command start installation : `bash install.sh`.
  1. <strong>Using Nautilus</strong> : navigate to the unpacked directory. Double click on the `.deb` file to open the software center, where you should see the option to install the software. After successful installation in terminal execute the following command : `sudo chmod -R 0777 /opt/datacollectorlinux/lib/app`.
1. If you get a dependency error while installing the deb packages, you can use the following command to fix it: <br>
`sudo apt install -f` or see the [FAQ](https://innometrics.ru/index.html) to solve the issue.
1. To uninstall : <br>
`sudo apt remove datacollectorlinux`

Arch based Systems (64 bits):

1. Upgrade your system.
1. Install base-devel package from community repos(use [pacman](https://wiki.archlinux.org/index.php/Pacman) or any other package manager you prefer)
1. Download PKGBUILD [Here](https://innometric.guru/files/collectors-files/linux_collector/Arch/PKGBUILD)
1. Execute makepkg -fs PKGBUILD. During installation choose java 11
1. Run `sudo pacman -U innometrics-agent-os-linux-[VERSION]-1-x86_64.pkg.tar.xz`
1. To finish the installation execute : `sudo chmod -R 777 /opt/datacollectorlinux/lib/app`

## Getting up & running

Ask your supervisor or your manager about your credentials. Once you run the application and input your credentials to login, the collector should automatically start working. Clicking on the application icon you can see all the details about your session.

## Release & versions

* [Version 1.0.1]()
* [Version 1.0.2]()
* [Version 1.0.3]()
* [Version 1.0.4]()
* [Version 1.0.5](Versions.md)
* [Version 1.0.6](Versions.md)


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
[Gcinizwe Dlamini](https://github.com/Gci04)<br>
[Vladimir Bazilevich](v.bazilevich@innopolis.university)<br><br>
Copyright (c) 2020 Innopolis University - All rights reserved.
