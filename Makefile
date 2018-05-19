
build:
	javac -sourcepath src -d build src/**/*.java

run:
	java -cp .:build:**/*.class solutions.GameSolver 2> /dev/null

clean:
	rm -r ./build/

