
@echo off


set "lib=C:\Program Files\Apache Software Foundation\Tomcat 10.1\lib"
echo "vers chemin lib"

set "tempLib=D:\bossy\cours_S4\Mr_naina\framework\tempLib"
echo "vers dossier de test temporaire"

set "javaPath=D:\bossy\cours_S4\Mr_naina\framework\test"
echo "entrain de compiler"

@REM Création du dossier temporaire s'il n'existe pas
if not exist "%tempLib%" mkdir "%tempLib%"

@REM Compilation des fichiers Java dans tempLib
javac -cp "%lib%\*" -d "%tempLib%" "%javaPath%\*.java"

echo "Compilation terminée."

@REM Création du fichier JAR à partir des classes compilées dans tempLib
cd "%tempLib%"
jar cf monFichier.jar *

echo "Fichier JAR créé avec succès dans %tempLib%."

@REM Copie du fichier JAR dans testLib
set "testLib=D:\bossy\cours_S4\Mr_naina\framework\Framework\Test\lib"
echo "Copie du fichier JAR dans %testLib%..."

copy "monFichier.jar" "%testLib%"
echo "Fichier JAR copié avec succès dans %testLib%."

pause
 
