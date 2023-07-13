import subprocess
import os
import json
from pathlib import Path
import sys
INCODER_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.insert(1, INCODER_DIR+'../') # utils file

from util import dedent_the_whole_method,vjbench_bug_id_list,vul4j_bug_id_list, cve_int_to_name

bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list


def genearte_incoder_input(output_file, trans):
    codex_input = {'config': comment_config, 'data': {}}
    for vul_int in bug_range_list:
        vul_id = "VUL4J-{}".format(vul_int)
        if vul_int > 1000:
            vul_id = cve_int_to_name[vul_int]
        VUL_FOLDER = os.path.join(INCODER_DIR+'../../VJBench-trans', vul_id)
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
    
        #####################################################
        buggy_file_abs_path = buggy_file
        with open(buggy_file_abs_path) as f:
            lines = f.readlines()
        # copy the lines to a new file from the buggy method start to the buggy method end
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
                # print(j,this_line[j])
                # # and 
                # print(len(this_line[j].strip()))
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
            new_buggy_line_list.append(buggy_line_list[buggy_line_start-buggy_method_start][:min_indent_len]+ "<|mask:0|>\n")    
        else:
                
            new_buggy_line_list.append(buggy_line_list[buggy_line_start-buggy_method_start][:min_indent_len]+ "<|mask:0|>\n")    

        suffix = []
        for k in range(buggy_line_end-buggy_method_start+1,len(buggy_line_list)):
            suffix.append(buggy_line_list[k])
        # extract_line = buggy_line_list[buggy_line_start-buggy_method_start]
        # new_buggy_line_list.append(extract_line.replace(extract_line.strip(),"<extra_id_0>"))
        # for k in range(buggy_line_end -buggy_method_start+1,len(buggy_line_list)):    
        #     new_buggy_line_list.append(buggy_line_list[k])
        # new 
        d_input, d_suffix = dedent_the_whole_method(new_buggy_line_list,suffix)
        prefix_prompt = "".join(d_input)
        suffix_prompt = "".join(d_suffix)
        # if suffix not end with \n, add it
        if suffix_prompt[-1] != "\n":
            suffix_prompt += "\n"

        filename = vul_id
        codex_input['data'][filename] = {
    
            'input':prefix_prompt + suffix_prompt +"<|mask:0|>"
        }
        # dump to json file
        with open(output_file, 'w') as f:
            json.dump(codex_input, f, indent=2)



with_comment = True


if __name__ == "__main__":

    for trans in [
        "structure_change_only", 
        "rename_only", 
        "rename+code_structure",
        "original"
        ]:
  
        if not with_comment:
            comment_config = "NO_COMMENT"
        else:
            comment_config = "WITH_COMMENT"

        input_folder = os.path.join(INCODER_DIR,"inputs")
        if not os.path.exists(input_folder):
            os.mkdir(input_folder)
        input_file = os.path.join(INCODER_DIR,"inputs","input-{}.json".format( trans))
        genearte_incoder_input(input_file, trans)