from config import ROOTS
import downloads
import collection
import models
import errors
from collection_test import resources_list as lightweight_json

lightweight_models = list(map(lambda x: models.build_model(x), collection.get_rdfs_to_test(lightweight_json)))
downloads.download_model(lightweight_models[0], verb=True)
assert errors.is_success_download(lightweight_models[0])

problematic_models = list(map(lambda x: models.build_model(x),
                              collection.get_rdfs_to_test(collection.file_json_2_vector(ROOTS["pending-matrix-root"] / "problematic_downloads.json"))))
downloads.download_model(problematic_models[1], verb=True)
assert not errors.is_success_download(problematic_models[1])
