 
#Remove previous installations of Fiji if they exist
FIJI_HOME=$HOME/blank_fiji
FIJI_APP=$FIJI_HOME/Fiji.app

#DIJ_DOWNLOAD_URL="https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.15/dependencies_2115.zip"
DIJ_DOWNLOAD_URL="https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.16/DeepImageJ_-2.1.16.jar"

#DIJ_DEPS_URL="https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.15/DeepImageJ_-2.1.15.jar"
DIJ_DEPS_URL="https://github.com/deepimagej/deepimagej-plugin/releases/download/2.1.16/dependencies_2116.zip"

rm -rf $FIJI_HOME
wget https://downloads.imagej.net/fiji/archive/20221201-1017/fiji-linux64.zip
unzip fiji-linux64.zip -d $FIJI_HOME
mv "$FIJI_HOME"/Fiji.* $FIJI_APP


# fix FilamentDetector issue (not necessary anymore, non existent file in the last version)
#mv $HOME/Fiji.app/jars/FilamentDetector-1.0.0.jar $HOME/Fiji.app/jars/FilamentDetector-1.0.0.jar.disabled
##$HOME/Fiji.app/ImageJ-linux64 --update add-update-site DeepImageJ https://sites.imagej.net/DeepImageJ/
##$HOME/Fiji.app/ImageJ-linux64 --update update
#rm $HOME/Fiji.app/jars/jna-4*.jar

wget $DIJ_DEPS_URL
unzip dependencies_2116.zip
mv dependencies_2116/dependencies_2.1.16/* $FIJI_APP/jars/
rm -rf dependencies_2116

wget $DIJ_DOWNLOAD_URL
#Add the -f mode to deal with the case of non existent diji instalations
rm  -f $FIJI_APP/plugins/DeepImageJ*.jar
mv DeepImageJ_-2.1.16.jar $FIJI_APP/plugins/DeepImageJ_-2.1.16.jar



#python3 -c "import imagej;ij = imagej.init('$HOME/Fiji.app');print('pyimagej initialized.')"
#export DISPLAY=:1
#Xvfb $DISPLAY -screen 0 1024x768x16 &

# Execution of a specific model with headless mode (done in the CI workflows)
#wget  https://zenodo.org/record/4608442/files/SMLM_Density%20Map_Estimation_%28DEFCoN%29.bioimage.io.model.zip
#mkdir -p $HOME/Fiji.app/models/DEFCoN.bioimage.io.model
#unzip 'SMLM_Density Map_Estimation_(DEFCoN).bioimage.io.model.zip' -d $HOME/Fiji.app/models/DEFCoN.bioimage.io.model/
#$HOME/Fiji.app/ImageJ-linux64 --headless --console -macro macroDIJ.ijm
