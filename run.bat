@echo off
setlocal
set "ROOT=%~dp0"

if not defined JAVA_HOME (
  echo [ERROR] JAVA_HOME is not set. Install JDK 8 and set JAVA_HOME first.
  pause
  exit /b 1
)

"%JAVA_HOME%\bin\java.exe" -jar "%ROOT%dist\yudao-server.jar"
pause
