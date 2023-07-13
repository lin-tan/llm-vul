import subprocess
import os
import json
from pathlib import Path
import sys
CODEGEN_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]
JAVA_DIR = CODEGEN_FINETUNE_DIR + '../../jasper/'
sys.path.insert(1, CODEGEN_FINETUNE_DIR+'../') # utils file

from util import vjbench_bug_id_list,vul4j_bug_id_list, cve_int_to_name

bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list

def command(cmd):
    # print(cmd)
    process = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    output, err = process.communicate()
    if output != b'' or err != b'':
        print(output)
        print(err)
    return output, err

def get_codegen_finetune_input(buggy_file, rem_start, rem_end, tmp_file):
    os.chdir(JAVA_DIR)
    command([
        'java', '-cp', '.:target:lib/*', 'clm.finetuning.FineTuningData', 'inference',
        buggy_file, str(rem_start), str(rem_end), tmp_file
    ])

def generate_codegen_finetune_input(output_file, obs):
    begin =  "class Main {\n"
    end = "\n}"
    codegen_input = {'config': 'finetune', 'data': {}}

    for vul_int in bug_range_list:
        vul_id = "VUL4J-{}".format(vul_int)
        if vul_int > 1000:
            vul_id = cve_int_to_name[vul_int]
        VUL_FOLDER = os.path.join(CODEGEN_FINETUNE_DIR+'../../VJBench-trans', vul_id)
        bug_location_file = os.path.join(VUL_FOLDER, "buggyline_location.json")
        # read the bug location file
        with open(bug_location_file, 'r') as f:
            buggy_line_dict = json.load(f) 
        buggy_line_list = buggy_line_dict[  trans]
        if trans == "structure_change_only":
            buggy_file = os.path.join(VUL_FOLDER, "{}_code_structure_change_only.java".format(vul_id))
        elif trans == "rename+code_structure":
            buggy_file = os.path.join(VUL_FOLDER, "{}_full_transformation.java".format(vul_id))
        elif  trans== "rename_only":
            buggy_file = os.path.join(VUL_FOLDER, "{}_rename_only.java".format(vul_id))
        elif trans== "original":         
            buggy_file = os.path.join(VUL_FOLDER, "{}_original_method.java".format(vul_id))
        if not os.path.exists(buggy_file):
            continue
     
        buggy_line_start = buggy_line_list[0][0]
        if (len(buggy_line_list[0])==1):
            buggy_line_end = buggy_line_start
        else:
            buggy_line_end = buggy_line_list[0][1]
        rem_loc = "{}-{}".format(buggy_line_start, buggy_line_end)
        
        buggy_file_path = os.path.join(VUL_FOLDER,buggy_file)
        # read the code
        with open(buggy_file_path, 'r') as f:
            buggy_code = f.read()
        # write to a tmo file
        tmp_code = begin + buggy_code + end
        tmp_code_file = os.path.join(CODEGEN_FINETUNE_DIR, "tmp_code.java")
        with open(tmp_code_file, 'w') as f:
            f.write(tmp_code)
        buggy_file_path = tmp_code_file
        buggy_line_start  = buggy_line_start  + 1
        buggy_line_end = buggy_line_end + 1


        tmp_file = CODEGEN_FINETUNE_DIR + 'tmp.json'
    
        get_codegen_finetune_input(buggy_file_path,  buggy_line_start, buggy_line_end, tmp_file)

        filename = vul_id 
        if not os.path.exists(tmp_file):
            print(filename, 'failed.', output_file, 'not found.')
            continue
        print(filename, 'succeeded')

        result = json.load(open(tmp_file, 'r'))
        if result["buggy function before"].strip() == '' and result["buggy line"].strip() == '' and result["buggy function after"].strip() == '':
            print(filename,  'failed. all empty.')
            continue
        codegen_input['data'][filename] = {
            'loc': rem_loc,
            'input': result['buggy function before'] + '// buggy lines start:\n' + result['buggy line'] + '// buggy lines end\n' + result['buggy function after'] + '// fixed lines:\n',
        }

        command(['rm', '-rf', tmp_file])
        command(['rm', '-rf', tmp_code_file])
        json.dump(codegen_input, open(output_file, 'w'), indent=2)      
        


if __name__ == "__main__":

    for trans in [
        "structure_change_only", 
        "rename_only", 
        "rename+code_structure",
        "original"
        ]:
        input_folder = os.path.join(CODEGEN_FINETUNE_DIR,"inputs")
        if not os.path.exists(input_folder):
            os.mkdir(input_folder)  

        input_file = os.path.join(CODEGEN_FINETUNE_DIR,"inputs","input-{}.json".format( trans))
        generate_codegen_finetune_input(input_file, trans)
