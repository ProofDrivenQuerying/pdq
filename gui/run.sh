# to compile create a runnable jar with all dependencies included, using the FullMain.java as the main entry point.
# update the path here openjsf, and the location/name of the new jar file.

# run gui:
 ../../PDQ-full.jar --module-path /home/gabor/git/openjfx/javafx-sdk-11.0.2/lib/ --add-modules=javafx.controls,javafx.fxml,javafx.graphics,javafx.swing,javafx.web,javafx.base

#run runtime:
./PDQ-full.jar runtime -s /home/gabor/git/pdq/regression/test/runtime/simple/rest_001/schema.xml -p /home/gabor/git/pdq/regression/test/runtime/simple/rest_001/expected-plan.xml -a /home/gabor/git/pdq/regression/test/runtime/simple/rest_001/accesses
