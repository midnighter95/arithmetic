init:
	git submodule update --init

compile:
	mill -i -j 0 arithmetic[5.0.0].compile

run:
	mill -i -j 0 arithmetic[5.0.0].run

test:
	mill -i -j 0 arithmetictest[5.0.0].test

firrtl:
	mill -i -j 0 arithmetic[5.0.0].elaborate

mfccompile:
	mill -i -j 0 arithmetic[5.0.0].mfccompile

bsp:
	mill -i mill.bsp.BSP/install

clean:
	git clean -fd

