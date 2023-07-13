import os
import json
import subprocess
import sys
import re
from pathlib import Path
import multiprocessing

INCODER_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, INCODER_FINETUNE_DIR+'../') # utils file

from util import  cve_test_java_file, cve_compile_java_file, VJBENCH_DIR , info_json, vjbench_json, ROOT_PATH
from util import extract_correct_method_code, translate_code, cve_name_to_int,read_test_results_gradle,read_test_results_maven




def get_transformed_method(vul_id, trans):
    dir_path = os.path.join(ROOT_PATH,"VJBench-trans","{}".format(vul_id))
    if trans == "rename+code_structure":
        f_path = os.path.join(dir_path, "{}_full_transformation.java").format(vul_id)
    elif trans == "structure_change_only":
        f_path = os.path.join(dir_path, "{}_code_structure_change_only.java").format(vul_id)
    else:
        f_path = os.path.join(dir_path, "{}_{}.java").format(vul_id, trans)
    with open(f_path, "r") as f:
        return f.read()
                            
                            
def validate_trans_vjbench( vul_id:str):
    # if vul_id is str, convert it to int
    if isinstance(vul_id, str):
        raw_vul_id = "VUL4J-{}".format(cve_name_to_int[vul_id])
    else:
        print("Please input a valid VJBench Bug ID")
        return

    with open(info_json, "r") as f:
        all_info_list = json.load(f)
    for info in all_info_list:
        if info["vul_id"] == raw_vul_id:
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
            vul = {'vul_id':vul_id,'project': vul_id, 'project_url':vjbench_info[vul_id]["github_url"], 'cve_id':info["CVE_id"], 'human_patch': info['human_patch'],'human_patch_url': info['human_patch']}


    with open(buggy_file_path, "r") as f:
        lines = f.readlines()

    for trans in [ "rename+code_structure","rename_only","original","structure_change_only"]: 
        if os.path.exists(os.path.join(INCODER_FINETUNE_DIR ,"trans_verify",vul_id, "{}_{}_complie_result.txt".format(vul_id,trans))):
            # and os.path.exists(os.path.join(INCODER_FINETUNE_DIR ,"trans_verify","VUL4J-{}".format(vul_id_int), "{}_testing_results.json".format(trans))) :
            continue
        print("validation: start validating {}".format(trans))
        if trans == "original":
            # simply write lines to the whole file
            with open(buggy_file_path, "w") as f:
                f.writelines(lines)
        else:
            transfmormed_method = get_transformed_method(vul_id, trans)
            if trans != "original" and trans != "structure_change_only":
                
                transfmormed_method = translate_code(transfmormed_method,vul_id)
            with open(buggy_file_path, "w") as f:
                f.writelines(lines[:buggy_method_start-1])
                f.write(transfmormed_method)
                f.writelines(lines[buggy_method_end:])
        # exit()
        c = -1
        print(transfmormed_method)
        print("validation: start compiling")
        if not cve_compile_java_file(project_path, compile_cmd):
            print("validation: compile failed")
            # print("validation: exit")
            # exit()
            c = 0
        else: 
            c = 1
            print("validation: start testing")
            test_result_value =  cve_test_java_file(project_path, test_cmd)
            if test_result_value == 1:
                print("validation: test success")
            elif test_result_value == 2:
                print("test_timeout")
        test_json_data = {}
        if "gradle" in compile_cmd:
            test_json_data = read_test_results_gradle(vul, project_path)
        elif "mvn" in compile_cmd:
            test_json_data = read_test_results_maven(vul, project_path)
        save_dir = os.path.join(INCODER_FINETUNE_DIR ,"trans_verify",vul_id)
        # write test results to json file
        if not os.path.exists(save_dir):
            os.makedirs(save_dir)
        json_name = "{}_{}_test.json".format(vul_id,trans)
        with open(os.path.join(save_dir,json_name), "w") as f:
            json.dump(test_json_data, f, indent=4)
        # write c to txt file
        with open(os.path.join(save_dir,"{}_{}_complie_result.txt".format(vul_id,trans)), "w") as f:
            f.write(str(c))
    
        
        
    with open(buggy_file_path, "w") as f:
        f.writelines(lines)
            
def check_validation_detail(vul_id):
    '''
    check if the content of each trans testing_results.json is the same
    use the original as the baseline
    '''
    
    def get_one_test_results(vul_id,trans):
        result_dict = {}
        new_dir = os.path.join(INCODER_FINETUNE_DIR ,"trans_verify",vul_id)
        new_testing_results_path = os.path.join(new_dir,  "{}_{}_test.json".format(vul_id,trans))

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

    base_dict = get_one_test_results(vul_id, "original")
    for trans in ["structure_change_only", "rename+code_structure","rename_only"]: # 
        result_dict =  get_one_test_results(vul_id,trans)
        # compare the two dict
        if result_dict["tests"]["overall_metrics"] != base_dict["tests"]["overall_metrics"]:
            print(vul_id,"overall_metrics not equal")
        # compare two list of dicts (failures)
        for f in base_dict["tests"]["failures"]:
            if f not in result_dict["tests"]["failures"]:
                print(vul_id,"failures not equal")
                break
    
# main funtion:
if __name__ == '__main__':

    bug_range_list  = cve_name_to_int.keys()
    # print("bug_range_list: ", bug_range_list)
    # ['Jenkins-3', 'Jinjava-1', 'Jenkins-1', 'Quartz-1', 'Retrofit-1', 'Ratpack-1', 'Json-sanitizer-1', 'Jenkins-2', 'Flow-1', 'Pulsar-1', 'Netty-1', 'BC-Java-1', 'Halo-1', 'Flow-2', 'Netty-2']
    
    for vul_id in bug_range_list:
        validate_trans_vjbench(vul_id)
        check_validation_detail(vul_id)
