#!/bin/bash
## @author Stylianos Voutsinas
## @file service.sh
#export DISPLAY=:0.0
xhost +local:
export DISPLAY=:0


if [[ $EUID -ne 0 ]]; then
  echo "This script must be run as root"
  exit 1
fi
. utils/Linux-MacOSX/discover.sh
DAEMON="EASYd"
DAEMON_NAME="easy-common.jar"
DAEMON_PATH=$(pwd -P)
MAINCL="org.sv.easy.cli.EasyMain"
NSTA="1.5"
NLTA="60"
TRIG="4"
DETRIG="2"
PIDFILE=$DAEMON_PATH/EASYd.pid
stalta="0" #Supported STA/LTA options: 0-3((0)Classic (1)Delayed (2)Recursive (3)ZDetect)
mode="rt"
getXbee xbee #Supported modes: null for NA, path to /dev/tty* to activate
getDigitiser serial #Supported modes: null for NA, path to /dev/tty* to activate
geoacc="true" #Supported modes: true -> accelerometer, false -> geophone
ip="8.8.8.8" #place your server's IP here
locIP="127.0.0.1"
exitcmd="EXIT"
red=$'\e[1;31m'
grn=$'\e[1;32m'
end=$'\e[0m'

introfunc(){
  printf "${grn}%-s${end}\n" "Welcome to e.a.sy. Earthquake Alert System"
  logo=`cat $DAEMON_PATH/logo`
  printf "${grn}%-s${end}\n" "$logo"
}
##@fn start()
##@brief Starts the Daemon
start(){
  #printf "%-50s" "Starting $DAEMON ..."
  printf "%-50s" "Found digitiser at:"
  printf "${grn}%-s${end}\n" "$digitiser"
  printf "%-50s" "Found XBee module at:"
  printf "${grn}%-s${end}\n" "$xbee"
  if [ ! -f $PIDFILE ]; then
    if [ "$mode" == "rec" ]; then
      echo "Recordings are not supported in the EASY service"
    else
      java -cp $DAEMON_PATH/$DAEMON_NAME $MAINCL $mode $stalta $serial $xbee $geoacc $ip $TRIG $DETRIG $NSTA $NLTA > service.out 2>service.err &
    fi
    echo $! > $PIDFILE
    introfunc
    printf "%-50s" "$DAEMON starting ..."
    PID=`cat $PIDFILE`
    if [ -z $PID ]; then
      printf "${red}%-s${end}\n" "[FAILED]"
    else
      printf "${grn}%-s${end}\n" "[OK]"
    fi
  else
    printf "${grn}%-s${end}\n" "$DAEMON is already running ..."
  fi
}

##@fn stop()
##@brief Stops the Daemon
stop(){
  if [ -f $PIDFILE ]; then
      java -cp easy-common.jar org.sv.easy.remote.EASYRemote $locIP $exitcmd
  else
    printf "%s\n" "pidfile not found"
  fi
}

##@fn status()
##@brief Returns the state of the Daemon
status(){
  printf "%-50s" "Checking $DAEMON..."
  if [ -f $PIDFILE ]; then
    PID=`cat $PIDFILE`
    if [ -z "`ps -axf | grep ${PID} | grep -v grep`" ]
    then
      printf "${red}%-s${end}\n" "[$DAEMON dead but pidfile exists, removing stale pidfile]"
      rm $PIDFILE
    else
      printf "${grn}%-s${end}\n" "[Running]"
    fi
  else
    printf "${red}%-s${end}\n" "[Service not running]"
  fi
}
##@fn execcmd()
##@brief Executes a command
execcmd(){
  if [ -f $PIDFILE ]; then
    echo "type one command from the list:"
    echo "EOWARN removes the issued warning from the end devices"
    echo "TESTMAIL sends a test email"
    echo "TESTXBEE sends a test xbee packet"
    echo "TESTIOT tests the IOT service"
    echo "STOPR terminates ringserver"
    echo "STARTR starts ringserver"
    echo "GPIOEN enables GPIOs"
    echo "GPIODIS disables GPIOs"
    echo "DELF Deletes files and logs older than 30days"
    echo "EXIT terminates properly the application\n"
    read cmd
    java -cp easy-common.jar org.sv.easy.remote.EASYRemote $locIP $cmd
  else
    printf "${red}%-s${end}\n" "[Service not running]"
  fi
}

##@brief Main logic here
case $1 in
  start)
  start
  ;;
  stop)
  stop
  ;;
  status)
  status
  ;;
  execcmd)
  execcmd
  ;;
  *)
  echo $"Usage: $0 {start|stop|status|execcmd}"
  exit 1
  ;;
esac
exit 0
