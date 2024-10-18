@echo off
set source="C:\Users\Stormi\IdeaProjects\microbot\runelite-client\src\main\java\net\runelite\client\plugins\microbot\storm"
set destination="C:\Users\Stormi\IdeaProjects\plugins"

REM Create symbolic links for files
for %%F in (%source%\*) do (
    mklink /H %destination%\%%~nxF %%F
)

REM Create symbolic links for directories
for /D %%D in (%source%\*) do (
    mklink /J %destination%\%%~nxD %%D
)

echo Symbolic links created successfully!
pause
