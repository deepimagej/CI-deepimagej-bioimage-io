from config import ROOTS
import collection

from pathlib import Path

# test file_json_2_vector
lightweight_json = collection.file_json_2_vector(ROOTS["pending-matrix-root"] / "lightweight_models.json")

assert lightweight_json == [{'resource_id': '10.5281/zenodo.7786492', 'version_id': '7786493'},
                            {'resource_id': '10.5281/zenodo.5914248', 'version_id': '6514622'},
                            {'resource_id': '10.5281/zenodo.6338614', 'version_id': '6338615'}]

# test resources_2_paths

rdf_paths_1 = collection.resources_2_paths({'resource_id': '10.5281/zenodo.7786492', 'version_id': '7786493'})
rdf_paths_2 = collection.resources_2_paths({'resource_id': '10.5281/zenodo.6200999', 'version_id': '**'})
rdf_paths_3 = collection.resources_2_paths({'resource_id': '**', 'version_id': '**'})

assert rdf_paths_1 == [Path(ROOTS["collection-root"], '10.5281/zenodo.7786492', '7786493', "rdf.yaml")]
assert len(rdf_paths_2) == 4
assert 190 <= len(rdf_paths_3) <= 300

# test get_rdfs_to_test
filenames = ["all_models.json", "two_models.json", "two_versions.json"]
files = map(lambda x: ROOTS["pending-matrix-root"] / x, filenames)
resource_vectors = map(lambda x: collection.file_json_2_vector(x), files)
r_a, r_b, r_c = list(map(lambda x: collection.get_rdfs_to_test(x), resource_vectors))

assert len(r_a) >= 165
assert {"7261975", "latest"} == set(map(lambda x: x.parent.name, r_b))
assert {"5888237", "5877226"} == set(map(lambda x: x.parent.name, r_c))

