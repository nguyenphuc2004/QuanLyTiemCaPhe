@echo off
REM Build file jar chay doc lap: target\tiemcaphe.jar (chay bang: java -jar target\tiemcaphe.jar, can Java 21+).
REM Build cho may khac: scripts\build-jar.bat win ^| linux ^| mac ^| mac-aarch64
cd /d "%~dp0\.."
if "%~1"=="" (
  call mvnw.cmd -q -DskipTests package
) else (
  call mvnw.cmd -q -DskipTests package -Djavafx.platform=%~1
)
if errorlevel 1 exit /b 1
echo Da build target\tiemcaphe.jar
