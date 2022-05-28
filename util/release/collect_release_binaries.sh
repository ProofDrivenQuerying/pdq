pdq_version="2.0.0"

# collect library jar files
cp ../../common/target/pdq-common-${pdq_version}.jar .
cp ../../cost/target/pdq-cost-${pdq_version}.jar .
cp ../../datasources/target/pdq-datasources-${pdq_version}.jar .
cp ../../planner/target/pdq-planner-${pdq_version}.jar .
cp ../../reasoning/target/pdq-reasoning-${pdq_version}.jar .
cp ../../runtime/target/pdq-runtime-${pdq_version}.jar .

# collect executable jar files
cp ../../planner/target/pdq-planner-${pdq_version}-jar-with-dependencies.jar ./pdq-planner-${pdq_version}-executable.jar
cp ../../reasoning/target/pdq-reasoning-${pdq_version}-jar-with-dependencies.jar ./pdq-reasoning-${pdq_version}-executable.jar
cp ../../runtime/target/pdq-runtime-${pdq_version}-jar-with-dependencies.jar ./pdq-runtime-${pdq_version}-executable.jar
cp ../../pdq-main/target/pdq-main-${pdq_version}-jar-with-dependencies.jar ./pdq-main-${pdq_version}-executable.jar
