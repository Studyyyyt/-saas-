#!/bin/zsh
set -e
cd "$HOME/Downloads/newsystem-springboot"
exec /opt/homebrew/opt/openjdk@17/bin/java -jar target/springboot-0.0.1-SNAPSHOT.jar
