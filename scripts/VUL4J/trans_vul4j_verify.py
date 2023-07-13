import os
import json
import subprocess
import sys
import re
from pathlib import Path
import multiprocessing

INCODER_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, INCODER_FINETUNE_DIR+'../') # utils file

from util import vul4j_compile_java_file, vul4j_test_java_file,vul4j_bug_id_list, ROOT_PATH, info_json, VUL4J_DIR
from util import extract_correct_method_code, translate_code

def copy_files(vul_id_int, trans):
    # print("copying files")
    dir_path =os.path.join(VUL4J_DIR,"VUL4J-{}".format(vul_id_int),"VUL4J")
    if not os.path.exists(dir_path):
        print("{} not exist".format( dir_path))
        return
    compile_result_path = os.path.join(dir_path, "compile_result.txt")
    compile_log_path = os.path.join(dir_path, "compile.log")
    testing_results_path = os.path.join(dir_path, "testing_results.json")
    testing_log_path = os.path.join(dir_path, "testing.log")

    new_dir = os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int))
    if not os.path.exists(new_dir):
        os.makedirs(new_dir)
    new_compile_result_path = os.path.join(new_dir, "{}_compile_result.txt".format(trans))
    new_testing_results_path = os.path.join(new_dir, "{}_testing_results.json".format(trans))
    new_testing_log_path = os.path.join(new_dir, "{}_testing.log".format(trans))
    new_compile_log_path = os.path.join(new_dir, "{}_compile.log".format(trans))
    if os.path.exists(compile_result_path):
        os.system("cp {} {}".format(compile_result_path, new_compile_result_path))
    if os.path.exists(testing_results_path):
        os.system("cp {} {}".format(testing_results_path, new_testing_results_path))
    if os.path.exists(testing_log_path):
        os.system("cp {} {}".format(testing_log_path, new_testing_log_path))
    if os.path.exists(compile_log_path):
        os.system("cp {} {}".format(compile_log_path, new_compile_log_path))

    




def delete_validation_result_file(vul_id_int):
    dir_path =os.path.join(VUL4J_DIR,"VUL4J-{}".format(vul_id_int),"VUL4J")
    if not os.path.exists(dir_path):
        print("{} not exist".format(dir_path))
        return
    # delete the compile_result.txt, compile.log, testing_results.json, testing.log
    compile_result_path = os.path.join(dir_path, "compile_result.txt")
    compile_log_path = os.path.join(dir_path, "compile.log")
    testing_results_path = os.path.join(dir_path, "testing_results.json")
    testing_log_path = os.path.join(dir_path, "testing.log")
    if os.path.exists(compile_result_path):
        os.remove(compile_result_path)
    if os.path.exists(compile_log_path):
        os.remove(compile_log_path)
    if os.path.exists(testing_results_path):
        os.remove(testing_results_path)
    if os.path.exists(testing_log_path):
        os.remove(testing_log_path)



def get_transformed_method(vul_id_int, trans):
    dir_path = os.path.join(ROOT_PATH,"VJBench-trans","VUL4J-{}".format(vul_id_int))
    if trans == "rename+code_structure":
        f_path = os.path.join(dir_path, "VUL4J-{}_full_transformation.java").format(vul_id_int)
    elif trans == "structure_change_only":
        f_path = os.path.join(dir_path, "VUL4J-{}_code_structure_change_only.java").format(vul_id_int)
    else:
        f_path = os.path.join(dir_path, "VUL4J-{}_{}.java").format(vul_id_int, trans)
    with open(f_path, "r") as f:
        return f.read()
                            
def validate_trans_vul4j( vul_id_int):
    raw_vul_id = "VUL4J-{}".format(vul_id_int)

    vul4j_project_path = os.path.join(VUL4J_DIR,raw_vul_id)
    with open(info_json, "r") as f:
        all_info_list = json.load(f)
    for info in all_info_list:
        if info["vul_id"] == raw_vul_id:
            compile_cmd = "vul4j compile -d {}".format(vul4j_project_path)
            test_cmd = "vul4j test -d {}".format(vul4j_project_path)
            if "buggy_file" not in info:
                print("{} buggy file not exist".format(vul_id_int))
                return
            buggy_file_path = info["buggy_file"]
            buggy_file_path = os.path.join(vul4j_project_path,buggy_file_path)
            buggy_method_start = info["buggy_method_with_comment"][0][0]
            buggy_method_end = info["buggy_method_with_comment"][0][1]
            buggy_line_start = info["buggy_line"][0][0]
            if (len(info["buggy_line"][0])==1):
                buggy_line_end = buggy_line_start
            else:
                buggy_line_end = info["buggy_line"][0][1]
            original_file_path = os.path.join(vul4j_project_path,"VUL4J","original_whole_file.txt")
            print("successfully get the info")
            break
    

    with open(original_file_path, "r") as f:
        lines = f.readlines()
  

    
    for trans in ["original","structure_change_only", "rename+code_structure", "rename_only"]: 
        if os.path.exists(os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int), "{}_compile_result.txt".format(trans))):
            # and os.path.exists(os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int), "{}_testing_results.json".format(trans))) :
            continue
        print("validation: start validating {}".format(trans))
        if trans == "original":
            # simply write lines to the whole file
            with open(buggy_file_path, "w") as f:
                f.writelines(lines)
        else:
            transfmormed_method = get_transformed_method(vul_id_int, trans)
            if trans != "original" and trans != "structure_change_only":
                transfmormed_method = translate_code(transfmormed_method,vul_id_int)
            with open(buggy_file_path, "w") as f:
                f.writelines(lines[:buggy_method_start-1])
                f.write(transfmormed_method)
                f.writelines(lines[buggy_method_end:])
        # exit()
        print("validation: start compiling")
        if not vul4j_compile_java_file(vul4j_project_path, compile_cmd):
            print("validation: compile failed")
            return
        else: 
            print("validation: start testing")
            test_result_value =  vul4j_test_java_file(vul4j_project_path, test_cmd)
            if test_result_value == 1:
                print("validation: test success")
            elif test_result_value == 2:
                print("test_timeout")
        with open(buggy_file_path, "w") as f:
            f.writelines(lines)
        copy_files(vul_id_int, trans)
        delete_validation_result_file(vul_id_int)
        
def check_results(vul_id_int, trans):
    
    new_dir = os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int))
    new_compile_result_path = os.path.join(new_dir, "{}_compile_result.txt".format(trans))
    new_testing_results_path = os.path.join(new_dir, "{}_testing_results.json".format(trans))
    new_testing_log_path = os.path.join(new_dir, "{}_testing.log".format(trans))
    new_compile_log_path = os.path.join(new_dir, "{}_compile.log".format(trans))
    if not os.path.exists(new_compile_result_path):
        print("{} not exist".format(new_compile_result_path))
    if not os.path.exists(new_testing_results_path):
        print("{} not exist".format(new_testing_results_path))
    if not os.path.exists(new_testing_log_path):
        print("{} not exist".format(new_testing_log_path))
    if not os.path.exists(new_compile_log_path):
        print("{} not exist".format(new_compile_log_path))
    
def check_validation_detail(vul_id_int):
    '''
    check if the content of each trans testing_results.json is the same
    use the original as the baseline
    '''
    
    def get_one_test_results(vul_id_int,trans):
        result_dict = {}
        new_dir = os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int))
        new_testing_results_path = os.path.join(new_dir, "{}_testing_results.json".format(trans))

        '''
        {
        "tests": {
        "overall_metrics": {
        "number_running": 3597,
        "number_passing": 3596,
        "number_error": 1,
        "number_failing": 0,
        "number_skipping": 0
        },
        "failures": [
        {
            "test_class": "com.alibaba.json.bvt.bug.Issue1005",
            "test_method": "test_for_issue",
            "failure_name": "java.lang.ClassCastException",
            "detail": "java.lang.Integer cannot be cast to java.lang.Byte",
            "is_error": true
        }
        ],
        ...
        }
        '''
        with open(new_testing_results_path, "r") as f:
            results = json.load(f)
        result_dict["tests"] = {}
        result_dict["tests"]["overall_metrics"] = results["tests"]["overall_metrics"]
        result_dict["tests"]["failures"] = results["tests"]["failures"]
        return result_dict

    base_dict = get_one_test_results(vul_id_int, "original")
    for trans in ["structure_change_only", "rename_only","rename+code_structure"]:
        result_dict =  get_one_test_results(vul_id_int,trans)
        # compare the two dict
        if result_dict["tests"]["overall_metrics"] != base_dict["tests"]["overall_metrics"]:
            print(vul_id_int,"overall_metrics not equal")
        # compare two list of dicts (failures)
        for f in base_dict["tests"]["failures"]:
            if f not in result_dict["tests"]["failures"]:
                print(vul_id_int,"failures not equal")
                break
    
        


        

       

    # 
# main funtion:
if __name__ == '__main__':
  

    for vul_id_int in vul4j_bug_id_list:
        validate_trans_vul4j(vul_id_int)
        # delete_validation_result_file(vul_id_int)
        # check_validation_detail(vul_id_int)