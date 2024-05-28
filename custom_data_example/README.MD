# Using custom data

## Allow format of custom data

The only supported file format is ```csv```. An example of can be seen in ```custom_data.csv```.
The file should contain the features and a target for each entry.
Features are the input data and there can be any amount of them as long as they are consistent,
each value can have an arbitrary name.
The target is the expected output of the model, given the input.
The format is as follows:

```csv
feature1,feature2,...,featureN,target
value1,value2,...,valueN,target_value
...
value1,value2,...,valueN,target_value
```

An example of a file can be seen below:

```csv
feature1,feature2,feature3,target
0.1,0.2,0.3,0
0.4,0.5,0.6,1
0.7,0.8,0.9,0
1.0,1.1,1.2,1
```

## Using the example code

To compile run the following command in this folder:

````bash
java -jar ../example/NeuroScript.jar example
````

To run the generated Python file run the following command:

```bash
python example.py
```
