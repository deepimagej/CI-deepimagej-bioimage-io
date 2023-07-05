from config import ROOTS
import utils

from collection_test import *
from models import *

parsed_model = parse_model(r_c[0])

assert utils.get_in(parsed_model, ["config", "bioimageio", "nickname"]) == "humorous-owl"

rdf_info = get_rdf_info(parsed_model)

assert rdf_info["type"] == "model"
assert rdf_info["dij-config?"]
assert rdf_info["run-mode"] is None

