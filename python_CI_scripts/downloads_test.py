from config import ROOTS
import downloads
import collection
import models
import errors
from collection_test import lightweight_json

# test download_model

lightweight_models = list(map(lambda x: models.build_model(x), collection.get_rdfs_to_test(lightweight_json)))
downloads.download_model(lightweight_models[0], verb=True)
assert errors.is_success_download(lightweight_models[0])

problematic_json = collection.file_json_2_vector(ROOTS["pending-matrix-root"] / "problematic_downloads.json")
problematic_models = list(map(lambda x: models.build_model(x), collection.get_rdfs_to_test(problematic_json)))
downloads.download_model(problematic_models[1], verb=True)
assert not errors.is_success_download(problematic_models[1])


# test save_correct_sample_images
use_cases_json = collection.file_json_2_vector(ROOTS["pending-matrix-root"] / "use_cases.json")
use_cases_models = list(map(lambda x: models.build_model(x), collection.get_rdfs_to_test(use_cases_json)))

downloads.save_correct_sample_images(lightweight_models[0], verb=True)

downloads.save_correct_sample_images(use_cases_models[0], verb=True)  # one uses a manual output file

