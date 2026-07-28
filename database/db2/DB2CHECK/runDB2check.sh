ARG1="$1";
ARG2="$2";
# echo "ARG1: $ARG1 ARG2: $ARG2";
java -jar target/db2check-1.0.1-jar-with-dependencies.jar $ARG1 $ARG2;
