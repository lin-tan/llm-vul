import json
import sys
import os

SRC_DIR = os.path.abspath(__file__)[: os.path.abspath(__file__).rindex('/') + 1]

sys.path.append(SRC_DIR + '/tester/')
sys.path.append(SRC_DIR + '/validation/')

from generator import generate_knod, read_vul_meta
from vul_rerank import rerank_patches_by_rank
# from vul_validate import validate_vul


def generate_general_ast_patches(input_dir, output_file, model_file):
    # step 1: generate the AST of patches
    meta = read_vul_meta(input_dir + 'meta_localize.txt')
    identifier_file = input_dir + 'identifiers.json'
    class_name_list = [filename.split('/')[-1][:-5] for (bug, dir, filename) in meta]
    
    # use general.pt to generate general patches
    # model_file = SRC_DIR + '../data/models/general.pt'
    input_file = input_dir + 'input_general_ast.json'
    # output_file = output_dir + 'general.txt'
    generate_knod(model_file, input_file, identifier_file, class_name_list, devices, beam_size, output_file)


def generate_insert_ast_patches(input_dir, output_file, model_file):
    # step 1: generate the AST of patches
    meta = read_vul_meta(input_dir + 'meta_localize.txt')
    identifier_file = input_dir + 'identifiers.json'
    class_name_list = [filename.split('/')[-1][:-5] for (bug, dir, filename) in meta]
    
    # use insert.pt to generate insertion patches
    # model_file = SRC_DIR + '../data/models/insert.pt'
    input_file = input_dir + 'input_insert_ast.json'
    # output_file = output_dir + 'insert.txt'
    generate_knod(model_file, input_file, identifier_file, class_name_list, devices, beam_size, output_file)


def rerank_ast_patches(meta_file, hypo_file_list, fix_type_list, output_file, dump=True):
    # step 2: rerank the AST of patches
    reranked = rerank_patches_by_rank(meta_file, hypo_file_list, fix_type_list, dump=dump, output_file=output_file)


# def validate_ast_patches(reranked_file, meta_file, identifiers_file, output_file, tmp_dir='/tmp/vul/'):
#     # step 3: convert AST of patches to source code patches and run test cases, will take a long time
#     validate_vul(
#         hypo_path=reranked_file,
#         meta_path=meta_file,
#         identifiers_path=identifiers_file,
#         output_path=output_file, 
#         tmp_dir=tmp_dir, progress_range=None
#     )


if __name__ == '__main__':
    devices = [0, 0]    # require two devices, but could be the same one

    for i in (1, 2, 3, 4):
        model_general = '/path_to_knod_model/general_/ast_repair/graph_transformer/models/general_' + str(i) + '/'
        if i == 1:
            model_general += '5.pt'
        else:
            model_general += '10.pt'
        beam_size = 100
        generate_general_ast_patches(
            SRC_DIR + '../data/vul_input/', 
            SRC_DIR + '../data/vul_output/general_' + str(i) + '.txt', 
            model_general
        )
    
    beam_size = 20
    generate_insert_ast_patches(
        SRC_DIR + '../data/vul_input/', 
        SRC_DIR + '../data/vul_output/insert_1.txt', 
        '/path_to_knod_model/general_/ast_repair/graph_transformer/models/insert_1/5.pt'
    )
    
    rerank_ast_patches(
        SRC_DIR + '../data/vul_input/meta_localize.txt',
        [
            SRC_DIR + '../data/vul_output/general_1.txt', 
            SRC_DIR + '../data/vul_output/general_2.txt', 
            SRC_DIR + '../data/vul_output/general_3.txt', 
            SRC_DIR + '../data/vul_output/general_4.txt', 
            SRC_DIR + '../data/vul_output/insert_1.txt'
        ],
        ['general', 'general', 'general', 'general', 'insert'],
        SRC_DIR + '../data/vul_output/reranked.json', True
    )
    # validate_ast_patches(
    #     SRC_DIR + '../data/vul_output/reranked.json',
    #     SRC_DIR + '../data/vul_input/meta_localize.txt',
    #     SRC_DIR + '../data/vul_input/identifiers.json',
    #     SRC_DIR + '../data/vul_output/validation.json'
    # )
