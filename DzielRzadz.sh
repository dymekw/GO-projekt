path=OtoczkaProjekt/otoczka.jar
for arg in "$@"
do
   if [ "$arg" = 'noVis' ]; then
     path=OtoczkaProjekt-bez_wizualizacji/otoczka.jar
   fi
done
java -jar $path data=DATA_SETS/DAC.txt alg=DAC maxSubCHsize=20