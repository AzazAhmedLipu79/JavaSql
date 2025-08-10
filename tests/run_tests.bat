@echo off
setlocal
pushd %~dp0\..

call build.bat
if errorlevel 1 (
  echo Build failed
  popd & exit /b 1
)

REM Remove existing test db folder if present
set DB_DIR=%CD%\storage\databases\testdb
if exist "%DB_DIR%" rmdir /s /q "%DB_DIR%"

REM Execute the entire SQL file in a single JVM so session persists
java -cp out SqlRunner tests\smoke.sql
if errorlevel 1 (
  echo SqlRunner failed
  popd & exit /b 1
)

REM Final status
if exist "%DB_DIR%" (
  echo PASS: testdb created
) else (
  echo FAIL: testdb not created
  popd & exit /b 1
)

echo Tests finished.
popd
