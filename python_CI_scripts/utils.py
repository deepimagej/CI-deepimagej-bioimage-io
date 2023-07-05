"""Generally used functions"""


def get_in(dic, key_list):
    """Returns the value in a nested associative structure, where key_list is a list of keys."""
    if len(key_list) == 0:
        return None
    return get_in(dic[key_list[0]], key_list[1:])
