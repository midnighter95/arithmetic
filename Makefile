init:
	git submodule update --init

compile:
	mill -i arithmetic.compile

bump:
	git submodule foreach git stash
	git submodule update --remote
	git add dependencies

bsp:
	mill -i mill.bsp.BSP/install

clean:
	git clean -fd

reformat:
	mill -i __.reformat

checkformat:
	mill -i __.checkFormat

firrtl:
	mill -i -j 0 arithmetic.run

test:
	mill -i -j 0 arithmetic.tests

mfccompile: test firrtl
	mill -i -j 0 arithmetic.mfccompile
