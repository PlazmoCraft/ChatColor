@echo off
setlocal
rem Builds the plugin. Requires Java 21 (uses C:\cfbuild\jdk if present).
rem Because this project lives under a non-ASCII path (which breaks Gradle's
rem worker daemon on Windows), the build happens in an ASCII copy at
rem C:\cfbuild\build-<random> and the final jar is copied back here.

set "BASE=C:\cfbuild"
set "SRC=%~dp0"
set "JAVA_HOME=%BASE%\jdk"
set "GRADLE_USER_HOME=%BASE%\gradle-home"
set "PROJ=%BASE%\build-%RANDOM%%RANDOM%"

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ChatColor] JDK 21 not found at "%JAVA_HOME%". Set JAVA_HOME to a JDK 21 and re-run.
    exit /b 1
)
if not exist "%BASE%\gradle\bin\gradle.bat" (
    echo [ChatColor] Gradle 8.11 not found at "%BASE%\gradle". Extract gradle-8.11 there.
    exit /b 1
)

mkdir "%PROJ%"
xcopy /e /i /q /y "%SRC%src" "%PROJ%\src" >nul
copy /y "%SRC%build.gradle.kts" "%PROJ%\" >nul
copy /y "%SRC%settings.gradle.kts" "%PROJ%\" >nul
copy /y "%SRC%gradle.properties" "%PROJ%\" >nul

set "PATH=%JAVA_HOME%\bin;%PATH%"
pushd "%PROJ%"
call "%BASE%\gradle\bin\gradle.bat" build --no-daemon --console=plain
set "EC=%ERRORLEVEL%"
popd

if "%EC%"=="0" (
    copy /y "%PROJ%\build\libs\chatcolor-1.0.0.jar" "%SRC%ChatColor-1.0.0.jar" >nul
    echo [ChatColor] Done: %SRC%ChatColor-1.0.0.jar
) else (
    echo [ChatColor] Build failed with code %EC%.
)
rmdir /s /q "%PROJ%" >nul 2>nul
exit /b %EC%