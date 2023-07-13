import os
import json
import sys
from pathlib import Path

INCODER_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.insert(1, INCODER_DIR+'../') # utils file

import torch
from transformers import AutoTokenizer, AutoModelForCausalLM

this_max_new_tokens = 512

def generate_incoder_output(input_file, output_file, model_dir, model_name, num_output=10):
    tokenizer = AutoTokenizer.from_pretrained(os.path.join(model_dir, model_name))
    model = AutoModelForCausalLM.from_pretrained(os.path.join(model_dir, model_name))
    model.parallelize(device_map)
    
    incoder_output = json.load(open(input_file, 'r'))
    incoder_output['model'] = model_name
    if os.path.exists(output_file):
        incoder_output = json.load(open(output_file, 'r'))

    for filename in incoder_output['data']:
        if filename in incoder_output['data'] and 'output' in incoder_output['data'][filename]:
            continue
        text = incoder_output['data'][filename]['input']

        print('generating', filename)
   
        try:
            input_ids = tokenizer(text, return_tensors="pt").input_ids.to(device_ids[0])
            eos_id = tokenizer.convert_tokens_to_ids('<|endofmask|>')
            generated_ids = model.generate(
                input_ids, max_new_tokens=this_max_new_tokens, num_beams=num_output, num_return_sequences=num_output, early_stopping=True,
                pad_token_id=eos_id, eos_token_id=eos_id
            )
        except Exception as e:
            print(e)
            continue
        output = []
        for generated_id in generated_ids:
            output.append(tokenizer.decode(generated_id, skip_special_tokens=False, clean_up_tokenization_spaces=False))
        incoder_output['data'][filename]['output'] = output
        json.dump(incoder_output, open(output_file, 'w'), indent=2)




if __name__ == '__main__':
    model_dir = sys.argv[1]
    for comment_config in ['WITH_COMMENT']:  # or 'NO_COMMENT'
        for trans in [
          "structure_change_only", 
            "rename_only", 
            "rename+code_structure",
            "original"
           
            ]:
           
        
            device_map = {
  
                0: [i for i in range(0, 7)], 
                1: [i for i in range(7, 16)],
                2: [i for i in range(16, 25)],
                3: [i for i in range(25, 32)]
            }
            device_ids = list(device_map.keys())
            input_file = os.path.join(INCODER_DIR,"inputs","input-{}.json".format( trans))
            output_dir = os.path.join(INCODER_DIR,"outputs")
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)
            for model_name in ["incoder-6B"]:
                output_file = os.path.join(output_dir,"output-{}-max_new_tokens-{}-{}-{}.json".format(model_name, this_max_new_tokens, trans,comment_config))
                generate_incoder_output(input_file, output_file,model_dir, model_name, num_output=10)
