@echo off
set "JAVA_HOME=C:\Program Files\Java\jdk-21.0.10"
set "PATH=C:\Program Files\Java\jdk-21.0.10\bin;%PATH%"
call C:\Maven\apache-maven-3.8.6\bin\mvn.cmd package -DskipTests %*
