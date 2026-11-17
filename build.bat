@echo off
setlocal
set "ROOT=%~dp0"

if not defined JAVA_HOME (
  echo [ERROR] JAVA_HOME is not set. Install JDK 8 and set JAVA_HOME first.
  pause
  exit /b 1
)

echo Using JAVA_HOME=%JAVA_HOME%
echo Using offline repository: %ROOT%offline-repo

pushd "%ROOT%efvs-thin"
call "%ROOT%apache-maven-3.6.3\bin\mvn.cmd" -o -B -s "%ROOT%settings.xml" -Dmaven.repo.local="%ROOT%offline-repo" -Dmaven.test.skip=true clean package
set "RC=%ERRORLEVEL%"
popd

if "%RC%"=="0" (
  echo.
  echo BUILD OK: efvs-thin\yudao-server\target\yudao-server.jar
) else (
  echo.
  echo BUILD FAILED, exit code %RC%
)
pause
exit /b %RC%
