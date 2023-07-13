import os
import json
import sys
from pathlib import Path

CODEGEN_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.insert(1, CODEGEN_FINETUNE_DIR+'../') # utils file
from util import vjbench_bug_id_list,vul4j_bug_id_list

import torch
from transformers import AutoTokenizer, CodeGenForCausalLM

this_max_new_tokens = 512
bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list


def generate_codegen_finetune_output(input_file, output_file,model_dir,  model_name, num_output=10):
    tokenizer = AutoTokenizer.from_pretrained(os.path.join(model_dir, 'codegen-6B-multi'))
    model = CodeGenForCausalLM.from_pretrained(os.path.join(model_dir, model_name) )#.to(device_ids[0])
    model.parallelize(device_map)
    
    codegen_output = json.load(open(input_file, 'r'))
    codegen_output['model'] = model_name
    for filename in codegen_output['data']:
        text = codegen_output['data'][filename]['input']
 
        print('generating', filename)

        input_ids = tokenizer(text, return_tensors="pt").input_ids.to(device_ids[0])
        eos_id = tokenizer.convert_tokens_to_ids(tokenizer.eos_token)
        
        try:
            generated_ids = model.generate(
                input_ids, max_new_tokens=this_max_new_tokens, num_beams=num_output, num_return_sequences=num_output, early_stopping=True, 
                pad_token_id=eos_id, eos_token_id=eos_id
            )
        except Exception as e:
            print(e)
            exit()
            continue
        output = []
        for generated_id in generated_ids:
            output.append(tokenizer.decode(generated_id, skip_special_tokens=False))
        codegen_output['data'][filename]['output'] = output
        json.dump(codegen_output, open(output_file, 'w'), indent=2)



# main funtion:
if __name__ == '__main__':
    model_dir = sys.argv[1]
    for trans in [
            "structure_change_only", 
            "rename_only", 
            "rename+code_structure",
            "original"]:
     
        device_map = {
            0: [i for i in range(0, 8)], 
            1: [i for i in range(8, 17)],
            2: [i for i in range(17, 26)],
            3: [i for i in range(26, 33)]
        }
        device_ids = list(device_map.keys())
        input_file = os.path.join(CODEGEN_FINETUNE_DIR,"inputs","input-{}.json".format( trans))
        output_dir = os.path.join(CODEGEN_FINETUNE_DIR,"outputs")
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
        for model_name in ["codegen-6B-finetune"]:
            output_file = os.path.join(output_dir,"output-{}-max_new_tokens-{}-{}.json".format(model_name, this_max_new_tokens, trans))
            generate_codegen_finetune_output(input_file, output_file, model_dir, model_name, num_output=10)
