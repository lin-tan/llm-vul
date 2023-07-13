import os
import json
import sys
from pathlib import Path

CODET5_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.insert(1, CODET5_DIR+'../') # utils file
from util import vjbench_bug_id_list,vul4j_bug_id_list

import torch
from transformers import RobertaTokenizer, T5ForConditionalGeneration

this_max_new_tokens = 512
bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list

def generate_codet5_output(input_file, output_file, model_dir, model_name, num_output=10):
    tokenizer = RobertaTokenizer.from_pretrained(os.path.join(model_dir , model_name))
    model = T5ForConditionalGeneration.from_pretrained(os.path.join(model_dir , model_name)).to(device)

    codet5_output = json.load(open(input_file, 'r'))
    codet5_output['model'] = model_name
    for filename in codet5_output['data']:
        text = codet5_output['data'][filename]['input']
        print('generating', filename)
        input_ids = tokenizer(text, return_tensors="pt").input_ids.to(device)
        eos_id = tokenizer.convert_tokens_to_ids(tokenizer.eos_token)

        try:
            generated_ids = model.generate(
                input_ids, max_new_tokens=this_max_new_tokens , num_beams=num_output, num_return_sequences=num_output, early_stopping=True, 
                # pad_token_id=eos_id, 
                eos_token_id=eos_id
            )
        except Exception as e:
             continue
        output = []
        for generated_id in generated_ids:
            output.append(tokenizer.decode(generated_id,  skip_special_tokens=True))
        codet5_output['data'][filename]['output'] = output
        json.dump(codet5_output, open(output_file, 'w'), indent=2)

if __name__ == '__main__':
    device = 0
    model_dir = sys.argv[1]
    for comment_config in ['WITH_COMMENT']:  # or 'NO_COMMENT'
        for trans in [
          "structure_change_only", 
            "rename_only", 
            "rename+code_structure",
            "original"
           
            ]:
           
            input_file = os.path.join(CODET5_DIR,"inputs","input-{}.json".format( trans))
            output_dir = os.path.join(CODET5_DIR,"outputs")
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)
            for model_name in ["codet5-large"]:
                output_file = os.path.join(output_dir,"output-{}-max_new_tokens-{}-{}-{}.json".format(model_name, this_max_new_tokens, trans,comment_config))
                generate_codet5_output(input_file, output_file,model_dir, model_name, num_output=10)
