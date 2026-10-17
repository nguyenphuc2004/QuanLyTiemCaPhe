@echo off
REM Dong goi ung dung thanh file cai dat Windows (.msi) bang jpackage.
REM
REM Yeu cau : - Chay TREN WINDOWS (jpackage khong dong goi cheo he dieu hanh).
REM           - JDK 21 tro len DAY DU, co thu muc jmods (Temurin: https://adoptium.net), bien JAVA_HOME tro toi JDK.
REM           - WiX Toolset 3.x (https://wixtoolset.org/docs/wix3/) va thu muc bin cua WiX nam trong PATH.
REM Cach dung: scripts\package-win.bat
REM Ket qua  : target\installer\QuanLyTiemCaPhe-<phien ban>.msi
setlocal enabledelayedexpansion
cd /d "%~dp0\.."

set APP_NAME=QuanLyTiemCaPhe
set MAIN_JAR=tiemcaphe.jar
set MAIN_CLASS=com.tiemcaphe.Launcher
if "%VENDOR%"=="" set VENDOR=TiemCaPhe
set APP_VERSION=
for /f "tokens=3 delims=<>" %%v in ('findstr /r "<version>[0-9]" pom.xml') do if not defined APP_VERSION set APP_VERSION=%%v
if "%APP_VERSION%"=="" set APP_VERSION=1.0.0

if "%JAVA_HOME%"=="" (
  echo [LOI] Chua dat bien JAVA_HOME. Hay tro JAVA_HOME den JDK 21 day du.
  exit /b 1
)
if not exist "%JAVA_HOME%\jmods" (
  echo [LOI] JDK tai "%JAVA_HOME%" khong co thu muc jmods nen jpackage se loi. Dung Temurin JDK 21.
  exit /b 1
)
where candle.exe >nul 2>nul || echo [CANH BAO] Khong thay WiX (candle.exe) trong PATH, jpackage --type msi se loi.

echo ==^> Build jar (bo qua test)
call mvnw.cmd -q -DskipTests package
if errorlevel 1 exit /b 1
if not exist "target\%MAIN_JAR%" (
  echo [LOI] Khong thay target\%MAIN_JAR%
  exit /b 1
)

if exist target\jpackage-input rmdir /s /q target\jpackage-input
if exist target\installer rmdir /s /q target\installer
mkdir target\jpackage-input
mkdir target\installer
copy /y "target\%MAIN_JAR%" target\jpackage-input\ >nul

set ICON_ARGS=
if exist packaging\icon.ico set ICON_ARGS=--icon packaging\icon.ico

echo ==^> jpackage (.msi)
"%JAVA_HOME%\bin\jpackage" ^
  --type msi ^
  --name %APP_NAME% ^
  --app-version %APP_VERSION% ^
  --vendor "%VENDOR%" ^
  --description "Phan mem ban hang va quan ly tiem ca phe" ^
  --input target\jpackage-input ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --dest target\installer ^
  --add-modules java.base,java.desktop,java.scripting,java.sql,java.logging,java.naming,java.prefs,java.xml,jdk.unsupported,jdk.jfr,jdk.localedata,jdk.charsets ^
  --java-options "-Dfile.encoding=UTF-8" ^
  --win-menu --win-menu-group "Tiem Ca Phe" --win-shortcut --win-dir-chooser --win-per-user-install --win-upgrade-uuid 7c1f4b6e-2a9d-4e8b-9f3a-5d6c8e1b2a40 ^
  %ICON_ARGS%
if errorlevel 1 exit /b 1

echo ==^> Xong. File cai dat nam trong target\installer
dir /b target\installer
endlocal
