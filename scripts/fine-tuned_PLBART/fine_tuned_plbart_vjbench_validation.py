import os
import json
import subprocess
import sys
import re
from pathlib import Path
import multiprocessing

PLBART_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, PLBART_FINETUNE_DIR+'../') # utils file

from util import  cve_test_java_file, cve_compile_java_file, VJBENCH_DIR , info_json, vjbench_json, ROOT_PATH
from util import extract_correct_method_code, translate_code, cve_name_to_int


validation_folder = os.path.join(ROOT_PATH, "Model_patches","validation", "fine_tuned_plbart_vjbench")
if not os.path.exists(validation_folder):
    os.mkdir(validation_folder)


def plbart_output_to_patch(output):
    return output.strip()
                            
def validate_plbart_vjbench( vul_id):
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

    with open(buggy_file_path, "r") as f:
        lines = f.readlines()


    validation_result = []
    code_before, code_after, flag = extract_correct_method_code(vul_id, trans)
    if not flag:
        return
    results = plbart_result["data"][vul_id]
    prompt = results["input"]
    if "output" not in results:
        return
    outputs = results["output"]
    
    
    validation_result_file_name = "{}_{}.json".format(os.path.basename(plbart_output_file)[:-5], vul_id )
    validation_result_file = os.path.join(validation_folder, validation_result_file_name)

    existing_validation_result_length = -1
    if os.path.exists(validation_result_file):
        with open(validation_result_file, "r") as f:
            existing_validation_result = json.load(f)["validation_result"]
            # get the length of the existing validation result
            existing_validation_result_length = len(existing_validation_result)
            validation_result = existing_validation_result
        

    for i in range(len(outputs)):
        # if smaller than the existing validation result, skip
        if i < existing_validation_result_length:
            continue
        generated_code = plbart_output_to_patch(outputs[i])

        vdict = {"patch":generated_code, "correctness":""}
        duplicated = False
        check_dupliacted = re.sub('\\s+', '', generated_code).strip()
        # what does re.sub('\\s+', '')do ?
        # It replaces all whitespace characters with nothing.

        for v in validation_result:
            check_v =  re.sub('\\s+', '', v["patch"]).strip()
            # print(check_v)
            if check_dupliacted == check_v:
                duplicated = True
                vdict["correctness"] = v["correctness"]
                if trans != "original" and trans != "structure_change_only":
                    vdict["translated"] = v["translated"]
                validation_result.append(vdict)
                results["validation_result"] = validation_result
                plbart_result["data"][vul_id] = results
            # write the plbart_result back to the json file
                with open(validation_result_file, "w") as f:
                    json.dump(plbart_result["data"][vul_id], f, indent=4)
                print("duplicated")
                break
        # exit()

        if not duplicated:
            if trans != "original" and trans != "structure_change_only":
                generated_code = translate_code(generated_code,vul_id)
                vdict["translated"] = generated_code

            with open(buggy_file_path, "w") as f:
                f.writelines(lines[:buggy_method_start-1])
                f.writelines(code_before)
                f.write(generated_code)
                f.writelines(code_after)
                f.writelines(lines[buggy_method_end:])
            # exit()
            print("validation: start compiling")
            if not cve_compile_java_file(project_path, compile_cmd):
                print("validation: compile failed")
                vdict["correctness"] = "uncompilable"
            else: 
                vdict["correctness"] = "compile_success"
                print("validation: start testing")
                test_result_value =  cve_test_java_file(project_path, test_cmd)
                if test_result_value == 1:
                    print("validation: test success")
                    vdict["correctness"] = "test_success"
                elif test_result_value == 2:
                    print("test_timeout")
                    vdict["correctness"] = "test_timeout"
            validation_result.append(vdict)
            results["validation_result"] = validation_result
            plbart_result["data"][vul_id]=results
            with open(validation_result_file, "w") as f:
                json.dump(plbart_result["data"][vul_id], f, indent=4)
            # exit()
    with open(buggy_file_path, "w") as f:
        f.writelines(lines)
            
# main funtion:
if __name__ == '__main__':
    bug_range_list  = cve_name_to_int.keys()
    for model_name in ["plbart-large-finetune"]:
        for trans in ["rename+code_structure","original", "structure_change_only","rename_only"]:
            plbart_output_file = os.path.join(ROOT_PATH,"Model_patches","model_output","fine-tuned_PLBART","output-plbart-large-finetune-{}.json".format(trans))
            with open(plbart_output_file, "r") as f:
                plbart_result = json.load(f)
            # validate_plbart_vjbench("Halo-1")
            # exit()
            p = multiprocessing.Pool(1)
            p.map(validate_plbart_vjbench, cve_name_to_int )
            print("Validatoin finished")