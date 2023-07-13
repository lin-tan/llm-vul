import codecs
import json
import os
import shutil
import sys
import time

from reconstruct import reconstruct_ctx_wrap_ast, combine_super_methods, reconstruct_wrap_ast
from reconstruct import reconstruct_patched_ctx_wrap_ast, semantic_reconstruct, remove_patch_wrap_ast, reconstruct_ast

VALIDATION_DIR = os.path.abspath(__file__)
VALIDATION_DIR = VALIDATION_DIR[: VALIDATION_DIR.rfind('/') + 1]


def read_rem(rem_path):
    fp = codecs.open(rem_path, 'r', 'utf-8')
    rem = []
    for line in fp.readlines():
        if line.strip() == '':
            rem.append([None, None, None, None, None])
        else:
            loc, index, rem_line = line.split('\t')
            start, end = loc.split('-')
            start_row, start_col = start.split(',')
            end_row, end_col = end.split(',')
            start_row, start_col = int(start_row), int(start_col)
            end_row, end_col = int(end_row), int(end_col)
            rem.append([
                start_row, start_col, end_row, end_col, rem_line.strip()
            ])
    fp.close()
    return rem


def read_meta(meta_path):
    fp = codecs.open(meta_path, 'r', 'utf-8')
    meta = []
    for line in fp.readlines():
        if len(line.strip().split()) == 6:
            proj, dir, path, rem_start, rem_end, tag = line.strip().split()
            meta.append([proj, dir, path, rem_start, rem_end, tag])
    fp.close()
    return meta


def insert_fix_vul(file_path, start_row, start_col, end_row, end_col, patch, project_dir, fix_type='general',
                         key=None):
    file_path = project_dir + file_path
    shutil.copyfile(file_path, file_path + '.bak')

    with open(file_path, 'r') as file:
        data = file.readlines()

    if fix_type == 'general':
        with open(file_path, 'w') as file:
            for i in range(start_row - 1):
                file.write(data[i])
            file.write(data[start_row - 1][: start_col - 1] + '\n')
            file.write(patch)
            file.write(data[end_row - 1][end_col:])
            for i in range(end_row, len(data)):
                file.write(data[i])
    else:
        with open(file_path, 'w') as file:
            for i in range(start_row - 1):
                file.write(data[i])
            file.write(patch)
            file.write(data[end_row - 1])
            for i in range(end_row, len(data)):
                file.write(data[i])

    return file_path + '.bak'


def reconstruct_vul(hypo_path, meta_path, identifiers_path, output_path):
    # global cnt, right
    identifiers_dict = json.load(open(identifiers_path, 'r'))
    hypo = json.load(open(hypo_path, 'r'))

    meta = read_meta(meta_path)
    bugs_to_validate = list(hypo.keys())
    rem_general = read_rem(VALIDATION_DIR + '../../data/vul_input/rem_general_localize.txt')
    rem_insert = read_rem(VALIDATION_DIR + '../../data/vul_input/rem_insert_localize.txt')
    vul_general = json.load(open(VALIDATION_DIR + '../../data/vul_input/input_general_ast.json', 'r'))
    vul_general = {data['id']: data for data in vul_general}
    vul_insert = json.load(open(VALIDATION_DIR + '../../data/vul_input/input_insert_ast.json', 'r'))
    vul_insert = {data['id']: data for data in vul_insert}

    reconstruct_result = {}
    for line_num in bugs_to_validate:
        proj, dir, path, rem_start, rem_end, fix_type = meta[int(line_num) - 1]

        start_row_g, start_col_g, end_row_g, end_col_g, rem_line_g = rem_general[int(line_num) - 1]
        start_row_i, start_col_i, end_row_i, end_col_i, rem_line_i = rem_insert[int(line_num) - 1]

        print(proj)

        if int(line_num) in vul_general:
            data = vul_general[int(line_num)]
            src2abs_g = {k: v for k, v in data['mappings'].items() if '<UNK>' not in v}
            abs2src_g = {v: k for k, v in data['mappings'].items() if '<UNK>' not in v}
            ctx_ast_nodes_g, node_num_before_g, sibling_num_before_g = reconstruct_ctx_wrap_ast(
                data['nodes'], data['edges'], data['rem_roots'][0], abs2src_g
            )
        else:
            src2abs_g, abs2src_g = None, None
            ctx_ast_nodes_g, node_num_before_g, sibling_num_before_g = None, None, None
        if int(line_num) in vul_insert:
            data = vul_insert[int(line_num)]
            src2abs_i = {k: v for k, v in data['mappings'].items() if '<UNK>' not in v}
            abs2src_i = {v: k for k, v in data['mappings'].items() if '<UNK>' not in v}
            ctx_ast_nodes_i, node_num_before_i, sibling_num_before_i = reconstruct_ctx_wrap_ast(
                data['nodes'], data['edges'], data['rem_roots'][0], abs2src_i
            )
        else:
            src2abs_i, abs2src_i = None, None
            ctx_ast_nodes_i, node_num_before_i, sibling_num_before_i = None, None, None
        
        if str(line_num) not in identifiers_dict:
            identifiers = {}
        else:
            identifiers = identifiers_dict[str(line_num)]
        print('identifiers num:', len(identifiers))

        reconstruct_result[line_num] = {
            'id': hypo[line_num]['id'],
            'patches': []
        }
        for rank, patch_ast in enumerate(hypo[line_num]['patches']):
            try:
                patch_fix_type = patch_ast['fix_type']
                if patch_fix_type == 'general':
                    abs2src, src2abs = abs2src_g, src2abs_g
                    ctx_ast_nodes, node_num_before, sibling_num_before = \
                        ctx_ast_nodes_g, node_num_before_g, sibling_num_before_g
                    start_row, start_col, end_row, end_col = start_row_g, start_col_g, end_row_g, end_col_g
                else:
                    abs2src, src2abs = abs2src_i, src2abs_i
                    ctx_ast_nodes, node_num_before, sibling_num_before = \
                        ctx_ast_nodes_i, node_num_before_i, sibling_num_before_i
                    start_row, start_col, end_row, end_col = start_row_i, start_col_i, end_row_i, end_col_i

                if abs2src is None or ctx_ast_nodes is None:
                    continue

                if patch_ast['n'].strip() == '<EOS>':
                    code_patches = ['']
                elif '_<UNK>' in patch_ast['n']:
                    if identifiers is None:
                        continue
                    fathers, edges, nodes = patch_ast['f'].split(), patch_ast['e'].split(), patch_ast['n'].split()
                    patch_ast_roots, patch_ast_nodes = reconstruct_wrap_ast(fathers, edges, nodes, abs2src)
                    ast_nodes = reconstruct_patched_ctx_wrap_ast(ctx_ast_nodes, node_num_before, sibling_num_before,
                                                                 patch_ast_roots, patch_ast_nodes)
                    reconstructed_nodes = semantic_reconstruct(patch_ast_nodes, ast_nodes, path, src2abs, abs2src,
                                                               identifiers)
                    if patch_fix_type == 'general':
                        ctx_ast_nodes_g = remove_patch_wrap_ast(ast_nodes, node_num_before, sibling_num_before,
                                                                patch_ast_roots, patch_ast_nodes)
                    else:
                        ctx_ast_nodes_i = remove_patch_wrap_ast(ast_nodes, node_num_before, sibling_num_before,
                                                                patch_ast_roots, patch_ast_nodes)
                    if reconstructed_nodes is None or reconstructed_nodes == []:
                        continue
                    reconstruct_max = 2
                    code_patches = [
                        reconstruct_ast(
                            patch_ast['f'].split(),
                            patch_ast['e'].split(),
                            reconstructed_node,
                            abs2src)
                        for reconstructed_node in reconstructed_nodes[: reconstruct_max]
                    ]
                else:
                    code_patches = [reconstruct_ast(
                        patch_ast['f'].split(),
                        patch_ast['e'].split(),
                        patch_ast['n'].split(),
                        abs2src
                    )]
                score = patch_ast['score']
                for patch in code_patches:
                    reconstruct_result[line_num]['patches'].append(
                        {
                            'rank': len(reconstruct_result[line_num]['patches']) + 1,
                            'score': score,
                            'fix_type': patch_ast['fix_type'],
                            'patch': patch
                        }
                    )
            except Exception as e:
                if int(line_num) in vul_general:
                    data = vul_general[int(line_num)]
                    src2abs_g = {k: v for k, v in data['mappings'].items() if '<UNK>' not in v}
                    abs2src_g = {v: k for k, v in data['mappings'].items() if '<UNK>' not in v}
                    ctx_ast_nodes_g, node_num_before_g, sibling_num_before_g = reconstruct_ctx_wrap_ast(
                        data['nodes'], data['edges'], data['rem_roots'][0], abs2src_g
                    )
                else:
                    src2abs_g, abs2src_g = None, None
                    ctx_ast_nodes_g, node_num_before_g, sibling_num_before_g = None, None, None
                if int(line_num) in vul_insert:
                    data = vul_insert[int(line_num)]
                    src2abs_i = {k: v for k, v in data['mappings'].items() if '<UNK>' not in v}
                    abs2src_i = {v: k for k, v in data['mappings'].items() if '<UNK>' not in v}
                    ctx_ast_nodes_i, node_num_before_i, sibling_num_before_i = reconstruct_ctx_wrap_ast(
                        data['nodes'], data['edges'], data['rem_roots'][0], abs2src_i
                    )
                else:
                    src2abs_i, abs2src_i = None, None
                    ctx_ast_nodes_i, node_num_before_i, sibling_num_before_i = None, None, None
        json.dump(reconstruct_result, open(output_path, 'w'), indent=2)


if __name__ == '__main__':
    reconstruct_vul(
        hypo_path=VALIDATION_DIR + '../../data/vul_output/reranked.json',
        meta_path=VALIDATION_DIR + '../../data/vul_input/meta_localize.txt',
        identifiers_path=VALIDATION_DIR + '../../data/vul_input/identifiers.json',
        output_path=VALIDATION_DIR + '../../data/vul_output/reconstruct.json', 
    )