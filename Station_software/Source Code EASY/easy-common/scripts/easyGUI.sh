#!/bin/bash
xhost +local:
export DISPLAY=:0
if [[ $EUID -ne 0 ]]; then
  echo "This script must be run as root"
  exit 1
fi
. utils/Linux-MacOSX/discover.sh
getXbee xbee
getDigitiser serial
YUM_CMD=$(which yum)
APT_GET_CMD=$(which apt-get)
ZYPPER_CMD=$(which zypper)
DNF_CMD=$(which dnf)
printf "%-50s" "Found digitiser at:"
printf "${grn}%-s${end}\n" "$digitiser"
printf "%-50s" "Found XBee module at:"
printf "${grn}%-s${end}\n" "$xbee"
java -jar easy-common.jar
