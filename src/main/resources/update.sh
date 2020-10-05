#!/bin/sh
getConfiguredClient()
{
  if  command -v curl &>/dev/null; then
    configuredClient="curl"
    echo $configuredClient
  elif command -v wget &>/dev/null; then
    configuredClient="wget"
    echo $configuredClient
  else
    echo "Error: This tool reqires either curl or wget to be installed\." >&2
    return 1
  fi
}
httpGet()
{
  case "$configuredClient" in
    curl)  curl -A curl -s -k "$@" ;;
    wget)  wget -qO- --no-check-certificate "$@" ;;
  esac
}

cacheFiles()
{
  #copy bd and config files to temp folder
  cp /opt/datacollectorlinux/lib/app/config.json /tmp/DataCollectorLinux_tmp_dir
  cp /opt/datacollectorlinux/lib/app/userdb.db /tmp/DataCollectorLinux_tmp_dir

}
restoreCachedFiles()
{
  mv -f /tmp/DataCollectorLinux_tmp_dir/config.json /opt/datacollectorlinux/lib/app/
  mv -f /tmp/DataCollectorLinux_tmp_dir/userdb.db  /opt/datacollectorlinux/lib/app/
}

checkInternet()
{
  httpGet google.com > /dev/null 2>&1 || { echo "Error: no active internet connection" >&2; return 1; } # query google with a get request
}
getLocalVersion()
{
  local_version='9.9.9'
  IFS="="
  while read -r name value
  do
    if [[ "$name" == 'app.version' ]]; then
      # echo "Content of $name is ${value//\"/}"
      local_version=${value//\"/}
      break
    fi
  done < /opt/datacollectorlinux/lib/app/DataCollectorLinux.cfg

  # echo "Local version : $local_version"
  local_version=${local_version//.}
}

getLatestVersion()
{
  latest_version=''
  while IFS= read -r line;
  do
    latest_version+=$line
    echo $line
  done <<< $(httpGet https://innometric.guru:9091/V1/Admin/collector-version?osversion=LINUX)

  if [[ latest_version == '' ]]; then
    latest_version='0.0.0'
  fi

  # echo "Latest version : $latest_version"
  update_zip_url='https://innometric.guru/files/collectors-files/linux_collector/dataCollector_'
  update_zip_url="${update_zip_url}${latest_version}.zip"

  latest_version=${latest_version//.}

}

update()
{
  if [[ $local_version < $latest_version ]]; then

    mkdir -p /tmp/DataCollectorLinux_tmp_dir && cd /tmp/DataCollectorLinux_tmp_dir

    if [ $configuredClient == 'wget' ]
    then
        $configuredClient $update_zip_url --no-check-certificate -q -O m.zip
    else
        $configuredClient-k -s -o m.zip $update_zip_url
    fi

    unzip -qq -o m.zip 
    rm m.zip

    cacheFiles
    #install new version
    deb_file=$(find . -type f -name "*.deb")

    if [[ $deb_file != '' ]]; then
      chmod +x $deb_file
      deb_file=${deb_file//'./'}
      working_dir=$(pwd)
      deb_file="${working_dir}/${deb_file}"
      pkexec apt-get install -f -qq -y $deb_file
    fi
    # restore cached files
    restoreCachedFiles
    pkill -f DataCollectorLinux
    gtk-launch datacollectorlinux-DataCollectorLinux.desktop
  fi
}

#Check for internet and configure http client
getConfiguredClient || exit 1
checkInternet || exit 1

# get version
getLocalVersion || exit 1
getLatestVersion || exit 1
# update to latest version
update || exit 1
