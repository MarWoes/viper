VIPER (Variant InsPector and Expert Rating tool) can be utilised to view variant calls and decide whether or not those are true or false positives. All calls are visualised using [IGV](https://github.com/igvteam/igv), thus enabling fast rating of sv calls. VIPER is implemented using Java and AngularJS.

If you are using VIPER for the first time, you should have a look at at the [short tutorial](https://github.com/MarWoes/viper/wiki/Getting-started).

## Requirements
To run the VIPER server, Java version 1.8 is required. You can check your java version by running `java -version` in a console.
Make sure to use a modern web browser, as VIPER is heavily reliant on the browser's Javascript environment.

## Installation
It is advised to use the VIPER and IGV jar files (as well as example configuration files) that can be downloaded from the [GitHub release pages](https://github.com/MarWoes/viper/releases).
If you are using Linux, it is recommended to install `Xvfb` with
`sudo apt-get install xvfb`
You can then run VIPER with
`java -jar VIPER.jar config.json`
to start the VIPER server, and open `http://localhost:8090` in a browser tab.

To build VIPER from source, `bower` and `gradle` need to be installed.
You can then use `./build.sh` to create an executable version of the VIPER server.

## Configuration

VIPER can be configured using a .json file.
There are mandatory and optional parameters that change the way VIPER operates.

### Required parameters

These parameters act as input to the VIPER server and must be specified by the user:

| parameter | description |
|-------| ----------- |
| *analysisFile* | File containing variant calls (must be a `.csv` or `.vcf` file). `.csv` files must include a header with column names. Columns `sample`, `type`, `chr1`, `bp1`, `chr2` and `bp2` are mandatory.
| *workDir* | Directory that stores images by igv and decision progress. **WARNING: Do not delete this directory, or your progress will be lost** |
| *bamDir* | Directory containing your `.bam`/`.bai`  sample files. |

### Optional parameters

These parameters are optional and may be omitted using the default values:

| parameter | description | default value |
|-------| ----------- |-----|
| *enableGrouping* | Group together calls with same chromosome values and similar breakpoint values | `true` |
| *breakpointTolerance* | If grouping is enabled, this is the maximum distance where two breakpoints are considered similar. | `3` |
| *csvDelimiter* | Character that delimits csv columns. | `";"` |
| *collectionDelimiter* | A single cell in a csv table may have multiple values. These values are seperated by this character. |`","` |
| *viperPort* | Port that VIPER listens on. | `8090` |
| *igvPort* | Port that IGV uses to communicate with VIPER | `9090` |
| *keepVcfSimple* | Use only mandatory vcf columns and ignore additional INFO and genotype information | `true` |
| *excludeRefVcfCalls* | Ignore calls that are marked as reference calls. | `true`
| *igvJar* | Path to IGV jar file. | `"igv.jar"`|
| *numPrecomputedSnapshots* | Precompute this number of breakpoint images to minimize visualization waiting time. | `10` |
| *viewRange* | A region of `[bp - viewRange, bp + viewRange]` is visualized using IGV. | `25` |
| *xvfbDisplay* | When using `Xvfb`, use this number as display number. | `1234` |
| *xvfbWidth* | When using `Xvfb`, create a window with this width. | `1280` |
| *xvfbHeight* | When using `Xvfb`, create a window with this height. | `1680` |
| *igvMaxMemory* | Maximum heap size of the IGV process. | `1200` |
| *xslxExportWindowSize* | When creating `.xlsx` files, this improves memory usage. Only change if exceptions occur during `.xlsx` export. | `1000` |
| *fastaRef* | `.fasta` reference file or IGV reference key (e.g. `hg19`). |

### Examples

Some example `.bam` files and sample `.csv`call files can be found on [Sciebo](https://uni-muenster.sciebo.de/index.php/s/Qf6xIn2WDOyHhFN).

---
The VIPER icon was provided by courtesy of [Niké Jenny Bruinsma](https://thenounproject.com/search/?q=snake&i=158882).
