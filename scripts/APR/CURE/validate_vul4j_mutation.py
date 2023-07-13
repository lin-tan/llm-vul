import json
import os
import shutil
import time
import subprocess
import multiprocessing
import sys

CURE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]
sys.path.insert(1, CURE_DIR+'../../') # utils file

from util import vul4j_compile_java_file, vul4j_test_java_file,vul4j_bug_id_list, ROOT_PATH, info_json, VUL4J_DIR
from util import extract_correct_method_code, translate_code


# sys.path.append(VALIDATE_QUIXBUGS_DIR + '../dataloader/')

import tokenization


def get_strings_numbers(file_path, loc):
    numbers_set = {}
    strings_set = {}
    with open(file_path, 'r') as file:
        data = file.readlines()
        for idx, line in enumerate(data):
            dist = loc - idx - 1
            strings, numbers = tokenization.get_strings_numbers(line)
            for num in numbers:
                if num != '0' and num != '1':
                    if num in numbers_set:
                        numbers_set[num] = min(dist, numbers_set[num])
                    else:
                        numbers_set[num] = dist
            for str in strings:
                if str in strings_set:
                    strings_set[str] = min(dist, strings_set[str])
                else:
                    strings_set[str] = dist
    final_strings = []
    final_numbers = []
    for k, v in numbers_set.items():
        final_numbers.append([k, v])
    for k, v in strings_set.items():
        final_strings.append([k, v])
    final_numbers.sort(key=lambda x: x[1])
    final_strings.sort(key=lambda x: x[1])
    return final_strings, final_numbers



def validate_cure(vul_id): 
    if os.path.exists(validated_result_path.format(vul_id)):
        print("{} already validated".format(vul_id))
        return
    vul_id_int = int(vul_id.split("-")[1])
    with open(reranked_result_path.format(vul_id), 'r') as f:
        reranked_result = json.load(f)
    find = 0
    project_path = os.path.join(VUL4J_DIR,vul_id)
    with open(info_json, "r") as f:
        all_info_list = json.load(f)
    for info in all_info_list:
        if info["vul_id"] == vul_id:
            compile_cmd = "vul4j compile -d {}".format(project_path)
            test_cmd = "vul4j test -d {}".format(project_path)
            if "buggy_file" not in info:
                print("{} buggy file not exist".format(vul_id_int))
                return
            buggy_file_path = info["buggy_file"]
            buggy_file_path = os.path.join(project_path,buggy_file_path)
            buggy_method_start = info["buggy_method_with_comment"][0][0]
            buggy_method_end = info["buggy_method_with_comment"][0][1]
            buggy_line_start = info["buggy_line"][0][0]
        

            if (len(info["buggy_line"][0])==1):
                buggy_line_end = buggy_line_start
            else:
                buggy_line_end = info["buggy_line"][0][1]

            codex_folder =  os.path.join(project_path,"VUL4J")
            if not os.path.exists(codex_folder):
                os.makedirs(codex_folder)
            original_file_path = os.path.join(project_path,"VUL4J","original_whole_file.txt")
            print("successfully get the info")
            find =1
            break

    if find ==0:
        return
    if not os.path.exists(original_file_path):
        # copy the buggy_file to the original_file_path
        cmd = "cp {} {}".format(buggy_file_path, original_file_path)
        os.system(cmd)

    with open(original_file_path, "r") as f:
        lines = f.readlines()
    
    validated_result = {}
    bug_start_time = time.time()
    for vul  in reranked_result.keys():
        validated_result[vul] = {'src': reranked_result[vul]['src'], 'patches': []}
        buggy_line_start = int(vul.split("-")[2])
        buggy_line_end = int(vul.split("-")[3])
        patches = reranked_result[vul]["patches"]
        for idx, patch_score in enumerate(patches):
            if idx >= 10:
                return
            # validate 5 hours for each bug at most
            if time.time() - bug_start_time > 5 * 3600:
                break
            # validate 5000 patches for each bug at most
            if len(validated_result[vul]['patches']) >= 5000:
                break

            tokenized_patch = patch_score["patch"]

            strings, numbers = get_strings_numbers(buggy_file_path, (int(buggy_line_start) + int(buggy_line_end)) // 2)
            strings = [item[0] for item in strings][:5]
            numbers = [item[0] for item in numbers][:5]
            # one tokenized patch may be reconstructed to multiple source-code patches
            reconstructed_patches = tokenization.token2statement(tokenized_patch.split(' '), numbers, strings)
            # validate most 5 source-code patches come from the same tokenized patch
            rlen = len(reconstructed_patches)
            if rlen > 5:
                rlen = 5
            
        
            code_before, code_after, flag = extract_correct_method_code(vul_id, 1,trans)
     
            for patch in reconstructed_patches[:rlen]:
                patch = patch.strip()
                generated_code = translate_code(patch,vul_id_int)
                correctness = ""
                with open(buggy_file_path, "w") as f:
                    f.writelines(lines[:buggy_method_start-1])
                    f.writelines(code_before)
                    f.writelines([generated_code])
                    f.write("\n")
                    f.writelines(code_after)
                    f.writelines(lines[buggy_method_end :])
                
                print("BUGGY FILE: {}".format(buggy_file_path))
                # exit()
                print("validation: start compiling")
                if not vul4j_compile_java_file(project_path, compile_cmd):
                    print("validation: compile failed")
                    correctness  = "uncompilable"
                else: 
                    correctness  = "compile_success"
                    print("validation: start testing")
                    test_result_value = vul4j_test_java_file(project_path, test_cmd)
                    if test_result_value == 1:
                        print("validation: test success")
                        correctness  = "test_success"
                    elif test_result_value == 2:
                        correctness  = "test_timeout"
                validated_result[vul]['patches'].append({
                    "patch": patch, "correctness": correctness, "translated":generated_code,
                })

                with open(validated_result_path.format(vul_id), 'w') as f:
                    json.dump(validated_result, f, indent=4)
           
                with open(buggy_file_path, "w") as f:
                    f.writelines(lines)
                

if __name__ == '__main__':
    vul_id = "VUL4J-1"
    global trans
    trans = "structure_change_only"
    reranked_result_path = os.path.join(ROOT_PATH,"Model_patches","model_output","CURE",trans,"{}_patches.json".format(vul_id))
    validated_result_path =  os.path.join(ROOT_PATH,"Model_patches","validation","CURE","{}_{}_validated_patches.json".format(trans,vul_id))
    validate_cure(vul_id)

