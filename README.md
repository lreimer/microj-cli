# microj CLI

A small CLI to quickly create Java service or application projects based on pretty generic templates.
This is kind of similar to existing project skaffolding approaches and CLIs such as Maven archetypes 
or the Angular CLI.

## Usage

```bash
$ ./gradlew run --args 'service --name test-service --template dev.ops.tools.microj:microj-jakartaee8-payara5:1.4@zip --overwrite'
$ ./gradlew run --args 'service --name test-service --repository https://github.com/lreimer/microj-jakartaee8-payara5.git --overwrite' 

# if you have build the Graal native image
cd build
$ ./microj service --name test-service --template dev.ops.tools.microj:microj-jakartaee8-payara5:1.4@zip --overwrite
$ ./microj service --name test-service --repository https://github.com/lreimer/microj-jakartaee8-payara5.git --overwrite
```

## Maintainer

M.-Leander Reimer (@lreimer), <mario-leander.reimer@qaware.de>

## License

This software is provided under the MIT open source license, read the `LICENSE`
file for details.
