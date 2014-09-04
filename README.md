als-comparison
==============

Comparing the performance of the ALS algorithm in distributed frameworks.

# Flink 0.6 hang up issue

In this repo right now you'll find essentially the same code twice: an [ALS](https://mahout.apache.org/users/recommender/intro-als-hadoop.html) implementation in the 0.5 release of Stratosphere and in the 0.6 release of Flink.

The issue we are facing is that the Stratophere implementation works nicely, however the Flink code hangs up for slightly larger inputs. We ran the experiments on a cluster so this repo does not use the {flink,stratosphere}-clients jar to provide an IDE runnable option, but has a local cluster setup.

# Try it out yourself

To build the repo  use `mvn clean install`.

To run the Stratosphere implementation on the two inputs:

```batch
./als_comparison_experiment.sh AlsStrato 1 sampledb2a.csv.txt 2 3 -
./als_comparison_experiment.sh AlsStrato 1 sampledb2b.csv.txt 2 3 -
```

Run Flink on the smaller input:

```batch
./als_comparison_experiment.sh AlsFlink 1 sampledb2a.csv.txt 2 3 -
```

These should work fine. However the following hangs up:

```batch
./als_comparison_experiment.sh AlsFlink 1 sampledb2b.csv.txt 2 3 -
```

You might need to execute `scripts/stop-all-local-flink-0.6-hadoop2.sh` after this failed execution.

# Notes

 * When removing the printing of one of the matrices the Flink implementation also works
 * the usage of RichFunctions does not seem to matter
