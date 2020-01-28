#!/bin/bash
getDigitiser(){
  digitiser=""
  for i in $(seq 0 99) ; do
    if  [ -e "/dev/ttyACM$i" ]
    then
      if [ "115200" == "$(stty -a -F /dev/ttyACM$i | grep -oh 115200)" ]
      then
        digitiser="/dev/ttyACM$i"
        break
      fi
    fi
  done

  if [ -z $digitiser ]
  then
    digitiser="null"
  fi
  eval $1=$digitiser
}

getXbee(){
  xbee=""
  for j in $(seq 0 99) ; do
    if  [ -e "/dev/ttyUSB$j" ]
    then
      if [ "19200" == "$(stty -a -F /dev/ttyUSB$j | grep -oh 19200)" ]
      then
        xbee="/dev/ttyUSB$j"
        break
      fi
    fi
  done

  if [ -z $xbee ]
  then
    xbee="null"
  fi
  eval $1=$xbee
}

getGPS(){
  gps=""
  for k in $(seq 0 99) ; do
    if  [ -e "/dev/ttyUSB$k" ]
    then
      if [ "9600" == "$(stty -a -F /dev/ttyUSB$k | grep -oh 9600)" ]
      then
        gps="/dev/ttyUSB$k"
        break
      fi
    fi
  done

  if [ -z $gps ]
  then
    gps="null"
  fi
  eval $1=$gps
}

#getGPS gps
#getXbee xbee
#getDigitiser digitiser
#echo "GPS found at: $gps"
#echo "XBee found at: $xbee"
#echo "digitiser found at: $digitiser"
