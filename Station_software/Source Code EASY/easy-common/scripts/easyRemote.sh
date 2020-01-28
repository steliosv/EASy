#!/bin/bash
if [ $# -ne 1 ]
	then
	echo "proper usage: $0 {IP_ADDR}"
else
	java -cp easy-common.jar org.sv.easy.remote.EASYRemote $1
fi
