import codecs
import functools
import json


def read_meta(meta_file):
    fp = codecs.open(meta_file, 'r', 'utf-8')
    meta = {}
    for i, l in enumerate(fp.readlines()):
        if len(l.split()) == 6:
            proj, dir, path, r_start, r_end, tag = l.strip().split()
        meta[i+1] = [proj, tag]
    fp.close()
    print(len(meta))
    return meta


def abstract_patch(patch):
    new_patch = []
    for token in patch.split():
        tokens = token.split('_')
        if len(tokens) == 2 and tokens[0] in ['VAR', 'METHOD', 'TYPE', 'STRING', 'CHAR', 'INT', 'FLOAT']:
            new_patch.append(tokens[0])
        else:
            new_patch.append(token)
    return ' '.join(new_patch)


def rerank_patches_by_rank(meta_file, file_list, fix_type_list, dump=False, output_file=None):
    def compare(p1, p2):
        if p1[1][0] == p2[1][0]:
            return p1[1][1] - p2[1][1]
        return p2[1][0] - p1[1][0]

    meta = read_meta(meta_file)
    hypo_list = []
    id_list = []
    for file, fix_type in zip(file_list, fix_type_list):
        print(file)
        hypo = {}
        fp = codecs.open(file, 'r', 'utf-8')
        for l in fp.readlines():
            l = l.split('\t')
            tag, flag, id = l[0].split('-')
            id = int(id)
            if id not in id_list:
                id_list.append(id)
            if id not in hypo:
                hypo[id] = {'id': id, 'target-f': '', 'target-e': '', 'target-n': '', 'patches': []}
            if tag == 'T' and meta[id][1] in file:
                hypo[id]['target-' + flag] = l[1].strip()
            elif tag == 'H':
                if flag == 'p':
                    hypo[id]['patches'].append({
                        'f': '', 'e': '', 'n': '', 'score': float(l[1].strip()), 'fix_type': fix_type
                    })
                elif flag == 'f':
                    hypo[id]['patches'][-1]['f'] = l[1].strip()
                elif flag == 'e':
                    hypo[id]['patches'][-1]['e'] = l[1].strip()
                elif flag == 'n':
                    hypo[id]['patches'][-1]['n'] = l[1].strip()
        fp.close()
        hypo_list.append(hypo)

    reranked = {}
    for id in id_list:
        reranked[id] = {'id': id, 'target-f': '', 'target-e': '', 'target-n': '', 'patches': []}
        if meta[id][1] == 'general' and id in hypo_list[0]:
            reranked[id]['target-f'] = hypo_list[0][id]['target-f']
            reranked[id]['target-e'] = hypo_list[0][id]['target-e']
            reranked[id]['target-n'] = hypo_list[0][id]['target-n']
        else:
            reranked[id]['target-f'] = hypo_list[-1][id]['target-f']
            reranked[id]['target-e'] = hypo_list[-1][id]['target-e']
            reranked[id]['target-n'] = hypo_list[-1][id]['target-n']
        tmp_patches = {}
        for rank in range(1000):
            same_rank_patches = {}
            for hypo in hypo_list:
                if id in hypo and len(hypo[id]['patches']) > rank:
                    patch = hypo[id]['patches'][rank]
                    key = patch['f'] + '<SEP>' + patch['e'] + '<SEP>' + patch['n'] + '<SEP>' + patch['fix_type']
                    if key not in same_rank_patches:
                        same_rank_patches[key] = patch['score']
                    else:
                        same_rank_patches[key] = max(same_rank_patches[key], patch['score'])
            for key in same_rank_patches:
                if key not in tmp_patches:
                    if key.split('<SEP>')[2].strip() == '<EOS>' and reranked[id]['target-n'].strip() == '<EOS>':
                        tmp_patches[key] = [0, same_rank_patches[key]]
                    else:
                        tmp_patches[key] = [rank, same_rank_patches[key]]
        tmp_patches = sorted(tmp_patches.items(), key=functools.cmp_to_key(compare), reverse=True)
        reranked[id]['patches'] = [
            {
                'f': p[0].split('<SEP>')[0],
                'e': p[0].split('<SEP>')[1],
                'n': p[0].split('<SEP>')[2],
                'rank': p[1][0],
                'score': p[1][1],
                'fix_type': p[0].split('<SEP>')[3]
            } for p in tmp_patches
        ]
    if dump:
        json.dump(reranked, open(output_file, 'w'), indent=2)
    return reranked



if __name__ == "__main__":
    hypo_dir = '../../data/vul_output/'
    meta_file = '../../data/vul_input/meta_localize.txt'
    output_file = '../../data/vul_output/reranked.json'
    
    reranked = rerank_patches_by_rank(
        [
            hypo_dir + 'general.txt',
            hypo_dir + 'insert.txt'
        ], 
        ['general', 'insert'], dump = True, output_file=output_file
    )