import os
import json
import subprocess
import sys
import re
from pathlib import Path
import  shutil
KNOD_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, KNOD_DIR+'../../') # utils file

from util import vul4j_compile_java_file, vul4j_test_java_file,vul4j_bug_id_list, ROOT_PATH, info_json, VUL4J_DIR
from util import extract_correct_method_code, translate_code
import multiprocessing


validation_folder = os.path.join(KNOD_DIR, "validation")
if not os.path.exists(validation_folder):
    os.mkdir(validation_folder)


                            
def validate_knod_vul4j( vul_id_int):
    validation_result_file = os.path.join(validation_folder,"VUL4J-{}.json".format(vul_id_int))
    if os.path.exists(validation_result_file):
        return
    
    raw_vul_id = "VUL4J-{}".format(vul_id_int)

    project_path = os.path.join(VUL4J_DIR,raw_vul_id)

    with open(info_json, "r") as f:
        all_info_list = json.load(f)
    for info in all_info_list:
        if info["vul_id"] == raw_vul_id:
            compile_cmd = "vul4j compile -d {}".format(project_path)
            test_cmd = "vul4j test -d {}".format(project_path)
            if "buggy_file" not in info:
                print("{} buggy file not exist".format(vul_id_int))
                return
            relative_buggy_file_path = info["buggy_file"]
            buggy_file_path = info["buggy_file"]
            buggy_file_path = os.path.join(project_path,buggy_file_path)
            buggy_method_start = info["buggy_method_with_comment"][0][0]
            buggy_method_end = info["buggy_method_with_comment"][0][1]
            buggy_line_start = info["buggy_line"][0][0]
            if (len(info["buggy_line"][0])==1):
                buggy_line_end = buggy_line_start
            else:
                buggy_line_end = info["buggy_line"][0][1]
        
            original_file_path = os.path.join(project_path,"VUL4J","original_whole_file.txt")
            # if not os.path.exists(original_file_path):
            #     print("create original file")
            #     # copy the buggy file to original file
            #     subprocess.call(["cp", buggy_file_path, original_file_path])
            
            print("successfully get the info")
           
            break
            
    
    
    with open(original_file_path, "r") as f:
        lines = f.readlines()

    patch_json_path = os.path.join(ROOT_PATH,"Model_patches","model_output","KNOD","original","VUL4J-{}.json".format(vul_id_int))
   
    if not os.path.exists(patch_json_path):
        print("patch json not exist", patch_json_path)
        return
    with open(patch_json_path, "r") as f:
        patch_json = json.load(f)

    results = patch_json

    line_id, patches = read_output(vul_id_int)
   

    for idx in [1]:
        validation_result = []
        # buggy_file_path
        for patch_dict in patches:
            subprocess.call(["cp", original_file_path,  buggy_file_path])
            patch= patch_dict["patch"]

            vdict = {"correctness":"", "patch":patch}
           
            fix_type = patch_dict["fix_type"]
            start_row, start_col, end_row, end_col =get_meta(line_id, fix_type)
            insert_fix_vul(relative_buggy_file_path, start_row, start_col, end_row, end_col, patch, project_path, fix_type)
            print(start_row, start_col, end_row, end_col)
            print(patch)
            print(buggy_file_path)
            # exit()
            print("validation: start compiling")
            if not vul4j_compile_java_file(project_path, compile_cmd):
                print("validation: compile failed")
                vdict["correctness"] = "uncompilable"
            else: 
                vdict["correctness"] = "compile_success"
                print("validation: start testing")
                test_result_value =  vul4j_test_java_file(project_path, test_cmd)
                if test_result_value == 1:
                    print("validation: test success")
                    vdict["correctness"] = "test_success"
                elif test_result_value == 2:
                    print("test_timeout")
                    vdict["correctness"] = "test_timeout"
            validation_result.append(vdict)
            results["validation_result"] = validation_result
            
            with open(validation_result_file, "w") as f:
                json.dump(results, f, indent=4)
        # exit()
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
def read_output(vul_id_int):
    patch_json_path = os.path.join(ROOT_PATH,"Model_patches","model_output","KNOD","original","VUL4J-{}.json".format(vul_id_int))
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
        print("meta_lines", len(meta_lines))
    print("line_id", line_id)
    # get the line with the line_id
    meta_line = meta_lines[line_id-1]
    # split the line into a list
    meta_list = meta_line.split("\t")
    location = meta_list[0] # 1117,9-1133,9 => start_row,start_col-end_row,.end_col
    print("location", location)
    # extract start_row, start_col, end_row, end_col,
    start_row, start_col, end_row, end_col = location.split(",")[0], location.split(",")[1].split("-")[0], location.split(",")[1].split("-")[1], location.split(",")[2]
    return int(start_row), int(start_col), int(end_row), int(end_col)

    

# main funtion:
if __name__ == '__main__':

    for model_name in [ "knod"]:
        print("model_name: ", model_name)
        validate_knod_vul4j(5) # VUL4J-5
        # exit()

        # p = multiprocessing.Pool(8)
        # p.map(validate_knod_vul4j, vul4j_bug_id_list )
        # print("Validatoin finished")
