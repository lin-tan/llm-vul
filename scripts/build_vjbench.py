import os
import subprocess
import json
from pathlib import Path
import sys
from util import VJBENCH_DIR
import argparse
SCRIPTS_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]
vjbench_json = os.path.join(SCRIPTS_DIR,"VJBench_data.json")

def checkout_vul(vjbench_id):
    # read the json file
    with open(vjbench_json) as f:
        vjbench_data = json.load(f)
    data = vjbench_data[vjbench_id]
    github_url = data["github_url"]
    github_url_cve =  data["github_url_cve"]
    github_url_cve_branch = data["github_url_cve_branch"]
    fix_commit = data["fix_commit"]
    fix_sha = fix_commit.split("/")[-1].strip()
    introducing_files = data["introducing_files"]
    # build_gradle = data["build_gradle"]

    working_directory = vjbench_id
    if github_url_cve.strip() != "":
        if github_url_cve_branch.strip() != "":
            cmd = "git clone -b {} {} {}".format(github_url_cve_branch,github_url_cve,vjbench_id).split()
        else:
            cmd = "git clone {} {}".format(github_url_cve,vjbench_id).split()
        subprocess.call(cmd,cwd=VJBENCH_DIR )
    else:
        cmd = "git clone {} {}".format(github_url,vjbench_id).split()
        subprocess.call(cmd,cwd=VJBENCH_DIR )
        cmd = "git reset --hard {}".format(fix_sha).split()
        working_directory = os.path.join(VJBENCH_DIR,vjbench_id)
        subprocess.call(cmd, cwd=working_directory)
        # # run  git show HEAD^1 and get the std output
        cmd = "git show HEAD^1".split()
        p = subprocess.Popen(cmd, cwd=working_directory,stdout=subprocess.PIPE)
        output, err = p.communicate()
        output = output.decode('utf-8', 'ignore').strip()
        # get the commit id
        commit_id = output.split()[1]
        print(commit_id)
        cmd = "git checkout {}".format(commit_id).split()
        # introducing files is a list
        # append the introducing files list to the cmd
        cmd.extend(introducing_files)
        subprocess.call(cmd, cwd=working_directory)
        # now we have the buggy version of the project
        # if build_gradle is not empty, append the build_gradle to the project build.gradle
        # if build_gradle.strip() != "":
        #     with open("build.gradle","a") as f:
        #         f.write(build_gradle)


def compile_vul(vjbench_id):
    with open(vjbench_json) as f:
        vjbench_data = json.load(f)
    data = vjbench_data[vjbench_id]
    compile_cmd = data["compile_cmd"]
    test_cmd = data["test_cmd"]
    working_directory = os.path.join(VJBENCH_DIR,vjbench_id)
    # compile the project
    cmd = compile_cmd.split()
    subprocess.call(cmd, cwd=working_directory)
   


def test_vul(vjbench_id):
    with open(vjbench_json) as f:
        vjbench_data = json.load(f)
    data = vjbench_data[vjbench_id]
    compile_cmd = data["compile_cmd"]
    test_cmd = data["test_cmd"]
    working_directory = os.path.join(VJBENCH_DIR,vjbench_id)
    # run the test
    cmd = test_cmd.split()
    subprocess.call(cmd, cwd=working_directory)




# main
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('method', type=str, help="checkout, compile, test")
    parser.add_argument('vul_id', type=str, help="VJBench id")
    # method should only be checkout, compile, test
    args = parser.parse_args()
    method = args.method
    vul_id = args.vul_id
    if method == "checkout":
        checkout_vul(vul_id)
    elif method == "compile":
        compile_vul(vul_id)
    elif method == "test":
        test_vul(vul_id)
    else:
        print("Please input: checkout, compile, test")
        sys.exit(1)
    with open(vjbench_json) as f:
        vjbench_data = json.load(f)
    # make sure vul_id is in the json file
    if vul_id not in vjbench_data:
        print("Please input a valid vul_id")
        sys.exit(1)
    # checkout_vul(vul_id)
    # compile_vul(vul_id)
    # test_vul(vul_id)


