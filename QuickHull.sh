path=OtoczkaProjekt/otoczka.jar
for arg in "$@"
do
   if [ "$arg" = 'noVis' ]; then
     path=OtoczkaProjekt-bez_wizualizacji/otoczka.jar
   fi
done
java -jar $path data=DATA_SETS/random100.txt alg=QH