from collection import *

resources_list = file_json_2_vector(ROOTS["pending-matrix-root"] / "lightweight_models.json")

assert resources_list == [{'resource_id': '10.5281/zenodo.7786492', 'version_id': '7786493'},
                          {'resource_id': '10.5281/zenodo.5914248', 'version_id': '6514622'},
                          {'resource_id': '10.5281/zenodo.6338614', 'version_id': '6338615'}]



