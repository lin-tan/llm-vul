import subprocess
import multiprocessing
import json
import os
import sys
from pathlib import Path
CUR_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 

sys.path.insert(1, CUR_DIR+'../') # utils file
from util import vjbench_bug_id_list,vul4j_bug_id_list,info_json,VUL4J_DIR 

bug_range_list = vjbench_bug_id_list+vul4j_bug_id_list
def copy_original_file():
    with open(info_json, "r") as f:
        data = json.load(f)
    for i in bug_range_list:
        project_path =os.path.join(VUL4J_DIR ,"VUL4J-{}".format(i))
        original_file_path = os.path.join(project_path ,"VUL4J","original_whole_file.txt")
        if not os.path.exists(original_file_path):
            if "buggy_file" in data[i-1]:
                code_file_path = os.path.join(project_path, data[i-1]["buggy_file"])
                # copy code file to original_whole_file.txt
                os.system("cp {} {}".format(code_file_path, original_file_path))




def checkout_all_projects():
    if not os.path.exists(VUL4J_DIR ):
        os.makedirs(VUL4J_DIR )
    for i in bug_range_list:
        cmd = "vul4j checkout --id VUL4J-{} -d {}/VUL4J-{}".format(i,VUL4J_DIR ,i)
        cmd =cmd.split()
        p3 = subprocess.Popen(cmd, stdout=subprocess.PIPE)
        out3, err = p3.communicate()
        print(out3)
        print(err)

def compile_all_projects():
    for i in bug_range_list:
        cmd = "vul4j compile -d {}/VUL4J-{}".format(VUL4J_DIR,i,i)
        cmd =cmd.split()
        p3 = subprocess.Popen(cmd, stdout=subprocess.PIPE)
        out3, err = p3.communicate()
        print(out3)
        print(err)

def compile_all_projects_parallel(i):
    cmd = "vul4j compile -d {}/VUL4J-{}".format(VUL4J_DIR,i,i)
    print(cmd)
    cmd =cmd.split()
    p3 = subprocess.Popen(cmd, stdout=subprocess.PIPE)
    out3, err = p3.communicate()
    print(out3)
    

# p = multiprocessing.Pool(multiprocessing.cpu_count()-8)
# p.map(compile_all_projects_parallel, bug_range_list)
# exit()
def count_the_compile_fail_project():
    fail_compile_list = []
    no_compile_result_list = []
    count = 0
    for i in bug_range_list:
        result_file_path = "{}/VUL4J-{}/VUL4J/compile_result.txt".format(VUL4J_DIR,i)
        if not os.path.exists(result_file_path):
            print("VUL4J-{} compile result not exist".format(i))
            no_compile_result_list.append(i)
            continue
        with open(result_file_path) as f:
            r = f.read()
            if "0" in r:
                print("VUL4J-{} compile failed".format(i))
                fail_compile_list.append(i)
                count += 1
    print("{} projects compile failed".format(count))
    print("compile fail", fail_compile_list)
    print(len(fail_compile_list))
    print("no compie result ", no_compile_result_list)
    print(len(no_compile_result_list))
# count_the_compile_fail_project()
# exit()
compile_fail_list = []
def test_all_projects_parallel(i):
    if i not in compile_fail_list:
        # for i in range(start,end+1):
        cmd = "vul4j test -d {}/VUL4J-{}".format(VUL4J_DIR,i)
        print(cmd)
        cmd =cmd.split()
        p3 = subprocess.Popen(cmd, stdout=subprocess.PIPE)
        out3, err = p3.communicate()
        print(out3)

test_available_list = []
for x in bug_range_list:
    if x not in compile_fail_list:
        test_available_list.append(x)
print(len(test_available_list))
# p = multiprocessing.Pool(multiprocessing.cpu_count()-8)
# p.map(test_all_projects_parallel, test_available_list)

def check_test_result():
    test_fail = []
    for i in test_available_list:
        if i not in compile_fail_list:
            test_result_json = "{}/VUL4J-{}/VUL4J/testing_results.json".format(VUL4J_DIR,i)
            if not os.path.exists(test_result_json):
                print("Error: VUL4J-{} test result does not exist".format(i))
            else:
                with open(test_result_json) as f:
                    r = json.load(f)
                    cve_id = r["cve_id"]
                    tests = r["tests"]
                    metric = tests["overall_metrics"]
                    if metric["number_running"] == 0:
                        print("Error: VUL4J-{} ZERO tests run.".format(i))
                        test_fail.append(i)
                    elif metric["number_failing"] == 0 and metric["number_error"] == 0:
                        print("VUL4J-{} No test fail".format(i))
                        # print("VUL4J-{} {} tests failed, {} tests error.".format(i,metric["number_failing"],metric["number_error"]))
                        test_fail.append(i)
    print("{} projects test failed".format(len(test_fail)))
    print("test fail", test_fail)

# main function
if __name__ == "__main__":
    checkout_all_projects()
    copy_original_file()