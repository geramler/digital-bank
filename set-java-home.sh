#!/usr/bin/env bash
# set-java-home.sh — set JAVA_HOME for this workspace
# Detected JDK installation (may be JRE-only); adjust if different.
JAVA_HOME="/usr/lib/jvm/java-25-openjdk-amd64"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

if [ ! -x "$JAVA_HOME/bin/javac" ]; then
  echo "Warning: 'javac' not found in $JAVA_HOME/bin — JDK compiler may be missing."
  echo "Install the JDK (example): sudo apt install openjdk-25-jdk-headless"
fi

echo "JAVA_HOME set to $JAVA_HOME"
