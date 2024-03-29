# ISSTA 2023 paper How Effective are Neural Networks for Fixing Security Vulnerabilities?

This repository contains the artifact of paper "How Effective are Neural Networks for Fixing Security Vulnerabilities?" published in ISSTA 2023 by Yi Wu, Nan Jiang, Hung Viet Pham, Thibaud Lutellier, Jordan Davis, Lin Tan, Petr Babkin, and Sameena Shah. 


## Setup
Please modifying the following paths in [`scripts/util.py`](scripts/util.py) to your own path.

```python

VUL4J_DIR = "/path/to/Vul4J_projects/" # the absolute path to the folder that will contain all the Vul4J projects

VJBENCH_DIR = "/path/to/VJBench_projects/" # the absolute path to the folder that will contain all the VJBench projects

```

## Vul4J
To reproduce the results for Vul4J, please refer to the [Vul4J repository](https://github.com/tuhh-softsec/vul4j) for how to set up.
A script to help checkout Vul4J vulnerabilities is available at [`./scripts/VUL4J/vul4j_projects.py`](scripts/VUL4J/vul4j_projects.py). These 35 Vul4J vulnerabilities are used in our study: VUL4J-1, 3, 4, 5, 6, 7, 8, 10, 12, 18, 19, 20, 22, 23, 25, 26, 30, 39, 40, 41, 43, 44, 46, 47, 50, 53, 55, 57, 59, 61, 64, 65, 66, 73, 74.


## VJBench
VJBench consists of 42 reproducible vulnerabilities, which are listed in [`VJBench_dataset.csv`](./VJBench_dataset.csv). More details are provided in [`VJBench_data.json`](./scripts/VJBench_data.json) including the compile and test commands for reproducing each vulnerability. The first 15 vulnerabilities listed in [`VJBench_dataset.csv`](./VJBench_dataset.csv) have single hunk fixes, and thus are used in our study: Netty-1, Netty-2, Jenkins-1, Jenkins-2, Jenkins-3, Jinjava-1, Halo-1, Retrofit-1, Quartz-1, Flow-1, Flow-2, BC-Java-1, Json-sanitizer-1, Ratpack-1, Pulsar-1. 

### Dependency

- Apache Maven 3.8.6
- Java 8
- Gradle 3.1
- git

(We are building a docker image for VJBench. It will be coming out soon.)

### Usage
```bash
git clone https://github.com/lin-tan/llm-vul.git
cd llm-vul
```
#### Checkout a vulnerability:
For example, if we want to checkout Halo-1
```bash
python3 ./scripts/build_vjbench.py checkout Halo-1
```
#### Compile:
```bash
python3 ./scripts/build_vjbench.py compile Halo-1
```
#### Test:
```bash
python3 ./scripts/build_vjbench.py test Halo-1
```
## Transformed Vulnerabilities

The dataset VJBench-trans, consists of three transformed versions for each of the 50 single-hunk vulnerabilities (15 from VJBench and 35 from Vul4J). The transformed vulnerabilities are obtained by applying : (1) Identifier renaming only (2) Code structure change only (3) Both identifier renaming and code structure change at the same time. In total, there are 3x50 = 150 transformed vulnerabilities in VJBench-trans. The transformed and original vulnerabilities and are available in the folder [`VJBench-trans`](./VJBench-trans/).

For each vulnerability, we provide seven files with the following suffixes:

- original_method.java: the original buggy method.
- rename_only.java: the transformed buggy method with identifier renaming only.
- code_structure_change_only.java: the transformed buggy method with code structure change only.
- full_transformation.java: the transformed buggy method with both identifier renaming and code structure change.
- identifier_rename_dict.json: the identifier renaming mapping dictionary.
- patch_for_code_structure_change_only.java: the fixed transformed buggy method with code structure change only
- buggyline_location.json: the buggy line location for the buggy method [buggy_line_start, buggy_line_end]


## Model Patches
Patches generated by the models and the validation results are under folder [`./Model_patches/model_output/`](./Model_patches/model_output).

For validation results, "test_success" means the patch passes the test cases,  "compile_success" means that the patch fails to pass the test cases but is compilable, "uncompilable" means the patch is uncompilable, and "test_timeout" is the patch validtion exceeds the time limit.

## Large Language Models
### Dependency
- Python 3.10.4
- PyTorch 1.12.0
- Numpy 1.22.3
- Huggingface transformers 4.21.0

### Usage
#### Model download
##### LLMs as is
```bash
cd models

# download plbart-large
git clone https://huggingface.co/uclanlp/plbart-large

# download codet5-large
git clone https://huggingface.co/Salesforce/codet5-large

# download codegen-6B-multi
git clone https://huggingface.co/Salesforce/codegen-6B-multi 

# download incoder-6B
git clone https://huggingface.co/facebook/incoder-6B
```

##### Fine-tuned LLMs
We use LLMs fine-tuned by prior work ["Impact of Code Language Models on Automated Program Repair"](https://github.com/lin-tan/clm)

The fine-tuned LLMs can be downloaded from here https://doi.org/10.5281/zenodo.7559244, https://doi.org/10.5281/zenodo.7559277.

The fine-tuned LLMs we use in our work are:
- plbart-large-finetuned
- codet5-large-finetuned
- codegen-6B-finetuned
- incoder-6B-finetuned

#### Prepare input

The inputs to the models are under folder [`./Model_patches/model_input/`](./Model_patches/model_input).

We utilize a java parser [Jasper](https://github.com/lin-tan/clm/tree/main/jasper) to parse the buggy code to generate input. First, we need to install jasper. 
```bash
cd jasper

javac -cp ".:lib/*" -d target src/main/java/clm/jasper/*.java src/main/java/clm/codet5/*.java src/main/java/clm/codegen/*.java src/main/java/clm/plbart/*.java src/main/java/clm/incoder/*.java src/main/java/clm/finetuning/*.java
```

Next, for example, we will run fine-tuned InCoder. 
```bash
python3 ./scripts/fine-tuned_InCoder/fine_tuned_incoder_prepare_input.py
```

#### Patch generation
Assume the path to the model is `/path/to/models_dir/incoder-6B-finetuned`
```bash
python3 ./scripts/fine-tuned_InCoder/fine_tuned_incoder_generate_output.py /path/to/models_dir/
```

#### Patch validation
validate VJBench vulnerabilities
```bash
python3 ./scripts/fine-tuned_InCoder/fine_tuned_incoder_vjbench_validation.py
```
validate Vul4J vulnerabilities
```bash
python3 ./scripts/fine-tuned_InCoder/fine_tuned_incoder_vul4j_validation.py
```

## Automated Program Repair

Please refer to each APR tool's repo (link provided below) for usage guidance. Some scripts for using APR tools are available in the folder `.scripts/APR`.

- [RewardRepair](https://github.com/ASSERT-KTH/RewardRepair)
- [KNOD](https://github.com/lin-tan/knod)
- [Recoder](https://github.com/pkuzqh/Recoder)
- [CURE](https://github.com/lin-tan/CURE)


## Citation
```
@inproceedings{10.1145/3597926.3598135,
author = {Wu, Yi and Jiang, Nan and Pham, Hung Viet and Lutellier, Thibaud and Davis, Jordan and Tan, Lin and Babkin, Petr and Shah, Sameena},
title = {How Effective Are Neural Networks for Fixing Security Vulnerabilities},
year = {2023},
isbn = {9798400702211},
publisher = {Association for Computing Machinery},
address = {New York, NY, USA},
url = {https://doi.org/10.1145/3597926.3598135},
doi = {10.1145/3597926.3598135},
booktitle = {Proceedings of the 32nd ACM SIGSOFT International Symposium on Software Testing and Analysis},
pages = {1282–1294},
numpages = {13},
keywords = {Automated Program Repair, Vulnerability, AI and Software Engineering, Language Model},
location = {Seattle, WA, USA},
series = {ISSTA 2023}
}
```
