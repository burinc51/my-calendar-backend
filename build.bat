@echo off
REM Set JAVA_HOME to JDK 21
set "JAVA_HOME=C:\Program Files\Java\jdk-21"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo Using JAVA_HOME=%JAVA_HOME%
echo Starting Maven build...

REM Run Maven Wrapper
mvnw -U clean package -DskipTests -Pproduction
