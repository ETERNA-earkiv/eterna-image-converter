Image Converter Plugin
-----------------------
This is a plugin for [ETERNA](https://github.com/ETERNA-earkiv/ETERNA).

It includes:
* Source code example
* Unit testing bootstrap
* Install dependencies and plugin properties examples
* Build script to compile and create docker image with ETERNA and your plugin
* Quick run instructions
* README automatic generation instructions

## How to build and run

To build execute `./build.sh`, this will run with the latest ETERNA version.
If you require a different ETERNA version, e.g. vX.X.X, update the pom.xml parent version and execute `./build.sh vX.X.X`

The build script will compile the plugin and create a docker image with the base ETERNA plus the plugin installed.
To run execute (image-converter should be replaced by the project folder name):

```shell
docker run -p 8080:8080 image-converter:latest
```

Then open in your favorite browser [http://localhost:8080](http://localhost:8080).
