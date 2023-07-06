"""Generally used functions"""
from pathlib import Path


def get(dic, key):
    """Get value from key in a dictionary. Safer that dict[key] for key errors"""
    if key in dic:
        return dic[key]
    else:
        return None


def get_in(dic, key_list):
    """Returns the value in a nested associative structure, where key_list is a list of keys."""
    if len(key_list) == 0:
        return None
    if key_list[0] in dic:
        if len(key_list) == 1:
            return dic[key_list[0]]
        else:
            return get_in(dic[key_list[0]], key_list[1:])


def new_root_path(old_root, new_root, file_path):
    """Returns a path with a new root"""
    return Path(new_root) / file_path.parent.relative_to(old_root)
