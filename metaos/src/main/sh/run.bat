@echo off

setLocal EnableDelayedExpansion
set CP=".
for /r ..\lib %%g in (*.jar) do (
  set CP=!CP!;%%g
)
set CP=!CP!"

set JRI_LD_PATH=../lib
set PPATH=%PATH%
set PATH=%PATH%;c:\progra~1\r\r-213~1.0\bin;c:\progra~1\r\r-213~1.0\bin\i386
echo %PATH%
java -cp %CP% -Dpython.path=. -Djava.library.path=../lib com.metaos.engine.Engine init.py %1 %2 %3 %4 %5 %6 %7 %8 %9
set PATH=%PPATH%
