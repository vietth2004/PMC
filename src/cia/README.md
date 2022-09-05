# Note

Remember to run `mvn validate` before building the first time on a new environment. This command will install Eclipse
CDT as a Maven package to local repository.

# CIA Command-line interface

Usage example: ```java -jar "path/to/cia.jar" -p <path/to/old.pro> -c <path/to/new.pro> -o <path/to/output.html>```

| Command      | Description |
| ----------- | ----------- |
| -p, --previous | Path to the project file of the previous version. |
| -c, --current  | Path to the project file of the current version. |
| -o, --output   | Path to the output file. Default output format is CSV file, will automatically switch to output HTML files when the output file extension is html. |
| -g, --gpp      | OPTIONAL. Path to the ```g++``` executable binary. If missing, use system one. |
| -q, --qmake    | OPTIONAL. Path to the ```qmake``` executable binary. If missing, use system one. |
| -h, --help     | Show help message. |

# Building jar file

- Run `mvn clean compile assembly:single`