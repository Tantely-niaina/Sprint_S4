@echo off
setlocal

set "nom_projet=sprint8"

set "work=D:\bossy\cours_S5\mr_naina\sprint_naina\Sprint-S4"

set "tomcat_webapps=C:\xampp\tomcat\webapps"

set "libpath=D:\bossy\cours_S5\mr_naina\sprint_naina\Sprint-S4\lib\*"

set "bin=%work%\bin"

javac -cp "%libpath%" -d %bin% Sprint-S4/src/frameworks/*.java Sprint-S4/src/controllers/*.java Sprint-S4/src/annotations/*.java Sprint-S4/src/util/*.java

jar cf frontcontrol.jar -C "%bin%" .

copy frontcontrol.jar "%libpath%"

del frontcontrol.jar

rem ----------------------------------------------------------------------

set "repertoire_temp=%TEMP%\%nom_projet%"

set "temp_classes=%repertoire_temp%\WEB-INF\classes"

mkdir "%repertoire_temp%"
IF NOT EXIST "%repertoire_temp%" (
    mkdir "%repertoire_temp%"
) ELSE (
    del /s /q "%repertoire_temp%\*"
    echo Suppression des anciens fichiers temporaires
)

mkdir "%repertoire_temp%\WEB-INF"

mkdir "%repertoire_temp%\WEB-INF\classes"

mkdir "%repertoire_temp%\WEB-INF\lib"

copy "%work%\pages\*.jsp" "%repertoire_temp%"

copy "%work%\lib\*" "%repertoire_temp%\WEB-INF\lib"

copy "%work%\web.xml" "%repertoire_temp%\WEB-INF"

javac -cp "%libpath%" -d %temp_classes% Sprint-S4/src/controllers/*.java Sprint-S4/src/annotations/*.java Sprint-S4/src/util/*.java



@REM jar -cvf "%repertoire_temp%.war" *

@REM move /y "%repertoire_temp%.war" "%tomcat_webapps%"

@REM rmdir /S /Q "%repertoire_temp%"

rem Création du fichier WAR
jar -cvf "%nom_projet%.war" -C "%repertoire_temp%" .

rem Copie du fichier WAR dans le répertoire webapps de Tomcat
copy "%nom_projet%.war" "%tomcat_webapps%"

rem Nettoyage du fichier WAR temporaire
del "%nom_projet%.war"

echo Done!
pause