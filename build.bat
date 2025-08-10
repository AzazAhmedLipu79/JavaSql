@echo off
setlocal enabledelayedexpansion
pushd %~dp0

REM Clean output to avoid stale classes
if exist out rmdir /s /q out
mkdir out

REM Build a list of all Java source files
dir /s /b src\*.java > sources.txt

REM Compile all sources in one javac invocation so dependencies resolve
javac -d out -sourcepath src @sources.txt
if errorlevel 1 (
  echo Compilation failed!
  popd & exit /b 1
)
echo Build OK
popd & exit /b 0
