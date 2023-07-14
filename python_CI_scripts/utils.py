"""Generally used functions"""

from config import FILES, CONSTANTS

from functools import reduce
from pathlib import Path
from datetime import datetime


def get(dic, key):
    """Get value from key in a dictionary. Safer that dict[key] for key errors"""
    if key in dic:
        return dic[key]
    else:
        return None


def get_in(dic, key_list, default=None):
    """Returns the value in a nested associative structure, where key_list is a list of keys."""
    if len(key_list) == 0:
        return default
    if len(key_list) == 1:
        return dic.get(key_list[0], default)
    else:
        return get_in(dic.get(key_list[0], {}), key_list[1:], default=default)



def new_root_path(old_root, new_root, file_path):
    """Returns a path with a new root"""
    return Path(new_root) / file_path.parent.relative_to(old_root)


def group_by(f, coll):
    """Returns a dict of the elements of coll keyed by the result of
    f on each element. The value at each key will be a vector of the
    corresponding elements, in the order they appeared in coll."""

    def fn(acum, x):
        k = f(x)
        if k not in acum:
            acum[k] = [x]
        else:
            acum[k].append(x)
        return acum

    return reduce(fn, coll, {})


def count_dict(d):
    """Counts dictionary entries which are seqs"""
    def fn(acum, x):
        val = None if x[1] is None else len(x[1])
        acum.update({x[0]: val})
        return acum
    return reduce(fn, d.items(), {})


def print_and_log(msg, log_files, pr=True):
    """Prints a string message and logs it on all log files provided"""
    if pr:
        print(msg, flush=True, end="")
    for log in log_files:
        with open(log, 'a') as f:
            f.write(msg)


def print_start_msg(msg, indent_level=0):
    tic = datetime.now()
    indent = " " * 2 * indent_level
    print("{}{}, started at: {}".format(indent, msg, tic))
    return tic


def print_elapsed_time(tic, msg, indent_level=0):
    tac = datetime.now()
    indent = " " * 2 * indent_level
    print("{}{} at: {}".format(indent, msg, tac))
    print("{}Elapsed time: {}".format(indent, tac - tic))
    return tac


def bracketize(s, special_chars=CONSTANTS["special-headless-chars"]):
    """Surround a string with [brackets] if it has special characters (spaces, underscores, points, ...)"""
    contains_special = reduce(lambda x, y: x or y, (map(lambda x: x in s, special_chars)))
    if contains_special:
        return "[" + s + "]"
    else:
        return s