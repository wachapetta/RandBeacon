#!/bin/bash

if ! command -v netcat &> /dev/null; then
	echo "Installing netcat..."
	apt update
	apt install -y netcat
fi

netcat -w 1 -zv localhost 5672

if [ $? -eq 0 ]
then
	echo "Connection available"
	exit 0
else
	echo "Connection not available"
	exit 1
fi