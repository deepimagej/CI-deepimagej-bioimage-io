from config import ROOTS
from collection import *


resources_list = file_json_2_vector(ROOTS["pending-matrix-root"] / "lightweight_models.json")

assert resources_list == [{'resource_id': '10.5281/zenodo.7786492', 'version_id': '7786493'},
                          {'resource_id': '10.5281/zenodo.5914248', 'version_id': '6514622'},
                          {'resource_id': '10.5281/zenodo.6338614', 'version_id': '6338615'}]


rdf_paths_1 = resources_2_paths({'resource_id': '10.5281/zenodo.7786492', 'version_id': '7786493'})
rdf_paths_2 = resources_2_paths({'resource_id': '10.5281/zenodo.6200999', 'version_id': '**'})
rdf_paths_3 = resources_2_paths({'resource_id': '**', 'version_id': '**'})

assert rdf_paths_1 == [Path(ROOTS["collection-root"], '10.5281/zenodo.7786492','7786493',"rdf.yaml")]
assert len(rdf_paths_2) == 4
assert 190 <= len(rdf_paths_3) <= 300
