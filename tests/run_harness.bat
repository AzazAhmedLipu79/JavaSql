@echo off
setlocal
pushd %~dp0\..

call build.bat
if errorlevel 1 exit /b 1

java -cp out tests.Harness
popd
