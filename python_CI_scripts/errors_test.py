from config import ROOTS
import utils
import errors
import models
import models_test

from pathlib import Path

# test the initial checks
non_model = models.build_model(models_test.not_a_model_rdf)

assert not errors.is_model(non_model)
assert errors.is_model(models_test.a_model_record)

assert not errors.is_no_run_mode(models_test.model_with_run_mode)
assert errors.is_no_run_mode(models_test.a_model_record)

assert not errors.is_any_compatible_weight(non_model)
assert not errors.is_any_compatible_weight(models_test.a_model_record)
assert errors.is_any_compatible_weight(models_test.pt_model_record)
assert errors.is_any_compatible_weight(models_test.tf_model_record)

manual_rdf = Path(ROOTS["collection-root"], "10.5281", "zenodo.7786492", "7786493", "rdf.yaml")
manual_model = models.build_model(manual_rdf)
assert not errors.is_manually_tested(models_test.pt_model_record)
assert errors.is_manually_tested(manual_model)

# test reproduce checks
errors.get_metrics_file(models_test.tf_model_record)

# test discriminating errors
model_records = [models_test.a_model_record, models_test.pt_model_record, models_test.tf_model_record]
d_models = {"keep-testing": model_records}

d_models_weights = errors.check_error(d_models, ("no-compatible-weights", errors.is_any_compatible_weight))
assert utils.count_dict(d_models_weights) == {'keep-testing': 2, 'error-found': 1}
assert utils.count_dict(d_models_weights["error-found"]) == {'no-compatible-weights': 1}

d_all_models_runmode = errors.check_error({"keep-testing": models_test.all_model_records},
                                          list(errors.init_errors_fns.items())[0])
assert len(d_all_models_runmode["keep-testing"]) > 190
assert set(map(lambda x: x["name"], d_all_models_runmode["error-found"]["key-run-mode"])) == {
    'Cells and gland Segmentation (FRUNet)',
    'Glial Cell SMLM (DeepSTORM - ZeroCostDL4Mic)',
    'Pancreatic Cell Phase Contrast Segmentation (DeepWater - CTC submission)',
    'Skin lesions classification'}


# test separate_by_error

models_discriminated = errors.separate_by_error(model_records, errors.init_errors_fns)
assert utils.count_dict(models_discriminated) == {'keep-testing': 2, 'error-found': 2}
assert utils.count_dict(models_discriminated["error-found"]) == {'key-run-mode': 0, 'no-compatible-weights': 1}

all_actual_models = list(filter(lambda x: errors.is_model(x), models_test.all_model_records))
all_models_discriminated = errors.separate_by_error(all_actual_models, errors.init_errors_fns)
assert 50 < utils.count_dict(all_models_discriminated)["keep-testing"] < 150
assert utils.count_dict(all_models_discriminated["error-found"])['key-run-mode'] == 4
assert utils.count_dict(all_models_discriminated["error-found"])["no-compatible-weights"] > 20


# test reproduce errors
assert errors.get_output_metrics(models_test.tf_model_record) == {'mae-center': 0.0, 'max-val': 255.0,
                                                                  'mse-center': 0.0, 'mae': 0.0, 'mse': 0.0}
assert errors.is_ok_metrics(models_test.tf_model_record)