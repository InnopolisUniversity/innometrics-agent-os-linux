# Frequently Asked Questions

- [Installation problem xdg-desktop-menu: No writable system menu directory found](#installation-problem-xdg-desktop-menu-no-writable-system-menu-directory-found)
- [How to install linux data collector?](#how-to-install-linux-data-collector)
- [How to uninstall linux data collector?](#how-to-uninstall-linux-data-collector)
- [What are the system Requirements for Linux data collector?](#what-are-the-system-requirements-for-linux-data-collector)
- [Non-writable settings file error](#non-writable-settings-file-error)


## Installation problem xdg-desktop-menu: No writable system menu directory found


```console
foo@bar:~$ bash install.sh
Setting up datacollectorlinux (1.0.1-1) ...
xdg-desktop-menu: No writable system menu directory found.
dpkg: error processing datacollectorlinux (--configure):
 subprocess installed post-installation script returned error exit status 3
Errors were encountered while processing:
 datacollectorlinux
E: Sub-process /usr/bin/dpkg returned an error code (1)
```
It is a bug in xdg. You can easily solve this yourself:

-  creating the folder xdg is looking for : `sudo mkdir /usr/share/desktop-directories/`
- Clean the apt-get : `sudo apt-get clean`
- Install data collector : `bash install.sh`
- [More Info about the xdg bug](https://askubuntu.com/questions/405800/installation-problem-xdg-desktop-menu-no-writable-system-menu-directory-found)

## How to install linux data collector?

Follow this steps :
1. Download the Linux Innometrics installer from [innometrics.ru](https://innometrics.ru/index.html).
1. If you receive an error that the App is from an unidentified developer, please follow [this](https://innopolis.ru) guide to resolve the issue.
1. Unpack the downloaded installer `.zip` file.
1. Run the installation using one of the following ways: by navigating to the unpacked directory using one of the ways listed below.
  1. <strong>Using terminal</strong> : navigate to the unpacked directory. Execute the following command start installation : `bash install.sh`.
  1. <strong>Using Nautilus</strong> : navigate to the unpacked directory. Double click on the `.deb` file to open the software center, where you should see the option to install the software. After successful installation in terminal execute the following command : `sudo chmod -R 0777 /opt/datacollectorlinux/lib/app`.

## What are the system Requirements for Linux data collector?

- `glibc:` version 2.12 or higher
- `GCC: ` 7 or greater
- Debian-based linux distribution 64 bits
- `Processor:` 2 gigahertz (GHz) or faster
- `RAM:` 2 GB (64-bit)
- `Hard disk space:` 16 GB (32-bit) or 20 GB (64-bit)
- `Windowing system for bitmap displays:` X11


## How report bugs?

Contact the developers or submit an issue on GitHub : [Link](https://github.com/InnopolisUniversity/innometrics-agent-os-linux/issues)

## How to uninstall linux data collector?

`sudo apt remove datacollectorlinux`


## Non-writable settings file error

You can fix this error by opening the terminal and executing the following command :

- `sudo chmod -R 0777 /opt/datacollectorlinux/lib/app`
