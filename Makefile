
build:
	javac -sourcepath src -d build src/**/*.java


run:
	java -cp .:build:**/*.class solutions.GameSolver

clean:
	rm -r ./build/

do: clean build run
