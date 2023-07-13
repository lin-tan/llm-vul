
import os
import openai
import json
import time
from pathlib import Path
temp = 0.6
top_p = 1.0
openai.api_key =  os.environ.get('OPENAI_API_KEY')
print(openai.api_key)
max_tokens = 512
engine =  "code-davinci-002"
stop = ["public",  "//C#","//Python","//Go"]
os.environ["TOKENIZERS_PARALLELISM"] = "false"

comment_config = "INSERTION_CODEX_MULTILINE_COMMENT"
# comment_config = "WITH_COMMENT"
# comment_config = "NO_COMMENT"


CODEX_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1] # 


def generate_codex_patch(input_file, config, xxx , obs):
    print("start generating")
    if obs == "original":
      
        output_foler = os.path.join(CODEX_DIR,"outputs","{}-{}-WITH_COMMENT".format(obs_config, xxx))
    else:
        output_foler = os.path.join(CODEX_DIR,"outputs","{}-{}".format(obs_config, xxx))
    if not os.path.exists(output_foler):
        os.makedirs(output_foler)

    # open input file (a json file)
    with open(input_file, 'r') as f:
        input_list = json.load(f)
    data = input_list['data']
    
    for cve_id in data:
        # if cve_id != "VUL4J-1008-1":
        #     continue
        # "VUL4J-1004-1"
        print("generating for {}".format(cve_id)   )
     
        output_file = "{}_temp{}_maxtoken{}_{}_codex_{}_.json".format(cve_id , temp, max_tokens, config,obs_config)
        
        print(os.path.join(output_foler,output_file ))
        if os.path.exists(os.path.join(output_foler,output_file)):
            print("{} already exists".format(os.path.join(output_foler,output_file)))
            continue
        prompt_text = data[cve_id]["prefix"]
        suffix_text = data[cve_id]["suffix"]
        print(cve_id)
        print(prompt_text)
        print("----------------------------------------------------------------------------------------------------")
        print(suffix_text)
        print()

        while True:
            round = 0
            try:
                a_a = False
                response = openai.Completion.create(
                            engine=engine, 
                            prompt=prompt_text, 
                            max_tokens= max_tokens,
                            temperature=temp,
                            top_p=top_p,
                            n=10,
                            suffix=suffix_text,
                            stop=stop,
                            logprobs=1)
                a_a = True
                if a_a:
                    output_path = os.path.join(output_foler,output_file )
                    with open(output_path, 'w') as f:
                        json.dump(response, f, indent=4)
                    print("output file saved to {}".format(output_path))
                    # exit()
                    break

            except openai.error.RateLimitError as e:
                
                print("Rate limit Exception:", e)
                print("Waiting 90 seconds and trying again")
                delay_time = 90 + 30*round
                round =  round + 1
                time.sleep(delay_time)
                continue
                # response = openai.Completion.create(
                #             engine=engine, 
                #             prompt=prompt_text, 
                #             max_tokens= max_tokens,
                #             temperature=temp,
                #             top_p=top_p,
                #             n=10,
                #             suffix=suffix_text,
                #             stop=stop,
                #             logprobs=1)
            except openai.error.InvalidRequestError as e:
                print("Invalid Request Exception:", e)
                continue
        

        
        

if __name__ == "__main__":
    global obs_config
    global obs
    config_list = [
        "INSERTION_CODEX_MULTILINE_COMMENT"
    ]
    # strucutre
    # for obs in ["original"]:
    # for obs in ["rename_only"]:
    for obs in [  
        # "structure",
        "rename+code_structure",
        "rename_only",
        # "original"
        ]:
        obs_config = obs
        for xxx in range(1,26):
            for config in config_list:
                input_file = os.path.join(CODEX_DIR,"inputs","input-{}-{}.json".format(comment_config, obs))
                generate_codex_patch(input_file,config, xxx, obs)
            
            