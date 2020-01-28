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
    yum install rxtx gpsd gpsd-clients ntp
  elif [[ ! -z $APT_GET_CMD ]]; then
    apt-get install librxtx-java gpsd gpsd-clients ntp
  elif [[ ! -z $ZYPPER_CMD ]]; then
    zypper install rxtx-java  gpsd gpsd-clients ntp
  elif [[ ! -z $DNF_CMD ]]; then
    dnf install rxtx gpsd gpsd-clients ntp
  else
    echo "Unsupported linux distro. Please make sure that you have installed the following packages: "
    echo "librxtx-java gpsd gpsd-clients ntp"
  fi
  if [[ ! -z $YUM_CMD ]]; then
    systemctl stop chronyd.service
    systemctl disable chronyd.service
    systemctl enable gpsd.service
    systemctl enable ntpd.service
  elif [[ ! -z $APT_GET_CMD ]]; then
    systemctl stop chronyd.service
    systemctl disable chronyd.service
    systemctl enable gpsd.service
    systemctl enable ntpd.service
  elif [[ ! -z $ZYPPER_CMD ]]; then
    systemctl stop chronyd.service
    systemctl disable chronyd.service
    systemctl enable gpsd.service
    systemctl enable ntpd.service
  elif [[ ! -z $DNF_CMD ]]; then
    systemctl stop chronyd.service
    systemctl disable chronyd.service
    systemctl enable gpsd.service
    systemctl enable ntpd.service
  else
    echo "Unsupported linux distro. Please make sure that you have enabled NTP and GPSD services "
  fi
  # perl -i -pe "BEGIN{undef $/;} s/^\[Service\]$/[Service\]\nRestart=always/sgm" /etc/systemd/system/multi-user.target.wants/ntpd.service
  # perl -i -pe "BEGIN{undef $/;} s/^\[Service\]$/[Service\]\nRestart=always/sgm" /etc/systemd/system/multi-user.target.wants/gpsd.service
  #  echo "# Options for gpsd, including serial devices" > /etc/sysconfig/gpsd
  echo "OPTIONS=\"-n \""  > /etc/sysconfig/gpsd
  #  echo "# Set to 'true' to add USB devices automatically via udev"  >> /etc/sysconfig/gpsd
  echo "USBAUTO=\"true\""  >> /etc/sysconfig/gpsd
  echo "GPSD configuration finished"
  echo "server 127.127.28.0 minpoll 4 maxpoll 4" >> /etc/ntp.conf
  echo "fudge  127.127.28.0 time1 0.183 refid NMEA" >> /etc/ntp.conf
  echo "server 127.127.28.1 minpoll 4 maxpoll 4 prefer" >> /etc/ntp.conf
  echo "fudge  127.127.28.1 refid PPS" >> /etc/ntp.conf
  #echo "restrict $1 mask 255.255.255.0 nomodify" >> /etc/ntp.conf
  echo "restrict -4 default kod notrap nomodify nopeer noquery limited" >> /etc/ntp.conf
  echo "restrict -6 default kod notrap nomodify nopeer noquery limited" >> /etc/ntp.conf
  # Local users may interrogate the ntp server more closely.
  echo "restrict 127.0.0.1" >> /etc/ntp.conf
  echo "restrict ::1" >> /etc/ntp.conf
  echo "NTPD configuration finished"
  if [[ ! -z $YUM_CMD ]]; then
    systemctl daemon-reload
    systemctl restart gpsd.service
    systemctl restart ntpd.service
  elif [[ ! -z $APT_GET_CMD ]]; then
    systemctl daemon-reload
    systemctl restart gpsd.service
    systemctl restart ntp.service
  elif [[ ! -z $ZYPPER_CMD ]]; then
    systemctl daemon-reload
    systemctl restart gpsd.service
    systemctl restart ntpd.service
  elif [[ ! -z $DNF_CMD ]]; then
    systemctl daemon-reload
    systemctl restart gpsd.service
    systemctl restart ntpd.service
  else
    echo "Unsupported linux distro. Please make sure that you have enabled and starded NTP and GPSD services "
  fi
  sudo cp ../../easyservice.sh /usr/bin/easyservice
  sudo cp ../../easyGUI.sh /usr/bin/easygui
  sudo cp 50-ttyusb.rules /etc/udev/rules.d/50-ttyusb.rules
  sudo cp 71-ti-permissions.rules /etc/udev/rules.d/71-ti-permissions.rules
  sudo udevadm control --reload-rules
  echo "rules update finished"
  ;;

  'Darwin')
  chown $USER ../../bin/SeedlinkServer/ringserver
  chmod u+x ../../bin/SeedlinkServer/ringserver
  cp RXTXcomm.jar  /Library/Java/Extensions
  librxtxSerial.jnilib /Library/Java/Extensions
  ;;
  *) echo "Unsupported OS"
  exit 2
  ;;
esac
