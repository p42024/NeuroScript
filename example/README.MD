# The example

## Testing the compiler

This is created to ensure the environment is setup correctly.\
In this folder run the following command:

````bash
java -jar NeuroScript.jar example
````

It should generate a Python file with the name `example.py` in the same folder.\
The file should contain the same code as ```expected_outcome.py```.

## Testing Python

Run the following command to check if python is setup correctly run the following command:

```bash
python expected_outcome.py
```

## Testing Antlr

To generate the Antlr parser use the following command in the root folder:

```bash
java -Xmx500M org.antlr.v4.Tool Grammar.g4 -o src/main/java/org/g5/parser -package org.g5.parser -visitor
```

## Testing Maven

Generate the compiler using the following command in the root folder (requires Antlr files are generated):

```bash
mvn clean package
```
