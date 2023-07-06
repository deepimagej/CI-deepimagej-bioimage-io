from config import ROOTS
import utils

import collection_test
import models

from pathlib import Path


parsed_model = models.parse_model(collection_test.r_c[0])

assert utils.get_in(parsed_model, ["config", "bioimageio", "nickname"]) == "humorous-owl"

rdf_info = models.get_rdf_info(parsed_model)

assert rdf_info["type"] == "model"
assert rdf_info["dij-config?"]
assert rdf_info["run-mode"] is None

an_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.6334881", "6346477", "rdf.yaml")
tf_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.5749843", "5888237", "rdf.yaml")
pt_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.5874741", "5874742", "rdf.yaml")