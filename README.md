# PNG-Compressor
Compresses PNG image files using the "pngquant" library

### Usage
`java -jar pngc.jar <options> [input directory] [output directory]`
#### Current Options
| Option Name | Description | Values |
| ----------- | ----------- | ------ |
| multi-core | enables multi-core option | `no value` |
| mc | shorthand for `multi-core` option | `no value` |

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
