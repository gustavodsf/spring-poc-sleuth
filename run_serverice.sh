while getopts s: flag
do
    case "${flag}" in
        s) service=${OPTARG};;
    esac
done

echo "..:: Starting Service $service ::.."

if [ $service -eq 1 ]
then
  cd ./slueth-test1
fi

if [ $service -eq 2 ]
then
  cd ./slueth-test2
fi

if [ $service -eq 3 ]
then
  cd ./slueth-test3
fi

if [ $service -eq 4 ]
then
  cd ./slueth-test4
fi

if [ $service -gt 5 ]
then
  echo "opção inválida!";
else
  mvn clean install
  java -jar ./target/slueth-test-0.0.1-SNAPSHOT.jar
fi