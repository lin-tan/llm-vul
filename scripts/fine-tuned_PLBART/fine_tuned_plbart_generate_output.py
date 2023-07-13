import os
import json
import sys
from pathlib import Path

PLBART_FINETUNE_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.insert(1, PLBART_FINETUNE_DIR+'../') # utils file
from util import vjbench_bug_id_list,vul4j_bug_id_list

import torch
from transformers import PLBartForConditionalGeneration, PLBartTokenizer
this_max_new_tokens = 512
bug_range_list =  vjbench_bug_id_list+vul4j_bug_id_list

def generate_plbart_finetune_output(input_file, output_file, model_dir, model_name, num_output=10):
    tokenizer = PLBartTokenizer.from_pretrained(os.path.join(model_dir ,"plbart-large"), src_lang="java", tgt_lang="java")
    model = PLBartForConditionalGeneration.from_pretrained(os.path.join(model_dir ,model_name)).to(device)
    
    plbart_output = json.load(open(input_file, 'r'))
    plbart_output['model'] = model_name
    for filename in plbart_output['data']:

        text = plbart_output['data'][filename]['input']

        print('generating', filename)
        print('input:')
        print(text)

        input_ids = tokenizer(text, add_special_tokens=False, return_tensors="pt").input_ids.to(device)
        try:
            generated_ids = model.generate(
                input_ids, max_new_tokens=this_max_new_tokens, num_beams=num_output, num_return_sequences=num_output, 
                early_stopping=True, decoder_start_token_id=tokenizer.lang_code_to_id["java"]
            )
        except Exception as e:
            continue
        output = []
        for generated_id in generated_ids:
            output.append(tokenizer.decode(generated_id, skip_special_tokens=True))
        plbart_output['data'][filename]['output'] = output
        json.dump(plbart_output, open(output_file, 'w'), indent=2)





# main funtion:
if __name__ == '__main__':
    model_dir = sys.argv[1]
    device = 0
    for trans in [
            "structure_change_only", 
            "rename_only", 
            "rename+code_structure",
            "original"]:
        input_file = os.path.join(PLBART_FINETUNE_DIR,"inputs","input-{}.json".format( trans))
        output_dir = os.path.join(PLBART_FINETUNE_DIR,"outputs")
        if not os.path.exists(output_dir):
            os.makedirs(output_dir)
        for model_name in ["plbart-large-finetune"]:
            output_file = os.path.join(output_dir,"output-{}-max_new_tokens-{}-{}.json".format(model_name, this_max_new_tokens, trans))
            generate_plbart_finetune_output(input_file, output_file, model_dir, model_name, num_output=10)

