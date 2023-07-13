import os
import json
import subprocess
import sys
import re
from pathlib import Path
import  shutil
KNOD_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, KNOD_DIR+'../../') # utils file


import multiprocessing
from util import  cve_test_java_file, cve_compile_java_file, VJBENCH_DIR , info_json, vjbench_json, ROOT_PATH
from util import extract_correct_method_code, translate_code, cve_name_to_int



validation_folder = os.path.join(KNOD_DIR, "validation")
if not os.path.exists(validation_folder):
    os.mkdir(validation_folder)



                            
def validate_codegen_vul4j(vul_id:str):
    validation_result_file = os.path.join(validation_folder,"{}.json".format(vul_id))
    if isinstance(vul_id, str):
        raw_vul_id = "VUL4J-{}".format(cve_name_to_int[vul_id])
    else:
        print("Please input a valid VJBench Bug ID")
        return

    with open(info_json, "r") as f:
        all_info_list = json.load(f)
    for info in all_info_list:
        if info["vul_id"] == raw_vul_id:
            relative_buggy_file_path = info["buggy_file"]

            buggy_file_path = info["buggy_file"]
            buggy_method_start = info["buggy_method_with_comment"][0][0]
            buggy_method_end = info["buggy_method_with_comment"][0][1]
            buggy_line_start = info["buggy_line"][0][0]
            cve_id = info["CVE_id"]
            project_path = os.path.join(VJBENCH_DIR, vul_id)
            buggy_file_path = os.path.join(project_path,buggy_file_path)
            if (len(info["buggy_line"][0])==1):
                buggy_line_end = buggy_line_start
            else:
                buggy_line_end = info["buggy_line"][0][1]
            with open(vjbench_json, "r") as f:
                vjbench_info = json.load(f)
            compile_cmd = vjbench_info[vul_id]["compile_cmd"]
            test_cmd = vjbench_info[vul_id]["test_cmd"]
            restore_cmd = "git checkout HEAD {}".format(vjbench_info[vul_id]["buggy_file_path"])
            # print(restore_cmd)
            # subprocess.call(restore_cmd.split(), shell=True)
            p =  subprocess.Popen(restore_cmd.split(), cwd=project_path,stdout=subprocess.PIPE)
            p.wait()

    with open(buggy_file_path, "r") as f:
        lines = f.readlines()

    patch_json_path = os.path.join(ROOT_PATH,"Model_patches","model_output","KNOD","original","{}.json".format(vul_id))
   
    if not os.path.exists(patch_json_path):
        print("patch json not exist", patch_json_path)
        return
    with open(patch_json_path, "r") as f:
        patch_json = json.load(f)

    results = patch_json

    line_id, patches = read_output(vul_id)
   


    validation_result = []
    # buggy_file_path
    for patch_dict in patches:
        restore_cmd = "git checkout HEAD {}".format(relative_buggy_file_path)
    
        p =  subprocess.Popen(restore_cmd.split(), cwd=project_path,stdout=subprocess.PIPE)
        p.wait()
        patch= patch_dict["patch"]

        vdict = {"correctness":"", "patch":patch}
        
        fix_type = patch_dict["fix_type"]
        start_row, start_col, end_row, end_col =get_meta(line_id, fix_type)
        insert_fix_vul(relative_buggy_file_path, start_row, start_col, end_row, end_col, patch, project_path, fix_type)
        # print(start_row, start_col, end_row, end_col)
        # print(patch)
        # print(buggy_file_path)
        print("validation: start compiling")
        if not cve_compile_java_file(project_path, compile_cmd):
            print("validation: compile failed")
            vdict["correctness"] = "uncompilable"
        else: 
            vdict["correctness"] = "compile_success"
            print("validation: start testing")
            test_result_value =  cve_test_java_file(project_path, test_cmd)
            if  test_result_value == 1:
                print("validation: test success")
                vdict["correctness"] = "test_success"
            elif test_result_value == 2:# test_timeout
                print("validation: test timeout")
                vdict["correctness"] = "test_timeout"

        validation_result.append(vdict)
        results["validation_result"] = validation_result
        
        with open(validation_result_file, "w") as f:
            json.dump(results, f, indent=4)

    with open(buggy_file_path, "w") as f:
        f.writelines(lines)




def insert_fix_vul(file_path, start_row, start_col, end_row, end_col, patch, project_dir, fix_type='general',key=None):
    file_path = os.path.join(project_dir,file_path)
    # shutil.copyfile(file_path, file_path + '.bak')

    with open(file_path, 'r') as file:
        data = file.readlines()

    if fix_type == 'general':
        with open(file_path, 'w') as file:
            for i in range(start_row - 1):
                file.write(data[i])
            file.write(data[start_row - 1][: start_col - 1] + '\n')
            file.write(patch)
            file.write(data[end_row - 1][end_col:])
            for i in range(end_row, len(data)):
                file.write(data[i])
    else:
        with open(file_path, 'w') as file:
            for i in range(start_row - 1):
                file.write(data[i])
            file.write(patch)
            file.write(data[end_row - 1])
            for i in range(end_row, len(data)):
                file.write(data[i])

    # return file_path + '.bak'   


KNOD_APR_DIR = "/path/to/knod_apr"
def read_output(vul_id):
    patch_json_path = os.path.join(ROOT_PATH,"Model_patches","model_output","KNOD","original","{}.json".format(vul_id))
    with open(patch_json_path, "r") as f:
        patch_json = json.load(f)
    line_id = patch_json["id"]
    patches = patch_json["patches"]
    
    return line_id, patches

def get_meta(line_id, fix_type):
    
    if fix_type =='general':
        meta_path = os.path.join(KNOD_APR_DIR, "data/vul_input_original/rem_general_localize.txt")
    else:
        meta_path = os.path.join(KNOD_APR_DIR, "data/vul_input_original/rem_insert_localize.txt")
    # read into lines
    with open(meta_path, "r") as f:
        meta_lines = f.readlines()
        # print("meta_lines", len(meta_lines))
    # print("line_id", line_id)
    # get the line with the line_id
    meta_line = meta_lines[line_id-1]
    # split the line into a list
    meta_list = meta_line.split("\t")
    location = meta_list[0] # 1117,9-1133,9 => start_row,start_col-end_row,.end_col
    # print("location", location)
    # extract start_row, start_col, end_row, end_col,
    start_row, start_col, end_row, end_col = location.split(",")[0], location.split(",")[1].split("-")[0], location.split(",")[1].split("-")[1], location.split(",")[2]
    return int(start_row), int(start_col), int(end_row), int(end_col)

    


# main funtion:
if __name__ == '__main__':
 
    validate_codegen_vul4j("Halo-1")
    print("Validatoin finished")
