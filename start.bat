where java >nul

if %errorlevel% NEQ 0 (
  msg "%username%" "No java found. Make sure you have installed Java (1.8)."
  start "" https://java.com/de/download/
  exit
)

SET TEMPFILE=%TEMP%\viper-tmpfile

java -version 2>& 1 | FIND "java version" > %TEMPFILE%
SET /p VERSIONSTRING= < %TEMPFILE%
DEL %TEMPFILE%
SET MAJORVERSION=%VERSIONSTRING:~14,1%
SET MINORVERSION=%VERSIONSTRING:~16,1%

if %MINORVERSION% LSS 8 (
  msg "%username%" "Your java version (%MAJORVERSION%.%MINORVERSION%) is outdated. Make sure you have installed a more recent version Java (at least 1.8)."
  start "" https://java.com/de/download/
  exit
)

java -jar VIPER.jar > log.txt 2>&1
