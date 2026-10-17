@echo off
REM Chay ung dung o che do phat trien. Them --demo de tao don hang mau:  scripts\run-dev.bat --demo
cd /d "%~dp0\.."
if "%~1"=="" (
  call mvnw.cmd -q javafx:run
) else (
  call mvnw.cmd -q javafx:run -Djavafx.args="%*"
)
