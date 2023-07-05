from config import ROOTS
import utils

from collection_test import *
from models import *

parsed_model = parse_model(r_c[0])

assert utils.get_in(parsed_model, ["config", "bioimageio", "nickname"]) == "humorous-owl"