@echo off
setlocal
pushd %~dp0\..

call build.bat || (echo Build failed & popd & exit /b 1)

echo === Running harness ===
call tests\run_harness.bat || (echo Harness failed & popd & exit /b 1)

echo === Running smoke ===
call tests\run_tests.bat || (echo Smoke failed & popd & exit /b 1)

echo === Running e2e ===
set DBE2E=%CD%\storage\databases\e2e_demo
if exist "%DBE2E%" rmdir /s /q "%DBE2E%"
java -cp out SqlRunner tests\e2e.sql || (echo E2E failed & popd & exit /b 1)

echo ALL TESTS PASSED
popd
