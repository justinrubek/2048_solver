
build:
	javac -sourcepath src -d build src/**/*.java


run:
	java -cp .:build:**/*.class solutions.GameSolver

clean:
	rm -rf ./build/

do: clean build run
