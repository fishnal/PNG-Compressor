# PNG-Compressor
Compresses PNG image files using the "pngquant" library

### Usage
`java -jar pngc.jar <options> [input directory] [output directory]`
#### Current Options
| Option Name | Description | Values |
| ----------- | ----------- | ------ |
| multi-core | enables multi-core option | `no value` |
| mc | shorthand for `multi-core` option | `no value` |
#### Example
```
java -jar pngc.jar --mc tests/1in tests/1out
```
This will compress all the PNG image files in the relative directory `tests/1in` and output them into the relative directory `tests/1out`, while also making use of the number of cores available to the program as determined by `Runtime#availableProcessors()`
Note that even though the multi-core option may be enabled, the program may still only be able to make use of one core. This can happen on single-core processors or when the runtim only has one core available for use.

```
java -jar pngc.jar tests/1in tests/1out
```
Similar to the previous example, but will instead compress one image at a time and not make use of multiple cores.

### How it works
Utilizing the [pngquant](https://pngquant.org/) library, the program unpacks the pngquant executable file (deleting it after termination) and compresses all PNG image files in the input directory and outputs the resulting compressions to the output directory. The file names are not changed, but the program executes pngquant with the override option, so if the output directory contains files with the same name as the current image that is being compressed, that existing file will be overriden.

Additionally, the program makes use of multiple cores available to the runtime and executes individual threads and processes for each compression based on the number of cores available as determined by `Runtime#availableProcessors()`

As stated, the program unpacks the pngquant library. If the program is abnormally terminated, then the pngquant library is not deleted. While this should not pose any issues, especially in terms of space as it is a mere ~700KB, the next time the program is executed (from the same directory as it was before), and terminates normally, the pngquant library will be deleted from the file system.

The exact command executed through the pngquant library is as follows:
```
pngquant.exe --force --quality 50-50 [input file] --output [output directory + "/" + input file name]
```

### OS Support
+ Windows
