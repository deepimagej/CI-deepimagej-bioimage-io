from config import ROOTS
from utils import *

# test new_root_path
a_path = Path("a_root", "first_folder", "second_folder", "file.txt")
expect = Path("a_new_root", "first_folder", "second_folder")

assert new_root_path("a_root", "a_new_root", a_path) == expect

# test group_by
groups = group_by(len, ["a", "as", "asd", "aa", "asdf", "qwer"])
assert groups == {1: ["a"], 2: ["as", "aa"], 3: ["asd"], 4: ["asdf", "qwer"]}

# test count_dict
d = {'a': [1, 2, 3, 4], 'b': [3, 5]}
assert count_dict(d) == {'a': 4, 'b': 2}
