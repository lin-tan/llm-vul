import os
import json
import subprocess
import sys
import re
from pathlib import Path
import multiprocessing

INCODER_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 
sys.path.insert(1, INCODER_DIR+'../') # utils file

from util import vul4j_compile_java_file, remove_multiline_comment,vul4j_test_java_file,vul4j_bug_id_list, ROOT_PATH, info_json, VUL4J_DIR
from util import extract_correct_method_code, translate_code


validation_folder = os.path.join(ROOT_PATH, "Model_patches","validation", "incoder_vul4j")
if not os.path.exists(validation_folder):
    os.mkdir(validation_folder)


def incoder_output_to_patch(output, config= 'INCODER_COMPLETE_CODEFORM_COMMENTFORM_NOCOMMENT'):
    if config in ['INCODER_COMPLETE_CODEFORM_NOCOMMENT', 'INCODER_COMPLETE_CODEFORM_COMMENTFORM_NOCOMMENT']:
        """
        find the } that matches the first { in the output
        """
        output = output[len('<|endoftext|>'):]
        output = output.split('<|mask:0|>')
        if len(output) < 3:
            return ''
        output = output[0] + output[2]

        output = output.strip().split('\n')
        no_comment_output = [line for line in output if not line.strip().startswith('//')]
        output = '\n'.join(no_comment_output)
        output = remove_multiline_comment(output)
        stack = ['{']
        try:
            start_index = output.index('{')
            patch = output[: start_index + 1]
            for c in output[start_index + 1: ]:
                patch += c
                if c == '}':
                    top = stack.pop()
                    if top != '{':
                        return ''
                    if len(stack) == 0:
                        return patch.strip()
                elif c == '{':
                    stack.append(c)
            return ''
        except Exception as e:
            return ''
                            
def validate_incoder_vul4j( vul_id_int):
    
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
            codex_folder =  os.path.join(vul4j_project_path,"VUL4J")
            if not os.path.exists(codex_folder):
                os.makedirs(codex_folder)
            original_file_path = os.path.join(vul4j_project_path,"VUL4J","original_whole_file.txt")

            print("successfully get the info")
            break
    

    with open(original_file_path, "r") as f:
        lines = f.readlines()

    
    for idx in [1]:
        validation_result = []
        code_before, code_after, flag = extract_correct_method_code(raw_vul_id, trans)
        if not flag:
            continue
        vul_id = raw_vul_id
        results = incoder_result["data"][vul_id]
        prompt = results["input"]
        outputs = results["output"]
        
        validation_result_file_name = "{}_{}.json".format(os.path.basename(incoder_output_file)[:-5], vul_id )
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
            generated_code = incoder_output_to_patch(outputs[i])

            vdict = {"patch":generated_code, "correctness":""}
            duplicated = False
            check_dupliacted = re.sub('\\s+', '', generated_code).strip()

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
                    incoder_result["data"][vul_id] = results
                # write the incoder_result back to the json file
                    with open(validation_result_file, "w") as f:
                        json.dump(incoder_result["data"][vul_id], f, indent=4)
                    print("duplicated")
                    break

            if not duplicated:
                
                if trans != "original" and trans !="structure_change_only":
                    generated_code = translate_code(generated_code,vul_id_int)  
                    vdict["translated"] = generated_code

                with open(buggy_file_path, "w") as f:
                    f.writelines(lines[:buggy_method_start-1])
                    f.write(generated_code)                 
                    f.writelines(lines[buggy_method_end:])
                print("validation: start compiling")
                if not vul4j_compile_java_file(vul4j_project_path, compile_cmd):
                    print("validation: compile failed")
                    vdict["correctness"] = "uncompilable"
                else: 
                    vdict["correctness"] = "compile_success"
                    print("validation: start testing")
                    test_result_value =  vul4j_test_java_file(vul4j_project_path, test_cmd)
                    if test_result_value == 1:
                        print("validation: test success")
                        vdict["correctness"] = "test_success"
                    elif test_result_value == 2:
                        print("test_timeout")
                        vdict["correctness"] = "test_timeout"
                validation_result.append(vdict)
                results["validation_result"] = validation_result
                incoder_result["data"][vul_id]=results
                with open(validation_result_file, "w") as f:
                    json.dump(incoder_result["data"][vul_id], f, indent=4)
        # exit()
    with open(buggy_file_path, "w") as f:
        f.writelines(lines)
            
# main funtion:
if __name__ == '__main__':

    for model_name in ["incoder-6B"]:
        for trans in [ "original", "rename_only", "rename+code_structure","structure_change_only"]:
            incoder_output_file = os.path.join(ROOT_PATH,"Model_patches","model_output","InCoder","output-incoder-6B-{}.json".format(trans))

            with open(incoder_output_file, "r") as f:
                incoder_result = json.load(f)
            # validate_incoder_vul4j(50)
            # exit()

            p = multiprocessing.Pool(1)
            p.map(validate_incoder_vul4j, vul4j_bug_id_list )
            print("Validatoin finished")
