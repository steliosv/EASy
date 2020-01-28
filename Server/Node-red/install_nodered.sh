#!/bin/bash
if [[ $EUID -ne 0 ]]; then
  echo "This script must be run as root"
  exit 1
fi

OS="`uname`"
case $OS in
  'Linux')
  YUM_CMD=$(which yum)
  APT_GET_CMD=$(which apt-get)
  ZYPPER_CMD=$(which zypper)
  DNF_CMD=$(which dnf)
  if [[ ! -z $YUM_CMD ]]; then
    yum install apache2 nodejs
    npm install -g --unsafe-perm node-red
    npm install -g node-red-admin
  elif [[ ! -z $APT_GET_CMD ]]; then
    apt-get install apache2 nodejs
    npm install -g --unsafe-perm node-red
    npm install -g node-red-admin
  elif [[ ! -z $ZYPPER_CMD ]]; then
    zypper install apache2 nodejs
    npm install -g --unsafe-perm node-red
    npm install -g node-red-admin
  elif [[ ! -z $DNF_CMD ]]; then
    dnf install apache2 nodejs
    npm install -g --unsafe-perm node-red
    npm install -g node-red-admin
  else
    echo "Unsupported linux distro. Please make sure that you have installed the following packages: "
    echo "apache2 node-red node-red-admin"
  fi
    chown $USER nodered
    cp nodered /usr/bin/node-red
    echo "When everything is installed generate an SSL certificate and add the full path to the privatekey and certificate inside settings.js"
    echo "run node-red-admin and generate the hash for the given password. place the new administrator password into settings.js"
    echo "and overwrite settings.js inside .node-red directory"
    echo "run sudo nodered start|stop|restart"
    echo "type on your browser https://<URL_NAME>/admin and import flow from dweet.js"
esac
