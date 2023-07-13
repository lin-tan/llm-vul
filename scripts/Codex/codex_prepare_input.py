import subprocess
import os
import json
from pathlib import Path
import sys
CODEX_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] 

sys.path.insert(1, CODEX_DIR+'../') 

from util import dedent_the_whole_method,  ROOT_PATH, DIFF_LIST,cve_int_to_name,vjbench_bug_id_list,vul4j_bug_id_list
comment_config = "INSERTION_CODEX_MULTILINE_COMMENT"
bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list
this_max_new_tokens = 512

def trans_codex_input(output_file, trans):
    codex_input = {'config': comment_config, 'data': {}}
    for vul_int in bug_range_list:

        vul_id = "VUL4J-{}".format(vul_int)
        if vul_int > 100:
            vul_id = cve_int_to_name[vul_int]
        VUL_FOLDER = os.path.join(ROOT_PATH, 'VJBench-trans', vul_id)
        if trans == "structure":
            buggy_file_abs_path = os.path.join(VUL_FOLDER, "{}_code_structure_change_only.java".format(vul_id))
            bug_location_name = "structure_change_only"
        elif trans == "rename+code_structure":
            buggy_file_abs_path = os.path.join(VUL_FOLDER, "{}_full_transformation.java".format(vul_id))
            bug_location_name = "rename+code_structure"
        elif  trans== "rename_only":
            buggy_file_abs_path = os.path.join(VUL_FOLDER, "{}_rename_only.java".format(vul_id))
            bug_location_name  = "rename_only"
        elif  trans== "original":
            buggy_file_abs_path = os.path.join(VUL_FOLDER, "{}_original_method.java".format(vul_id))
            bug_location_name  = "original"
        bug_location_file = os.path.join(VUL_FOLDER, "buggyline_location.json")


  


        with open(bug_location_file, 'r') as f:
            buggy_line_info = json.load(f)[bug_location_name]

        buggy_line_start = buggy_line_info[0][0]
        if (len(buggy_line_info[0])==1):
            buggy_line_end = buggy_line_start
        else:
            buggy_line_end = buggy_line_info[0][1]

        with open(buggy_file_abs_path) as f:
            lines = f.readlines()
       
        buggy_line_list = []

        buggy_method_start = 1
        buggy_method_end = len(lines)

    
        for i in range(buggy_method_start-1, buggy_method_end):
            buggy_line_list.append(lines[i])

    
        min_indent_len= 1000000
        for i in range(buggy_line_start-1, buggy_line_end):
            this_line = lines[i]
            # print(i, this_line)
            j=0
            while(j<len(this_line) and len(this_line[j].strip()) == 0):
                j+=1
            if j < min_indent_len:
                min_indent_len = j
        indent = lines[buggy_line_start-1][:min_indent_len]
        # new_line = lines[i][:min_indent_len] + " * " +lines[i][min_indent_len:]


        new_buggy_line_list = []
        # new_buggy_line_list.append("// Java \n")

        # new 
        for k in range(0,buggy_line_start-buggy_method_start):
            new_buggy_line_list.append(buggy_line_list[k])
        if with_comment:
            new_buggy_line_list.append(buggy_line_list[buggy_line_start-buggy_method_start][:min_indent_len]+ "/* BUG: \n")
            for k in range(buggy_line_start-buggy_method_start,buggy_line_end-buggy_method_start+1):
                buggy_line_strip = buggy_line_list[k].strip()
                new_buggy_line_list.append(buggy_line_list[k][:min_indent_len]+ " * "+buggy_line_list[k][min_indent_len:])
            new_buggy_line_list.append(buggy_line_list[buggy_line_start-buggy_method_start][:min_indent_len]+ " * FIXED: \n")    
            new_buggy_line_list.append(buggy_line_list[buggy_line_start-buggy_method_start][:min_indent_len]+ " */\n")    
                
        suffix = []
        for k in range(buggy_line_end-buggy_method_start+1,len(buggy_line_list)):
            suffix.append(buggy_line_list[k])

        d_input, d_suffix = dedent_the_whole_method(new_buggy_line_list,suffix)
        prefix_prompt = "// Java \n"+"".join(d_input)
        suffix_prompt = "".join(d_suffix)

        filename = vul_id 
        codex_input['data'][filename] = {
    
            'prefix':prefix_prompt,
            'suffix': suffix_prompt 
        }
        json.dump(codex_input, open(output_file, 'w'), indent=2)      



with_comment =True

# main 
if __name__ == "__main__":
    

    for trans in ["original", "rename_only","rename+code_structure","structure"]:
        input_file = os.path.join(CODEX_DIR,"inputs","input-{}-{}.json".format(comment_config, trans))
        trans_codex_input(input_file, trans)